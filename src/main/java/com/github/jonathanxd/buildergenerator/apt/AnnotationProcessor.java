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
import com.github.jonathanxd.buildergenerator.annotation.Default;
import com.github.jonathanxd.buildergenerator.annotation.DefaultImpl;
import com.github.jonathanxd.buildergenerator.annotation.GenBuilder;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.spec.BuilderSpec;
import com.github.jonathanxd.buildergenerator.spec.MethodRefSpec;
import com.github.jonathanxd.buildergenerator.spec.MethodSpec;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.buildergenerator.unification.UnificationFactory;
import com.github.jonathanxd.buildergenerator.unification.UnifiedDefaultImpl;
import com.github.jonathanxd.buildergenerator.unification.UnifiedGenBuilder;
import com.github.jonathanxd.buildergenerator.unification.UnifiedMethodRef;
import com.github.jonathanxd.buildergenerator.unification.UnifiedPropertyInfo;
import com.github.jonathanxd.buildergenerator.unification.UnifiedValidator;
import com.github.jonathanxd.buildergenerator.util.AnnotatedConstructUtil;
import com.github.jonathanxd.buildergenerator.util.ExecutableElementsUtil;
import com.github.jonathanxd.buildergenerator.util.FilerUtil;
import com.github.jonathanxd.buildergenerator.util.TypeElementUtil;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.TypeDeclaration;
import com.github.jonathanxd.codeapi.builder.MethodDeclarationBuilder;
import com.github.jonathanxd.codeapi.common.CodeModifier;
import com.github.jonathanxd.codeapi.common.CodeParameter;
import com.github.jonathanxd.codeapi.extra.AnnotationsKt;
import com.github.jonathanxd.codeapi.keyword.Keyword;
import com.github.jonathanxd.codeapi.keyword.Keywords;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.GenericType;
import com.github.jonathanxd.codeapi.util.Identity;
import com.github.jonathanxd.codeapi.util.ModelCodeTypesKt;
import com.github.jonathanxd.iutils.collection.CollectionUtils;
import com.github.jonathanxd.iutils.object.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
    private static final String DEFAULT_IMPL_ANNOTATION_CLASS = "com.github.jonathanxd.buildergenerator.annotation.DefaultImpl";
    private static final Pattern FQ_REGEX = Pattern.compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*");

    /**
     * Disables
     */
    private static final boolean disableStrictCheck = false;

    private boolean generateSource = true;
    private ProcessingEnvironment processingEnvironment;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
        this.messager = new BuilderGeneratorMessager(this.processingEnvironment.getMessager());
        Options.load(this.processingEnvironment.getOptions());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(DefaultImpl.class)) {
            try {
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;

                    UnifiedDefaultImpl annotation = AnnotatedConstructUtil
                            .getUnifiedAnnotation(executableElement, DEFAULT_IMPL_ANNOTATION_CLASS, UnifiedDefaultImpl.class)
                            .orElseThrow(() -> new IllegalStateException("Cannot find @DefaultImpl annotation"));

                    if (!MethodRefValidator.validate(executableElement, annotation.value(), this.getMessager(), this.processingEnvironment.getElementUtils(), MethodRefValidator.Type.DEFAULT_IMPL)) {
                        return false;
                    }


                }
            } catch (Throwable t) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "An error occurred '" + t.toString() + "'", element);
                t.printStackTrace(new MessagerPrint(this.getMessager()));
                return false;
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PropertyInfo.class)) {
            try {
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;

                    if (AnnotatedConstructUtil.getUnifiedAnnotation(executableElement, DEFAULT_IMPL_ANNOTATION_CLASS, UnifiedDefaultImpl.class).isPresent()) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Methods annotated with @DefaultImpl are not eligible to be a property, remove @PropertyInfo or @DefaultImpl annotation.", executableElement);
                        return false;
                    }

                    List<? extends VariableElement> parameters = executableElement.getParameters();

                    if (parameters.size() != 1) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Builder property method MUST have only one argument.", executableElement);
                        return false;
                    }

                    UnifiedPropertyInfo unifiedPropertyInfo =
                            AnnotatedConstructUtil.getUnifiedAnnotation(executableElement, PROPERTY_INFO_ANNOTATION_CLASS, UnifiedPropertyInfo.class)
                                    .orElseThrow(() -> new IllegalStateException("Cannot find @PropertyInfo annotation"));

                    UnifiedMethodRef defaultValue = unifiedPropertyInfo.defaultValue();

                    if (!Default.isDefaultMethodRef(defaultValue)) {

                        if (!MethodRefValidator.validate(executableElement, defaultValue, this.getMessager(), this.processingEnvironment.getElementUtils(), MethodRefValidator.Type.DEFAULT_VALUE)) {
                            return false;
                        }
                    }

                    UnifiedValidator unifiedValidator = unifiedPropertyInfo.validator();

                    if (!Default.isDefaultValidator(unifiedValidator)) {

                        UnifiedMethodRef methodRef = unifiedValidator.value();

                        if (!Default.isDefaultMethodRef(methodRef)) {
                            if (!MethodRefValidator.validate(executableElement, methodRef, this.getMessager(), this.processingEnvironment.getElementUtils(), MethodRefValidator.Type.VALIDATOR)) {
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

        List<String> processedTypes = new ArrayList<>();

        List<Pair<Element, Element>> genBuilderElements = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(GenBuilder.class)) {
            if (element.getKind() == ElementKind.CONSTRUCTOR || element.getKind() == ElementKind.METHOD) {
                genBuilderElements.add(Pair.of(element, element));
            } else if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                ExecutableElement moreArgs = null;

                for (Element elem : typeElement.getEnclosedElements()) {
                    if (elem instanceof ExecutableElement) {
                        if (moreArgs == null || moreArgs.getParameters().size() < ((ExecutableElement) elem).getParameters().size())
                            moreArgs = (ExecutableElement) elem;
                    }
                }

                if (moreArgs == null) {
                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "At least one constructor is required!", element);
                    return false;
                }

                genBuilderElements.add(Pair.of(moreArgs, element));
            } else {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid annotated element!", element);
                return false;
            }

        }

        for (Pair<Element, Element> basePair : genBuilderElements) {
            Element element = basePair._1();
            Element annotatedElement = basePair._2();

            try {

                boolean isConstructor = element.getKind() == ElementKind.CONSTRUCTOR;

                if (isConstructor || element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;

                    boolean isPublicStatic = !executableElement.getModifiers().contains(Modifier.PUBLIC) || !executableElement.getModifiers().contains(Modifier.STATIC);

                    if (!isConstructor && !isPublicStatic) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Factory method must be public and static.", annotatedElement);
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

                    Optional<UnifiedGenBuilder> mirrorOptional = AnnotatedConstructUtil.getUnifiedAnnotation(annotatedElement, BUILDER_GEN_ANNOTATION_CLASS, UnifiedGenBuilder.class);

                    if (mirrorOptional.isPresent()) {

                        UnifiedGenBuilder genBuilder = mirrorOptional.get();
                        AnnotationMirror annotationMirror = (AnnotationMirror) AnnotationsKt.getHandlerOfAnnotation(genBuilder).getOriginal();

                        if (!Default.isDefaultType(genBuilder.base())) {
                            baseType = genBuilder.base();
                        } else {
                            List<? extends TypeMirror> interfaces = ((TypeElement) enclosingElement).getInterfaces();

                            if (interfaces.size() == 1) {
                                baseType = TypeElementUtil.toCodeType(interfaces.get(0), processingEnvironment.getElementUtils());
                            } else {
                                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Base type cannot be inferred, please specify the base type!", annotatedElement, annotationMirror);
                                return false;
                            }
                        }

                        if (!genBuilder.qualifiedName().isEmpty()) {
                            String value = genBuilder.qualifiedName();


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
                                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid class name '" + value + "', the class name MUST match the java class naming rules (Java Language Specification, Section 3.8. Identifiers).", annotatedElement, annotationMirror);
                                return false;
                            }

                            builderQualifiedName = value;
                        } else {
                            builderQualifiedName = factoryResultType.getPackageName() + ".builder." + baseType.getSimpleName() + "Builder";
                        }

                    }

                    if (baseType == null) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot determine base type.", annotatedElement);
                        return false;
                    }

                    List<? extends VariableElement> parameters = executableElement.getParameters();

                    List<String> propertyOrder = new ArrayList<>();

                    for (VariableElement parameter : parameters) {
                        propertyOrder.add(parameter.getSimpleName().toString());
                    }


                    TypeElement baseTypeElement = processingEnvironment.getElementUtils().getTypeElement(baseType.getCanonicalName());

                    TypeElement builder = null;

                    List<ExecutableElement> executables = new ArrayList<>();

                    this.consumeMethods(baseTypeElement, e -> {
                        String name = e.getSimpleName().toString();
                        if (executables.stream().noneMatch(elem -> elem.getSimpleName().toString().equals(name)))
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

                    List<MethodSpec> methodSpecs = new ArrayList<>();
                    List<ExecutableElement> builderMethods = new ArrayList<>();


                    List<PropertySpec> propertySpecs = new ArrayList<>();

                    String boundTypeName = null;

                    CodeType bdType = ModelCodeTypesKt.getCodeTypeFromTypeParameters(builder);

                    if (bdType instanceof GenericType) {
                        GenericType genericType = (GenericType) bdType;

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

                    this.consumeMethods(builder, methodToConsume -> {
                        Optional<UnifiedDefaultImpl> unifiedDefaultImplOpt = AnnotatedConstructUtil.getUnifiedAnnotation(methodToConsume, "com.github.jonathanxd.buildergenerator.annotation.DefaultImpl", UnifiedDefaultImpl.class);

                        if (methodToConsume.isDefault() || unifiedDefaultImplOpt.isPresent()) {

                            if (!methodToConsume.isDefault()) {
                                UnifiedDefaultImpl unifiedDefault = unifiedDefaultImplOpt.get();

                                List<CodeParameter> collect = methodToConsume
                                        .getParameters()
                                        .stream()
                                        .map(o -> new CodeParameter(TypeElementUtil.fromGenericMirror(o.asType())/*TypeElementUtil.toCodeType(o.asType(), this.processingEnvironment.getElementUtils())*/, o.getSimpleName().toString()))
                                        .collect(Collectors.toList());

                                CodeType rtype = TypeElementUtil.fromGenericMirror(methodToConsume.getReturnType());

                                MethodDeclaration targetMethod = MethodDeclarationBuilder.builder()
                                        .withModifiers(CodeModifier.PUBLIC)
                                        .withName(methodToConsume.getSimpleName().toString())
                                        .withReturnType(rtype)
                                        .withParameters(collect)
                                        .build();

                                MethodRefSpec methodRefSpec = MethodRefValidator.get(methodToConsume, unifiedDefault.value(), this.processingEnvironment.getElementUtils(), MethodRefValidator.Type.DEFAULT_IMPL);

                                methodSpecs.add(new MethodSpec(targetMethod, methodRefSpec));

                            }
                        } else {
                            builderMethods.add(methodToConsume);
                        }

                    });

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

                        boolean isValid = params.size() == 1;

                        if (!Options.isDisableStrictSetterCheck()) {
                            if (!Identity.nonStrictEq(parameterType, type)
                                    || (boundTypeName == null && !Identity.nonStrictEq(returnType, builderType))
                                    || (boundTypeName != null && !returnType.getCanonicalName().equals(boundTypeName))) {
                                isValid = false;
                            }
                        }

                        if (!isValid) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Property setter method '"
                                            + simpleName
                                            + "' of property '"
                                            + s
                                            + "' must have only one parameter of type '"
                                            + type
                                            + "' (current "
                                            + parameterType
                                            + ") and return type '"
                                            + builderType
                                            + (boundTypeName != null ? " (or " + boundTypeName + ")" : "")
                                            + "' (current: "
                                            + returnType
                                            + ").", withMethod);
                            return false;
                        } else {


                            Optional<UnifiedPropertyInfo> unifiedPropertyInfoOpt = AnnotatedConstructUtil.getUnifiedAnnotation(withMethod, PROPERTY_INFO_ANNOTATION_CLASS, UnifiedPropertyInfo.class);

                            if (unifiedPropertyInfoOpt.isPresent()) {

                                PropertySpec from = this.from(s, type, withMethod, unifiedPropertyInfoOpt.get(), isNullable, isOptional);

                                String name = from.getDefaultsPropertyName();

                                if (!name.equals(s)) {

                                    Optional<ExecutableElement> propertyGetter = ExecutableElementsUtil.get(executables, "get" + StringsKt.capitalize(name));

                                    if (!propertyGetter.isPresent()) {
                                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Specified property name '" + name + "' cannot be found!.", withMethod,
                                                (AnnotationMirror) AnnotationsKt.getHandlerOfAnnotation(unifiedPropertyInfoOpt.get()).getOriginal());
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

                    BuilderSpec builderSpec = new BuilderSpec(builderQualifiedName, factoryClass, factoryResultType, factoryMethodName, baseType, bdType, propertySpecs, methodSpecs);


                    if (!roundEnv.processingOver()) {

                        CodeAPIBuilderGenerator.Source builderGenerator = new CodeAPIBuilderGenerator.Source();

                        Pair<TypeDeclaration, String> pair = builderGenerator.generate(builderSpec, methodTypeSpecs -> {
                        });

                        TypeDeclaration declaration = pair._1();

                        Optional<FileObject> fileObject = FilerUtil.get(this.processingEnvironment.getFiler(), declaration.getPackageName(), declaration.getSimpleName());

                        fileObject.ifPresent(FileObject::delete);

                        String qualifiedName = declaration.getQualifiedName();

                        if (processedTypes.contains(qualifiedName)) {
                            this.getMessager().printMessage(Diagnostic.Kind.WARNING, "Already processed!", annotatedElement);
                        } else {

                            try {
                                Element[] origin;

                                if (element == annotatedElement) {
                                    origin = new Element[]{element};
                                } else {
                                    origin = new Element[]{annotatedElement, element};
                                }

                                JavaFileObject classFile = processingEnvironment.getFiler().createSourceFile(qualifiedName, origin);

                                OutputStream outputStream = classFile.openOutputStream();

                                outputStream.write(pair._2().getBytes("UTF-8"));

                                outputStream.flush();
                                outputStream.close();

                                processedTypes.add(qualifiedName);
                            } catch (FilerException e) {
                                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to create source file of Builder class '" + qualifiedName + "' (file already exists?): " + e.getMessage(), annotatedElement);
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to create source file of Builder class '" + qualifiedName + "': " + e.getMessage(), annotatedElement);
                                throw new RuntimeException(e);
                            }
                        }

                    }
                }
            } catch (Throwable t) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "An error occurred '" + t.toString() + "'", annotatedElement);
                t.printStackTrace(new MessagerPrint(this.getMessager()));
                return false;
            }
        }

        return false;
    }

    private PropertySpec from(String name, CodeType type, ExecutableElement annotated, UnifiedPropertyInfo unifiedPropertyInfo, boolean isNullable_, boolean isOptional) {


        boolean isNullable = isNullable_ || unifiedPropertyInfo.isNullable();

        String defaultsPropertyName = Default.stringOptional(unifiedPropertyInfo.defaultsPropertyName()).orElse(name);

        MethodRefSpec defaultValue = Default.methodRefOptional(unifiedPropertyInfo.defaultValue())
                .map(annotation -> MethodRefValidator.get(annotated, annotation, this.processingEnvironment.getElementUtils(), MethodRefValidator.Type.DEFAULT_VALUE))
                .orElse(null);


        MethodRefSpec validator = Default.methodRefOptional(unifiedPropertyInfo.validator().value())
                .map(annotation -> MethodRefValidator.get(annotated, annotation, this.processingEnvironment.getElementUtils(), MethodRefValidator.Type.VALIDATOR))
                .orElse(null);

        return new PropertySpec(name, defaultsPropertyName, type, isNullable, isOptional, defaultValue, validator);

    }

    void consumeMethods(TypeElement typeElement, Consumer<ExecutableElement> consumer) {
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement) {
                consumer.accept((ExecutableElement) element);
            }
        }

        TypeMirror superclass = typeElement.getSuperclass();

        List<Runnable> later = new ArrayList<>();

        if (superclass.getKind() != TypeKind.NONE && !superclass.toString().equals("java.lang.Object")) {
            TypeElement element = TypeElementUtil.toTypeElement(superclass, processingEnvironment.getElementUtils());

            if (element == null) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find super class '" + superclass.toString() + "'!", typeElement);
                return;
            }

            later.add(() -> consumeMethods(processingEnvironment.getElementUtils().getTypeElement(superclass.toString()), consumer));
        }

        for (TypeMirror itf : typeElement.getInterfaces()) {

            if (itf.getKind() != TypeKind.NONE) {
                TypeElement element = TypeElementUtil.toTypeElement(itf, processingEnvironment.getElementUtils());

                if (element == null) {
                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find interface '" + itf.toString() + "'!", typeElement);
                    return;
                }

                later.add(() -> consumeMethods(element, consumer));
            }
        }

        later.forEach(Runnable::run);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return CollectionUtils.setOf(BUILDER_GEN_ANNOTATION_CLASS, INLINE_ANNOTATION_CLASS, PROPERTY_INFO_ANNOTATION_CLASS, DEFAULT_IMPL_ANNOTATION_CLASS);
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