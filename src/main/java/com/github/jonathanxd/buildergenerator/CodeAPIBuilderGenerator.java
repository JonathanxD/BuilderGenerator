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
package com.github.jonathanxd.buildergenerator;

import com.github.jonathanxd.buildergenerator.spec.BuilderSpec;
import com.github.jonathanxd.buildergenerator.spec.MethodRefSpec;
import com.github.jonathanxd.buildergenerator.spec.MethodSpec;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.buildergenerator.util.CTypeUtil;
import com.github.jonathanxd.buildergenerator.util.InlineMethodInvoker;
import com.github.jonathanxd.buildergenerator.util.MethodInvocationUtil;
import com.github.jonathanxd.buildergenerator.util.MethodResolver;
import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.MutableCodeSource;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.ClassDeclaration;
import com.github.jonathanxd.codeapi.base.FieldAccess;
import com.github.jonathanxd.codeapi.base.FieldDeclaration;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.TypeDeclaration;
import com.github.jonathanxd.codeapi.base.VariableBase;
import com.github.jonathanxd.codeapi.builder.ClassDeclarationBuilder;
import com.github.jonathanxd.codeapi.builder.FieldDeclarationBuilder;
import com.github.jonathanxd.codeapi.builder.MethodDeclarationBuilder;
import com.github.jonathanxd.codeapi.bytecode.BytecodeClass;
import com.github.jonathanxd.codeapi.bytecode.BytecodeOptions;
import com.github.jonathanxd.codeapi.bytecode.VisitLineType;
import com.github.jonathanxd.codeapi.bytecode.gen.BytecodeGenerator;
import com.github.jonathanxd.codeapi.common.CodeModifier;
import com.github.jonathanxd.codeapi.common.CodeParameter;
import com.github.jonathanxd.codeapi.common.InvokeType;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.common.TypeSpec;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.ConstructorFactory;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.source.gen.PlainSourceGenerator;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.Generic;
import com.github.jonathanxd.codeapi.type.GenericType;
import com.github.jonathanxd.codeapi.type.PlainCodeType;
import com.github.jonathanxd.codeapi.util.CodeTypes;
import com.github.jonathanxd.codeapi.util.source.CodeArgumentUtil;
import com.github.jonathanxd.iutils.collection.CollectionUtils;
import com.github.jonathanxd.iutils.object.Pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.text.StringsKt;

/**
 * Uses {@code CodeAPI} to generate {@code Builder} classes. {@link Bytecode} generates an bytecode
 * classes and {@link Source} generate source classes. Because of modular nature of {@code CodeAPI},
 * you only need dependency on the {@code CodeAPI-SourceWriter} when using {@code BuilderGenerator}
 * as annotation processor.
 *
 * Runtime builder generator is not available yet, but it will only require dependency on the {@code
 * CodeAPI-BytecodeWriter}. Do not reference {@link Source Source class} if the class path do not
 * contains {@code CodeAPI-SourceWriter} module and do not reference {@link Bytecode Bytecode class}
 * if the class path do not contains {@code CodeAPI-BytecodeWriter} module.
 */
public final class CodeAPIBuilderGenerator {

    private CodeAPIBuilderGenerator() {
        throw new IllegalStateException();
    }

    static TypeDeclaration generate(BuilderSpec builderSpec) {

        String builderName = builderSpec.getBuilderQualifiedName();
        CodeType baseClass = builderSpec.getBaseClass();
        CodeType builderBaseClass = builderSpec.getBuilderBaseClass();
        List<PropertySpec> properties = builderSpec.getProperties();

        List<ExtendedProperty> extendedProperties = properties.stream()
                .map(propertySpec -> new ExtendedProperty(propertySpec, MethodResolver.resolveValidator(propertySpec).orElse(null), MethodResolver.resolveDefaultMethod(propertySpec).orElse(null)))
                .collect(Collectors.toList());

        // Refers to 'classDeclaration' type. That is undefined yet.
        CodeType ref = new PlainCodeType(builderName, false);

        Generic builderBaseGeneric = Generic.type(builderBaseClass).of(baseClass, ref);

        MutableCodeSource body = new MutableCodeSource();

        ClassDeclaration classDeclaration = ClassDeclarationBuilder.builder()
                .withModifiers(CodeModifier.PUBLIC, CodeModifier.FINAL)
                .withQualifiedName(builderName)
                .withSuperClass(Types.OBJECT)
                .withImplementations(builderBaseGeneric)
                .withBody(body)
                .build();


        CodeAPIBuilderGenerator.addPropertiesFields(extendedProperties, body);
        CodeAPIBuilderGenerator.addConstructors(extendedProperties, baseClass, body);
        CodeAPIBuilderGenerator.addWithMethods(extendedProperties, classDeclaration, body);
        CodeAPIBuilderGenerator.addGetterMethods(extendedProperties, body);
        CodeAPIBuilderGenerator.addDefMethod(builderBaseGeneric, body, builderSpec);
        CodeAPIBuilderGenerator.addBuildMethod(extendedProperties, baseClass, body, builderSpec);

        return classDeclaration;
    }

    private static void addPropertiesFields(List<ExtendedProperty> properties, MutableCodeSource mutableCodeSource) {

        for (ExtendedProperty property : properties) {

            CodePart propertyDefaultValue = CodeAPIBuilderGenerator.getPropertyDefaultValue(property.propertySpec, property.defaultValue);

            FieldDeclarationBuilder valueBuilder = FieldDeclarationBuilder.builder()
                    .withModifiers(CodeModifier.PRIVATE)
                    .withType(property.propertySpec.getType())
                    .withName(property.propertySpec.getName());


            if (propertyDefaultValue != null)
                valueBuilder.withValue(propertyDefaultValue);


            FieldDeclaration value = valueBuilder.build();

            mutableCodeSource.add(value);
        }

    }

    private static void addConstructors(List<ExtendedProperty> properties, CodeType baseType, MutableCodeSource mutableCodeSource) {
        mutableCodeSource.add(ConstructorFactory.constructor(EnumSet.of(CodeModifier.PUBLIC), CodeSource.empty()));

        MutableCodeSource constructorSource = new MutableCodeSource();


        MutableCodeSource body = new MutableCodeSource();

        VariableBase base = CodeAPI.accessLocalVariable(baseType, "defaults");

        constructorSource.add(CodeAPI.ifStatement(CodeAPI.checkNotNull(base), body));

        for (ExtendedProperty property : properties) {

            PropertySpec propertySpec = property.propertySpec;
            String defaultsPropertyName = propertySpec.getDefaultsPropertyName();
            String name = propertySpec.getName();
            CodeType type = propertySpec.getType();

            CodePart getterInvoke = CodeAPI.invoke(InvokeType.get(baseType), baseType, base, "get" + StringsKt.capitalize(defaultsPropertyName), new TypeSpec(type, Collections.emptyList()), Collections.emptyList());

            if (propertySpec.isOptional()) {
                getterInvoke = CodeAPI.invokeVirtual(Optional.class, getterInvoke, "orElse", CodeAPI.typeSpec(Object.class, Object.class), Collections.singletonList(Literals.NULL));
            }

            body.add(CodeAPI.setThisField(type, name, getterInvoke));
        }

        mutableCodeSource.add(ConstructorFactory.constructor(EnumSet.of(CodeModifier.PUBLIC), new CodeParameter[]{
                CodeAPI.parameter(base.getType(), base.getName())
        }, constructorSource));
    }

    private static void addWithMethods(List<ExtendedProperty> properties, CodeType currentType, MutableCodeSource mutableCodeSource) {
        for (ExtendedProperty property : properties) {
            PropertySpec propertySpec = property.propertySpec;
            String name = propertySpec.getName();
            CodeType type = propertySpec.getType();

            MutableCodeSource body = new MutableCodeSource();

            addPropertyVerification(property.propertySpec, CodeAPI.accessLocalVariable(type, name), property.validator, body);

            body.add(CodeAPI.setThisField(type, name, CodeAPI.accessLocalVariable(type, name)));
            body.add(CodeAPI.returnValue(currentType, CodeAPI.accessThis()));

            mutableCodeSource.add(
                    MethodDeclarationBuilder.builder()
                            .withModifiers(CodeModifier.PUBLIC)
                            .withReturnType(currentType)
                            .withName("with" + StringsKt.capitalize(name))
                            .withParameters(CodeAPI.parameter(type, name))
                            .withBody(body)
                            .withBody(body)
                            .build()
            );
        }
    }

    private static void addGetterMethods(List<ExtendedProperty> properties, MutableCodeSource mutableCodeSource) {
        for (ExtendedProperty property : properties) {
            PropertySpec propertySpec = property.propertySpec;
            String name = propertySpec.getName();
            CodeType type = propertySpec.getType();

            if (propertySpec.isOptional())
                type = Generic.type(CodeAPI.getJavaType(Optional.class)).of(type);

            mutableCodeSource.add(
                    MethodDeclarationBuilder.builder()
                            .withModifiers(CodeModifier.PUBLIC)
                            .withReturnType(type)
                            .withName("get" + StringsKt.capitalize(name))
                            .withBody(CodeAPI.source(
                                    !propertySpec.isOptional()
                                            ? CodeAPI.returnValue(type, CodeAPI.accessThisField(type, name))
                                            : CodeAPI.returnValue(type, CodeAPI.invokeStatic(Optional.class, "ofNullable", CodeAPI.typeSpec(Optional.class, Object.class), Collections.singletonList(CodeAPI.accessThisField(type, name))))
                            ))
                            .build()
            );
        }
    }

    private static void addDefMethod(GenericType implementationType, MutableCodeSource mutableCodeSource, BuilderSpec builderSpec) {

        CodeType builderBaseClass = builderSpec.getBuilderBaseClass();
        Function<CodeType, String> getName = codeType -> codeType instanceof GenericType && !((GenericType) codeType).isType() ? ((GenericType) codeType).getName() : null;

        Function<CodeType, CodeType> fixer = CTypeUtil::resolve;

        if (builderBaseClass instanceof GenericType) {
            GenericType genericType = (GenericType) builderBaseClass;
            GenericType.Bound[] bounds = genericType.getBounds();

            if (bounds.length == 2) {
                String fName = getName.apply(bounds[0].getType());
                String sName = getName.apply(bounds[1].getType());

                fixer = codeType -> {

                    if (fName != null)
                        codeType = CodeTypes.applyType(codeType, fName, CTypeUtil.resolve(implementationType.getBounds()[0].getType()));

                    if (sName != null)
                        codeType = CodeTypes.applyType(codeType, sName, CTypeUtil.resolve(implementationType.getBounds()[1].getType()));

                    return CTypeUtil.resolve(codeType);
                };
            }

        }

        for (MethodSpec methodSpec : builderSpec.getMethodSpecs()) {
            Optional<MethodRefSpec> defaultMethodOpt = methodSpec.getDefaultMethod();

            if (defaultMethodOpt.isPresent()) {

                MethodRefSpec methodRefSpec = defaultMethodOpt.get();
                Optional<InlineMethodInvoker> method = MethodResolver.resolve(() -> defaultMethodOpt);

                MethodDeclaration targetMethod = methodSpec.getTargetMethod();

                /////////////////////////////////////////////////////////////////////////////////////////////////////////
                CodeType returnType = fixer.apply(targetMethod.getReturnType());

                List<CodeParameter> parameterList = new ArrayList<>();

                for (CodeParameter codeParameter : targetMethod.getParameters()) {
                    parameterList.add(codeParameter.builder().withType(fixer.apply(codeParameter.getType())).build());
                }

                List<CodePart> arguments = CodeArgumentUtil.argumentsFromParameters(targetMethod.getParameters());

                arguments.add(0, CodeAPI.accessThis());

                /////////////////////////////////////////////////////////////////////////////////////////////////////////

                CodeSource body;

                if(method.isPresent()) {
                    body = CodeSource.fromPart(method.get().apply(new Object[]{CodeAPI.accessThis(), arguments}));
                } else {
                    MethodTypeSpec methodTypeSpec = methodRefSpec.getMethodTypeSpec();
                    body = CodeSource.fromPart(
                            CodeAPI.returnValue(returnType, CodeAPI.cast(methodTypeSpec.getTypeSpec().getReturnType(), returnType, MethodInvocationUtil.toInvocation(methodTypeSpec, arguments))));
                }

                mutableCodeSource.add(targetMethod.builder()
                        .withReturnType(returnType)
                        .withParameters(parameterList)
                        .withBody(body)
                        .build());
            }
        }

    }

    private static void addBuildMethod(List<ExtendedProperty> properties, CodeType baseType, MutableCodeSource mutableCodeSource, BuilderSpec builderSpec) {


        MutableCodeSource source = new MutableCodeSource();

        for (ExtendedProperty property : properties) {
            FieldAccess fieldAccess = CodeAPI.accessThisField(property.propertySpec.getType(), property.propertySpec.getName());

            addPropertyVerification(property.propertySpec, fieldAccess, property.validator, source);
        }

        List<CodeType> argumentsTypes = properties.stream()
                .map(extendedProperty -> extendedProperty.propertySpec.getType())
                .collect(Collectors.toList());

        List<CodePart> arguments = properties.stream()
                .map(extendedProperty -> CodeAPI.accessThisField(extendedProperty.propertySpec.getType(), extendedProperty.propertySpec.getName()))
                .collect(Collectors.toList());

        source.add(CodeAPI.returnValue(builderSpec.getFactoryResultType(), MethodInvocationUtil.createFactoryInvocation(builderSpec, argumentsTypes, arguments)));

        mutableCodeSource.add(MethodDeclarationBuilder.builder()
                .withModifiers(CodeModifier.PUBLIC)
                .withName("build")
                .withReturnType(baseType)
                .withBody(source)
                .build());
    }

    private static void addPropertyVerification(PropertySpec property, CodePart codePart, InlineMethodInvoker invoker, MutableCodeSource mutableCodeSource) {

        if (!property.isNullable() && !property.getType().isPrimitive()) {

            mutableCodeSource.add(CodeAPI.invokeStatic(
                    Objects.class,
                    "requireNonNull",
                    CodeAPI.voidTypeSpec(Object.class, String.class),
                    CollectionUtils.listOf(codePart, Literals.STRING("The property '" + property.getName() + "' cannot be null."))
            ));
        } else if (property.isOptional()) {
            codePart = CodeAPI.invokeStatic(Optional.class, "ofNullable", CodeAPI.typeSpec(Optional.class, Object.class), Collections.singletonList(codePart));
        }


        Optional<MethodRefSpec> validatorSpecOpt = property.getValidatorSpec();

        if (validatorSpecOpt.isPresent()) {
            if (invoker != null) {
                CodePart apply = invoker.apply(new Object[]{new VariableRef(property.getType(), property.getName()), codePart});
                mutableCodeSource.add(apply);
            } else {
                mutableCodeSource.add(MethodInvocationUtil.validationToInvocation(validatorSpecOpt.get().getMethodTypeSpec(), codePart, property));
            }
        }
    }

    private static CodePart getPropertyDefaultValue(PropertySpec property, InlineMethodInvoker invoker) {

        Optional<MethodRefSpec> defaultValueSpec = property.getDefaultValueSpec();

        CodeType type = property.getType();

        if (defaultValueSpec.isPresent()) {
            if (invoker != null) {
                return invoker.apply(new Object[]{new VariableRef(type, property.getName())});
            } else {
                return MethodInvocationUtil.defaultValueToInvocation(defaultValueSpec.get().getMethodTypeSpec(), property);
            }
        } else {
            if (type.isPrimitive()) {
                if (type.is(Types.BOOLEAN))
                    return Literals.FALSE;
                if (type.is(Types.BYTE))
                    return Literals.BYTE((byte) 0);
                if (type.is(Types.SHORT))
                    return Literals.SHORT((short) 0);
                if (type.is(Types.CHAR))
                    return Literals.CHAR((char) 0);
                if (type.is(Types.INT))
                    return Literals.INT(0);
                if (type.is(Types.FLOAT))
                    return Literals.FLOAT(0F);
                if (type.is(Types.DOUBLE))
                    return Literals.DOUBLE(0D);
                if (type.is(Types.LONG))
                    return Literals.LONG(0L);

                throw new IllegalArgumentException("Illegal property type: '" + type + "'!");
            } else {
                return Literals.NULL;
            }

        }
    }

    static final class ExtendedProperty {
        final PropertySpec propertySpec;
        final InlineMethodInvoker validator;
        final InlineMethodInvoker defaultValue;

        ExtendedProperty(PropertySpec propertySpec, InlineMethodInvoker validator, InlineMethodInvoker defaultValue) {
            this.propertySpec = propertySpec;
            this.validator = validator;
            this.defaultValue = defaultValue;
        }
    }

    public static class Bytecode implements BuilderGenerator<BytecodeClass[]> {

        @Override
        public BytecodeClass[] generate(BuilderSpec builderSpec) {

            CodePart part = CodeAPIBuilderGenerator.generate(builderSpec);

            BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

            bytecodeGenerator.getOptions().set(BytecodeOptions.VISIT_LINES, VisitLineType.FOLLOW_CODE_SOURCE);

            // Should we enable this? Compilation slowdown? hmmm, it is not good
            bytecodeGenerator.getOptions().set(BytecodeOptions.GENERATE_BRIDGE_METHODS, Boolean.TRUE);

            return bytecodeGenerator.gen(part);
        }
    }

    public static class Source implements BuilderGenerator<Pair<TypeDeclaration, String>> {

        @Override
        public Pair<TypeDeclaration, String> generate(BuilderSpec builderSpec) {

            TypeDeclaration part = CodeAPIBuilderGenerator.generate(builderSpec);

            PlainSourceGenerator sourceGenerator = new PlainSourceGenerator();

            return Pair.of(part, sourceGenerator.gen(part));

        }

    }


}
