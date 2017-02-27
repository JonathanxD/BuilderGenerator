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
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
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
import com.github.jonathanxd.codeapi.base.MethodInvocation;
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
import com.github.jonathanxd.codeapi.type.PlainCodeType;
import com.github.jonathanxd.iutils.collection.CollectionUtils;
import com.github.jonathanxd.iutils.object.Pair;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
            String name = propertySpec.getName();
            CodeType type = propertySpec.getType();

            MethodInvocation getterInvoke = CodeAPI.invoke(InvokeType.get(baseType), baseType, base, "get" + StringsKt.capitalize(name), new TypeSpec(type, Collections.emptyList()), Collections.emptyList());

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

            mutableCodeSource.add(
                    MethodDeclarationBuilder.builder()
                            .withModifiers(CodeModifier.PUBLIC)
                            .withReturnType(type)
                            .withName("get" + StringsKt.capitalize(name))
                            .withBody(CodeAPI.source(CodeAPI.returnValue(type, CodeAPI.accessThisField(type, name))))
                            .build()
            );
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


        Optional<MethodTypeSpec> validatorSpecOpt = property.getValidatorSpec();

        if (validatorSpecOpt.isPresent()) {
            if (invoker != null) {
                CodePart apply = invoker.apply(new Object[]{new VariableRef(property.getType(), property.getName()), codePart});
                mutableCodeSource.add(apply);
            } else {
                mutableCodeSource.add(MethodInvocationUtil.validationToInvocation(validatorSpecOpt.get(), codePart, property));
            }
        }
    }

    private static CodePart getPropertyDefaultValue(PropertySpec property, InlineMethodInvoker invoker) {

        Optional<MethodTypeSpec> defaultValueSpec = property.getDefaultValueSpec();

        CodeType type = property.getType();

        if (defaultValueSpec.isPresent()) {
            if (invoker != null) {
                return invoker.apply(new Object[]{new VariableRef(type, property.getName())});
            } else {
                return MethodInvocationUtil.defaultValueToInvocation(defaultValueSpec.get(), property);
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

                if (type.getCanonicalName().equals("java.util.Optional"))
                    return CodeAPI.invokeStatic(Optional.class, "empty", CodeAPI.typeSpec(Optional.class), Collections.emptyList());

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
