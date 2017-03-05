/*
 *      BuilderGenerator - Builder implementation generator <https://github.com/JonathanxD/BuilderGenerator>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 JonathanxD
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.buildergenerator.apt;

import com.github.jonathanxd.buildergenerator.CodeAPIBuilderGenerator;
import com.github.jonathanxd.buildergenerator.annotation.Conversions;
import com.github.jonathanxd.buildergenerator.annotation.GenBuilder;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.spec.BuilderSpec;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.buildergenerator.util.AnnotatedConstructUtil;
import com.github.jonathanxd.buildergenerator.util.AnnotationMirrorUtil;
import com.github.jonathanxd.buildergenerator.util.ExecutableElementsUtil;
import com.github.jonathanxd.buildergenerator.util.FilerUtil;
import com.github.jonathanxd.buildergenerator.util.TypeElementUtil;
import com.github.jonathanxd.buildergenerator.util.TypeResolver;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.Annotation;
import com.github.jonathanxd.codeapi.base.TypeDeclaration;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.keyword.Keyword;
import com.github.jonathanxd.codeapi.keyword.Keywords;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.GenericType;
import com.github.jonathanxd.codeapi.util.GenericTypeUtil;
import com.github.jonathanxd.iutils.collection.CollectionUtils;
import com.github.jonathanxd.iutils.object.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;

import kotlin.text.StringsKt;

public class AnnotationProcessor extends AbstractProcessor {

    private static final String BUILDER_GEN_ANNOTATION_CLASS = "com.github.jonathanxd.buildergenerator.annotation.GenBuilder";
    private static final String PROPERTY_INFO_ANNOTATION_CLASS = "com.github.jonathanxd.buildergenerator.annotation.PropertyInfo";
    private static final String INLINE_ANNOTATION_CLASS = "com.github.jonathanxd.buildergenerator.annotation.Inline";
    private static final Pattern FQ_REGEX = Pattern.compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*");

    private boolean generateSource = true;
    private ProcessingEnvironment processingEnvironment;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
        this.messager = new BuilderGeneratorMessager(this.processingEnvironment.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(PropertyInfo.class)) {
            try {
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;

                    List<? extends VariableElement> parameters = executableElement.getParameters();

                    if (parameters.size() != 1) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Builder property method MUST have only one argument.", executableElement);
                        return false;
                    }

                    Optional<AnnotationMirror> propertyInfoAnnotation = AnnotatedConstructUtil.getAnnotationMirror(executableElement, PROPERTY_INFO_ANNOTATION_CLASS);

                    AnnotationMirror propertyInfoMirror = propertyInfoAnnotation.orElseThrow(() -> new IllegalStateException("Cannot find @PropertyInfo annotation"));

                    Annotation annotation = (Annotation) AnnotationMirrorUtil.toCodeAPI(propertyInfoMirror, this.processingEnvironment.getElementUtils());

                    CodeType propertyType = TypeElementUtil.toCodeType(parameters.get(0).asType(), this.processingEnvironment.getElementUtils());

                    Object defaultValue = annotation.getValues().get("defaultValue");

                    if (defaultValue != null) {

                        if (!(defaultValue instanceof Annotation)) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid input type for 'defaultValue' value in @PropertyInfo annotation: 'defaultValue' must be a @MethodRef instance.", executableElement, propertyInfoMirror);
                            return false;
                        }

                        if (!MethodRefValidator.validate(executableElement, propertyInfoMirror, (Annotation) defaultValue, propertyType, this.getMessager(), this.processingEnvironment.getElementUtils(), false)) {
                            return false;
                        }
                    }

                    Object validator = annotation.getValues().get("validator");

                    if (validator != null) {

                        if (!(validator instanceof Annotation)) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid input type for 'validator' value in @PropertyInfo annotation: 'defaultValue' must be a @Validator instance.", executableElement, propertyInfoMirror);
                            return false;
                        }

                        Annotation validatorAnnotation = (Annotation) validator;

                        Object value = validatorAnnotation.getValues().get("value");

                        if (value != null) {
                            if (!(value instanceof Annotation)) {
                                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid input type for 'value' value in @Validator annotation: 'value' must be a @MethodRef instance.", executableElement, propertyInfoMirror);
                                return false;
                            }

                            if (!MethodRefValidator.validate(executableElement, propertyInfoMirror, (Annotation) value, propertyType, this.getMessager(), this.processingEnvironment.getElementUtils(), true)) {
                                return false;
                            }
                        }

                    }
                }
            } catch (Throwable t) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "An error occurred '" + t.toString() + "'", element);
                t.printStackTrace(new MessagerPrint(this.getMessager()));
                return false;
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(GenBuilder.class)) {
            try {
                boolean isConstructor = element.getKind() == ElementKind.CONSTRUCTOR;

                if (isConstructor || element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;

                    boolean isPublicStatic = !executableElement.getModifiers().contains(Modifier.PUBLIC) || !executableElement.getModifiers().contains(Modifier.STATIC);

                    if (!isConstructor && !isPublicStatic) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Factory method must be public and static.", element);
                        return false;
                    }

                    Element enclosingElement = executableElement.getEnclosingElement();

                    if (!(enclosingElement instanceof TypeElement)) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Enclosing element of method must be a Type.", enclosingElement);
                        return false;
                    }

                    String builderQualifiedName = null;
                    CodeType factoryClass = TypeElementUtil.toCodeType((TypeElement) enclosingElement);
                    CodeType factoryResultType;
                    CodeType baseType = null;
                    String factoryMethodName = null;

                    if (isConstructor) {
                        factoryResultType = factoryClass;
                    } else {
                        baseType = TypeElementUtil.toCodeType(executableElement.getReturnType(), processingEnvironment.getElementUtils());
                        factoryResultType = baseType;
                        factoryMethodName = executableElement.getSimpleName().toString();
                    }

                    Optional<AnnotationMirror> mirrorOptional = AnnotatedConstructUtil.getAnnotationMirror(element, BUILDER_GEN_ANNOTATION_CLASS);

                    if (mirrorOptional.isPresent()) {
                        AnnotationMirror annotationMirror = mirrorOptional.get();

                        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();

                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                            if (entry.getKey().getSimpleName().contentEquals("base")) {
                                AnnotationValue value = entry.getValue();

                                Object baseValue = value.getValue();

                                if (!(baseValue instanceof DeclaredType)) {
                                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Value '" + baseValue + "' provided to 'base' property of @GenBuilder annotation is not valid.", element, annotationMirror, value);
                                    return false;
                                }

                                baseType = TypeElementUtil.toCodeType((DeclaredType) baseValue, processingEnvironment.getElementUtils());
                            }

                            if (entry.getKey().getSimpleName().contentEquals("qualifiedName")) {
                                String value = (String) entry.getValue().getValue();


                                boolean isKeyword = false;
                                for (Field field : Keywords.class.getDeclaredFields()) {

                                    if (Keyword.class.isAssignableFrom(field.getType())) {
                                        try {
                                            isKeyword |= value.equals(((Keyword) field.get(null)).getName());
                                        } catch (IllegalAccessException ignored) {
                                        }
                                    }
                                }

                                if (value.equals("null") || value.equals("true") || value.equals("false") || isKeyword || !FQ_REGEX.matcher(value).matches()) {
                                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid class name '" + value + "', the class name MUST match the java class naming rules (Java Language Specification, Section 3.8. Identifiers).", element, annotationMirror, entry.getValue());
                                    return false;
                                }

                                if (!value.isEmpty())
                                    builderQualifiedName = value;
                            }
                        }

                        if (baseType == null) {
                            List<? extends TypeMirror> interfaces = ((TypeElement) enclosingElement).getInterfaces();

                            if (interfaces.size() == 1) {
                                baseType = TypeElementUtil.toCodeType(interfaces.get(0), processingEnvironment.getElementUtils());
                            } else {
                                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Base type cannot be inferred, please specify the base type!", element, annotationMirror);
                                return false;
                            }
                        }

                    }

                    if (baseType == null) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot determine base type.", element);
                        return false;
                    }

                    if (builderQualifiedName == null) {
                        builderQualifiedName = factoryResultType.getPackageName() + ".builder." + factoryResultType.getSimpleName() + "Builder";
                    }

                    List<? extends VariableElement> parameters = executableElement.getParameters();

                    List<String> propertyOrder = new ArrayList<>();

                    List<Pair<String, GenericType>> propertiesNameAndType = new ArrayList<>();

                    for (VariableElement parameter : parameters) {
                        TypeMirror typeMirror = parameter.asType();
                        String s = typeMirror.toString();

                        GenericType parameterType = GenericTypeUtil.fromSourceString(s, new TypeResolver(processingEnvironment.getElementUtils()));

                        propertyOrder.add(parameter.getSimpleName().toString());

                        propertiesNameAndType.add(Pair.of(parameter.getSimpleName().toString(), parameterType));
                    }


                    TypeElement baseTypeElement = processingEnvironment.getElementUtils().getTypeElement(baseType.getCanonicalName());

                    TypeElement builder = null;

                    List<ExecutableElement> executables = new ArrayList<>();

                    this.consumeMethods(baseTypeElement, e -> {
                        if (executables.stream().noneMatch(elem -> elem.getSimpleName().toString().equals(e.getSimpleName().toString())))
                            executables.add(e);
                    });

                    for (Element enclosedElement : baseTypeElement.getEnclosedElements()) {

                        if (enclosedElement instanceof TypeElement) {
                            TypeElement innerClass = (TypeElement) enclosedElement;

                            if (innerClass.getSimpleName().contentEquals("Builder")) {

                                if (innerClass.getKind() != ElementKind.INTERFACE) {
                                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid 'Builder' in base type '" + baseType + "': The 'Builder' must be an interface.", innerClass);
                                    return false;
                                }

                                builder = innerClass;
                            }

                        }
                    }

                    if (builder == null) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find Builder class in base type '" + baseType + "'.", baseTypeElement);
                        return false;
                    }

                    CodeType builderType = TypeElementUtil.toCodeType(builder);

                    List<ExecutableElement> builderMethods = new ArrayList<>();

                    this.consumeMethods(builder, builderMethods::add);

                    List<PropertySpec> propertySpecs = new ArrayList<>();

                    for (String s : propertyOrder) {

                        Optional<ExecutableElement> optional = ExecutableElementsUtil.get(executables, "get" + StringsKt.capitalize(s));
                        Optional<ExecutableElement> builderMethod = ExecutableElementsUtil.get(builderMethods, "with" + StringsKt.capitalize(s));

                        if (!optional.isPresent()) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Missing getter 'get" + StringsKt.capitalize(s) + "' method of property '" + s + "'.", baseTypeElement);
                            return false;
                        }

                        if (!builderMethod.isPresent()) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Missing Builder 'with" + StringsKt.capitalize(s) + "' method of property '" + s + "'.", builder);
                            return false;
                        }

                        ExecutableElement getter = optional.get();
                        ExecutableElement withMethod = builderMethod.get();

                        GenericType propertyType = (GenericType) TypeElementUtil.toCodeType(getter.getReturnType(), this.processingEnvironment.getElementUtils());

                        String simpleName = withMethod.getSimpleName().toString();
                        List<? extends VariableElement> params = withMethod.getParameters();
                        CodeType parameterType = TypeElementUtil.toCodeType(params.get(0).asType(), this.processingEnvironment.getElementUtils());
                        CodeType returnType = TypeElementUtil.toCodeType(withMethod.getReturnType(), this.processingEnvironment.getElementUtils());

                        CodeType type = propertyType;

                        boolean any = false;
                        boolean isNullable = false;
                        boolean isOptional = false;

                        if (type.getCanonicalName().equals("java.util.Optional") && !type.is(parameterType)) {

                            GenericType.Bound[] bounds = propertyType.getBounds();

                            if (bounds.length > 0) {
                                type = bounds[0].getType();
                                isOptional = true;
                            }
                        }


                        String boundTypeName = null;

                        if (baseType instanceof GenericType) {
                            GenericType genericType = (GenericType) baseType;

                            if (genericType.getBounds().length == 2) {
                                GenericType.Bound bound = genericType.getBounds()[1];
                                CodeType boundType = bound.getType();

                                if (boundType instanceof GenericType) {
                                    GenericType bound_ = (GenericType) boundType;

                                    if (!bound_.isType() && bound_.getBounds().length == 1) {
                                        if (bound_.getBounds()[0].getType().getCanonicalName().equals(builderType.getCanonicalName()))
                                            boundTypeName = bound_.getName();
                                    }
                                }
                            }

                        }

                        if (params.size() != 1
                                || !parameterType.is(type)
                                || !returnType.getCanonicalName().equals(builderType.getCanonicalName())
                                || (boundTypeName != null && !boundTypeName.equals(builderType.getCanonicalName()))) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Property setter method '" + simpleName + "' of property '" + s + "' MUST have only one parameter of type '" + type + "' (current " + parameterType + ") and return type '" + builderType.getCanonicalName() + "' (current: " + returnType.getCanonicalName() + ").", withMethod);
                        } else {


                            Optional<AnnotationMirror> mirror = AnnotatedConstructUtil.getAnnotationMirror(withMethod, PROPERTY_INFO_ANNOTATION_CLASS);

                            if (mirror.isPresent()) {

                                PropertySpec from = from(s, type, mirror.get(), isNullable, isOptional);

                                String name = from.getDefaultsPropertyName();

                                if (!name.equals(s)) {
                                    if (!propertyOrder.contains(name)) {
                                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Specified property name '" + name + "' cannot be found!.", withMethod, mirror.get());
                                        return false;
                                    }
                                }

                                propertySpecs.add(from);
                                any = true;
                            }
                        }


                        if (!any) {
                            propertySpecs.add(new PropertySpec(s, s, type, isNullable, isOptional, null, null));
                        }

                    }

                    BuilderSpec builderSpec = new BuilderSpec(builderQualifiedName, factoryClass, factoryResultType, factoryMethodName, baseType, builderType, propertySpecs);


                    if (!roundEnv.processingOver()) {

                        CodeAPIBuilderGenerator.Source builderGenerator = new CodeAPIBuilderGenerator.Source();

                        Pair<TypeDeclaration, String> pair = builderGenerator.generate(builderSpec);

                        TypeDeclaration declaration = pair._1();

                        Optional<FileObject> fileObject = FilerUtil.get(this.processingEnvironment.getFiler(), declaration.getPackageName(), declaration.getSimpleName());

                        fileObject.ifPresent(FileObject::delete);

                        String qualifiedName = declaration.getQualifiedName();

                        try {
                            JavaFileObject classFile = processingEnvironment.getFiler().createSourceFile(qualifiedName, element);

                            OutputStream outputStream = classFile.openOutputStream();

                            outputStream.write(pair._2().getBytes("UTF-8"));

                            outputStream.flush();
                            outputStream.close();
                        } catch (FilerException e) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to create source file of Builder class '" + qualifiedName + "' (file already exists?): " + e.getMessage(), element);
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to create source file of Builder class '" + qualifiedName + "': " + e.getMessage(), element);
                            throw new RuntimeException(e);
                        }

                    }
                    return true;

                }
            } catch (Throwable t) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "An error occurred '" + t.toString() + "'", element);
                t.printStackTrace(new MessagerPrint(this.getMessager()));
                return false;
            }
        }

        return false;
    }

    private PropertySpec from(String name, CodeType type, AnnotationMirror annotationMirror, boolean isNullable_, boolean isOptional) {

        AnnotationMirrorHelper annotationMirrorHelper = new AnnotationMirrorHelper(annotationMirror, processingEnvironment.getElementUtils());


        boolean isNullable = annotationMirrorHelper.<Boolean>get("isNullable").orElse(isNullable_);

        String defaultsPropertyName = annotationMirrorHelper.<String>get("defaultsPropertyName").orElse(name);

        MethodTypeSpec defaultValue = Conversions.CAPI.toMethodSpec(
                annotationMirrorHelper.<Annotation>get("defaultValue").orElse(null),
                type,
                new CodeType[]{Types.STRING}
        ).orElse(null);

        MethodTypeSpec validator = Conversions.CAPI.validatorToMethodSpec(
                annotationMirrorHelper.<Annotation>get("validator").orElse(null),
                type,
                new CodeType[]{Types.STRING}
        ).orElse(null);

        return new PropertySpec(name, defaultsPropertyName, type, isNullable, isOptional, defaultValue, validator);

    }

    private void consumeMethods(TypeElement typeElement, Consumer<ExecutableElement> consumer) {
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement) {
                consumer.accept((ExecutableElement) element);
            }
        }

        TypeMirror superclass = typeElement.getSuperclass();

        if (superclass.getKind() != TypeKind.NONE && !superclass.toString().equals("java.lang.Object")) {
            TypeElement element = TypeElementUtil.toTypeElement(superclass, processingEnvironment.getElementUtils());

            if (element == null) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find super class '" + superclass.toString() + "'", typeElement);
                return;
            }

            consumeMethods(processingEnvironment.getElementUtils().getTypeElement(superclass.toString()), consumer);
        }

        for (TypeMirror itf : typeElement.getInterfaces()) {

            if (itf.getKind() != TypeKind.NONE) {
                TypeElement element = TypeElementUtil.toTypeElement(itf, processingEnvironment.getElementUtils());

                if (element == null) {
                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find interface '" + itf.toString() + "'", typeElement);
                    return;
                }

                consumeMethods(element, consumer);
            }
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return CollectionUtils.setOf(BUILDER_GEN_ANNOTATION_CLASS, INLINE_ANNOTATION_CLASS, PROPERTY_INFO_ANNOTATION_CLASS);
    }

    private Messager getMessager() {
        return this.messager;
    }

    private static final class MessagerPrint extends PrintStream {

        public MessagerPrint(Messager messager) {
            super(new MessagerOutStream(messager));
        }


    }

    private static final class MessagerOutStream extends OutputStream {
        private final Messager messager;
        private final StringBuilder sb = new StringBuilder();

        private MessagerOutStream(Messager messager) {
            this.messager = messager;
        }

        @Override
        public void write(int b) throws IOException {

            if (b == '\n') {
                messager.printMessage(Diagnostic.Kind.ERROR, sb.toString());
                sb.setLength(0);
            }


            if (Character.isValidCodePoint(b)) {
                sb.append((char) b);
            }
        }
    }
}
