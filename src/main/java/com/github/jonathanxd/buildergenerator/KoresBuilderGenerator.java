/*
 *      BuilderGenerator - Builder implementation generator <https://github.com/JonathanxD/BuilderGenerator>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 JonathanxD
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
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.object.Pair;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.MutableInstructions;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.base.ConstructorDeclaration;
import com.github.jonathanxd.kores.base.FieldAccess;
import com.github.jonathanxd.kores.base.FieldDeclaration;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.TypeDeclaration;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.base.VariableAccess;
import com.github.jonathanxd.kores.bytecode.BytecodeClass;
import com.github.jonathanxd.kores.bytecode.BytecodeOptions;
import com.github.jonathanxd.kores.bytecode.VisitLineType;
import com.github.jonathanxd.kores.bytecode.processor.BytecodeGenerator;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.PartFactory;
import com.github.jonathanxd.kores.inspect.InstructionsInspect;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.source.process.PlainSourceGenerator;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.type.GenericType;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.KoresTypes;
import com.github.jonathanxd.kores.type.PlainKoresType;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.collections.CollectionsKt;
import kotlin.text.StringsKt;

/**
 * Uses {@code CodeAPI} to generate {@code Builder} classes. {@link Bytecode} generates an bytecode classes and {@link Source}
 * generate source classes. Because of modular nature of {@code CodeAPI}, you only need dependency on the {@code
 * CodeAPI-SourceWriter} when using {@code BuilderGenerator} as annotation processor.
 *
 * Runtime builder generator is not available yet, but it will only require dependency on the {@code CodeAPI-BytecodeWriter}. Do
 * not reference {@link Source Source class} if the class path do not contains {@code CodeAPI-SourceWriter} module and do not
 * reference {@link Bytecode Bytecode class} if the class path do not contains {@code CodeAPI-BytecodeWriter} module.
 */
public final class KoresBuilderGenerator {

    private KoresBuilderGenerator() {
        throw new IllegalStateException();
    }

    static TypeDeclaration generate(BuilderSpec builderSpec, Consumer<List<MethodTypeSpec>> verifier) {

        String builderName = builderSpec.getBuilderQualifiedName();
        KoresType baseClass = builderSpec.getBaseClass();
        KoresType builderBaseClass = builderSpec.getBuilderBaseClass();
        List<PropertySpec> properties = builderSpec.getProperties();

        List<ExtendedProperty> extendedProperties = properties.stream()
                .map(propertySpec -> new ExtendedProperty(propertySpec,
                        MethodResolver.resolveValidator(propertySpec).orElse(null),
                        MethodResolver.resolveDefaultMethod(propertySpec).orElse(null)))
                .collect(Collectors.toList());

        // Refers to 'classDeclaration' type. That is undefined yet.
        KoresType ref = new PlainKoresType(builderName, false);

        Generic builderBaseGeneric = Generic.type(builderBaseClass).of(baseClass, ref);

        MutableInstructions body = MutableInstructions.create();

        List<MethodDeclaration> methods = new ArrayList<>();

        ClassDeclaration classDeclaration = ClassDeclaration.Builder.builder()
                .modifiers(KoresModifier.PUBLIC, KoresModifier.FINAL)
                .qualifiedName(builderName)
                .superClass(Types.OBJECT)
                .implementations(builderBaseGeneric)
                .fields(KoresBuilderGenerator.getPropertiesFields(extendedProperties))
                .constructors(KoresBuilderGenerator.getConstructors(extendedProperties, baseClass))
                .methods(methods)
                .build();


        methods.addAll(
                Collections3.concat(
                        KoresBuilderGenerator.getWithMethods(extendedProperties, classDeclaration),
                        KoresBuilderGenerator.getDefMethod(builderBaseGeneric, builderSpec),
                        KoresBuilderGenerator.getGetterMethods(extendedProperties),
                        KoresBuilderGenerator.getBuildMethod(extendedProperties, baseClass, builderSpec)
                )

        );

        List<MethodTypeSpec> inspect = InstructionsInspect.builder(codePart -> codePart instanceof MethodDeclaration)
                .includeRoot(true)
                .mapTo(codePart -> {
                    MethodDeclaration methodDeclaration = (MethodDeclaration) codePart;
                    return new MethodTypeSpec(classDeclaration, methodDeclaration.getName(),
                            new TypeSpec(methodDeclaration.getReturnType(),
                                    methodDeclaration.getParameters().stream().map(KoresParameter::getType).collect(
                                            Collectors.toList())));
                })
                .inspect(body);

        verifier.accept(inspect);

        return classDeclaration;
    }

    private static List<FieldDeclaration> getPropertiesFields(List<ExtendedProperty> properties) {

        List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

        for (ExtendedProperty property : properties) {

            Instruction propertyDefaultValue = KoresBuilderGenerator.getPropertyDefaultValue(property.propertySpec,
                    property.defaultValue);

            FieldDeclaration.Builder valueBuilder = FieldDeclaration.Builder.builder()
                    .modifiers(KoresModifier.PRIVATE)
                    .type(property.propertySpec.getType())
                    .name(property.propertySpec.getName());


            if (propertyDefaultValue != null)
                valueBuilder.value(propertyDefaultValue);


            FieldDeclaration value = valueBuilder.build();

            fieldDeclarations.add(value);
        }

        return fieldDeclarations;
    }

    private static List<ConstructorDeclaration> getConstructors(List<ExtendedProperty> properties, KoresType baseType) {

        List<ConstructorDeclaration> constructorDeclarations = new ArrayList<>();

        constructorDeclarations.add(PartFactory.constructorDec().modifiers(KoresModifier.PUBLIC).build());

        MutableInstructions constructorSource = MutableInstructions.create();


        MutableInstructions body = MutableInstructions.create();

        VariableAccess base = Factories.accessVariable(baseType, "defaults");

        constructorSource.add(Factories.ifStatement(Factories.checkNotNull(base), body));

        for (ExtendedProperty property : properties) {

            PropertySpec propertySpec = property.propertySpec;
            String defaultsPropertyName = propertySpec.getDefaultsPropertyName();
            String name = propertySpec.getName();
            KoresType type = propertySpec.getType();

            Instruction getterInvoke = InvocationFactory.invoke(InvokeType.get(baseType), baseType, base,
                    "get" + StringsKt.capitalize(defaultsPropertyName), new TypeSpec(type, Collections.emptyList()),
                    Collections.emptyList());

            if (propertySpec.isOptional()) {
                getterInvoke = InvocationFactory.invokeVirtual(Optional.class, getterInvoke, "orElse",
                        Factories.typeSpec(Object.class, Object.class), Collections.singletonList(Literals.NULL));
            }

            body.add(Factories.setThisFieldValue(type, name, getterInvoke));
        }

        constructorDeclarations.add(
                PartFactory.constructorDec()
                        .modifiers(KoresModifier.PUBLIC)
                        .parameters(Factories.parameter(base.getType(), base.getName()))
                        .body(constructorSource)
                        .build()
        );

        return constructorDeclarations;
    }

    private static List<MethodDeclaration> getWithMethods(List<ExtendedProperty> properties, KoresType currentType) {
        return properties.stream()
                .map(property -> {
                    PropertySpec propertySpec = property.propertySpec;
                    String name = propertySpec.getName();
                    Type type = propertySpec.getType();

                    MutableInstructions body = MutableInstructions.create();

                    addPropertyVerification(property.propertySpec, Factories.accessVariable(type, name), property.validator,
                            body);

                    body.add(Factories.setThisFieldValue(type, name, Factories.accessVariable(type, name)));
                    body.add(Factories.returnValue(currentType, Factories.accessThis()));
                    // Good type, not better type, I know
                    Type goodType;

                    if (!ImplicitKoresType.is(type, propertySpec.getBuilderSetterType())) {
                        goodType = propertySpec.getBuilderSetterType();
                    } else {
                        goodType = type;
                    }


                    return MethodDeclaration.Builder.builder()
                            .modifiers(KoresModifier.PUBLIC)
                            .returnType(currentType)
                            .name("with" + StringsKt.capitalize(name))
                            .parameters(Factories.parameter(goodType, name))
                            .body(body)
                            .build();
                }).collect(Collectors.toList());
    }

    private static List<MethodDeclaration> getGetterMethods(List<ExtendedProperty> properties) {
        return properties.stream()
                .map(property -> {
                    PropertySpec propertySpec = property.propertySpec;
                    String name = propertySpec.getName();
                    Type type = propertySpec.getType();

                    // Good type, not better type, I know
                    Type goodType;

                    if (!ImplicitKoresType.is(type, propertySpec.getBuilderSetterType())) {
                        goodType = propertySpec.getBuilderSetterType();
                    } else {
                        goodType = type;
                    }

                    if (propertySpec.isOptional()) {
                        goodType = Generic.type(Optional.class).of(goodType);
                    }

                    return MethodDeclaration.Builder.builder()
                            .modifiers(KoresModifier.PUBLIC)
                            .returnType(goodType)
                            .name("get" + StringsKt.capitalize(name))
                            .body(Instructions.fromPart(
                                    !propertySpec.isOptional()
                                    ? Factories.returnValue(type, Factories.accessThisField(type, name))
                                    : Factories.returnValue(type, InvocationFactory.invokeStatic(Optional.class, "ofNullable",
                                            Factories.typeSpec(Optional.class, Object.class),
                                            Collections.singletonList(Factories.accessThisField(type, name))))
                            ))
                            .build();
                }).collect(Collectors.toList());
    }

    private static List<MethodDeclaration> getDefMethod(GenericType implementationType,
                                                        BuilderSpec builderSpec) {
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();

        Type builderBaseClass = builderSpec.getBuilderBaseClass();
        Function<Type, String> getName = KoresType -> KoresType instanceof GenericType && !((GenericType) KoresType).isType()
                                                      ? ((GenericType) KoresType).getName() : null;

        Function<Type, Type> fixer = CTypeUtil::resolve;

        if (builderBaseClass instanceof GenericType) {
            GenericType genericType = (GenericType) builderBaseClass;
            GenericType.Bound[] bounds = genericType.getBounds();

            if (bounds.length == 2) {
                String fName = getName.apply(bounds[0].getType());
                String sName = getName.apply(bounds[1].getType());

                fixer = type -> {
                    KoresType koresType = KoresTypes.getKoresType(type);

                    if (fName != null)
                        type = KoresTypes.applyType(koresType, fName,
                                KoresTypes.getKoresType(CTypeUtil.resolve(implementationType.getBounds()[0].getType())));

                    if (sName != null)
                        type = KoresTypes.applyType(koresType, sName,
                                KoresTypes.getKoresType(CTypeUtil.resolve(implementationType.getBounds()[1].getType())));

                    return CTypeUtil.resolve(type);
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
                Type returnType = fixer.apply(targetMethod.getReturnType());

                List<KoresParameter> parameterList = new ArrayList<>();

                for (KoresParameter KoresParameter : targetMethod.getParameters()) {
                    parameterList.add(KoresParameter.builder().type(fixer.apply(KoresParameter.getType())).build());
                }

                List<Instruction> arguments = ConversionsKt.getAccess(targetMethod.getParameters());

                /////////////////////////////////////////////////////////////////////////////////////////////////////////
                MethodTypeSpec methodTypeSpec = methodRefSpec.getMethodTypeSpec();
                List<Type> parameterTypes = methodTypeSpec.getTypeSpec().getParameterTypes();

                MutableInstructions body = MutableInstructions.create();

                if (methodRefSpec.isThis()) {
                    arguments = CollectionsKt.mapIndexed(arguments,
                            (integer, codePart) -> Factories.cast(parameterList.get(integer).getType(),
                                    parameterTypes.get(integer),
                                    codePart));
                }

                MethodDeclaration methodDeclaration = targetMethod.builder()
                        .returnType(returnType)
                        .parameters(parameterList)
                        .body(body)
                        .build();

                if (method.isPresent()) {
                    body.add(method.get().apply(new Object[]{targetMethod, arguments}));
                } else {
                    if (!methodRefSpec.isThis()) {
                        arguments.add(0, Factories.accessThis());
                    }

                    body.add(Factories.returnValue(returnType,
                            Factories.cast(methodTypeSpec.getTypeSpec().getReturnType(), returnType,
                                    MethodInvocationUtil.toInvocation(methodRefSpec.isThis(), methodTypeSpec, arguments))));
                }

                methodDeclarations.add(methodDeclaration);

            }
        }

        return methodDeclarations;
    }

    private static List<MethodDeclaration> getBuildMethod(List<ExtendedProperty> properties, KoresType baseType,
                                                          BuilderSpec builderSpec) {

        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        MutableInstructions source = MutableInstructions.create();

        for (ExtendedProperty property : properties) {
            FieldAccess fieldAccess = Factories.accessThisField(property.propertySpec.getType(), property.propertySpec.getName());

            addPropertyVerification(property.propertySpec, fieldAccess, property.validator, source);
        }

        List<KoresType> argumentsTypes = properties.stream()
                .map(extendedProperty -> extendedProperty.propertySpec.getType())
                .collect(Collectors.toList());

        List<Instruction> arguments = properties.stream()
                .map(extendedProperty -> Factories.accessThisField(extendedProperty.propertySpec.getType(),
                        extendedProperty.propertySpec.getName()))
                .collect(Collectors.toList());

        source.add(Factories.returnValue(builderSpec.getFactoryResultType(),
                MethodInvocationUtil.createFactoryInvocation(builderSpec, argumentsTypes, arguments)));

        methodDeclarations.add(MethodDeclaration.Builder.builder()
                .modifiers(KoresModifier.PUBLIC)
                .name("build")
                .returnType(baseType)
                .body(source)
                .build());

        return methodDeclarations;
    }

    private static void addPropertyVerification(PropertySpec property, Instruction codePart, InlineMethodInvoker invoker,
                                                MutableInstructions mutableInstructions) {

        if (!property.isNullable() && !property.getType().isPrimitive()) {

            mutableInstructions.add(InvocationFactory.invokeStatic(
                    Objects.class,
                    "requireNonNull",
                    Factories.voidTypeSpec(Object.class, String.class),
                    Collections3.listOf(codePart, Literals.STRING("The property '" + property.getName() + "' cannot be null."))
            ));
        } else if (property.isOptional()) {
            codePart = InvocationFactory.invokeStatic(Optional.class, "ofNullable",
                    Factories.typeSpec(Optional.class, Object.class),
                    Collections.singletonList(codePart));
        }


        Optional<MethodRefSpec> validatorSpecOpt = property.getValidatorSpec();

        if (validatorSpecOpt.isPresent()) {
            if (invoker != null) {
                Instruction apply = invoker.apply(
                        new Object[]{new VariableRef(property.getType(), property.getName()), codePart});
                mutableInstructions.add(apply);
            } else {
                mutableInstructions.add(
                        MethodInvocationUtil.validationToInvocation(false, validatorSpecOpt.get().getMethodTypeSpec(), codePart,
                                property));
            }
        }
    }

    private static Instruction getPropertyDefaultValue(PropertySpec property, InlineMethodInvoker invoker) {

        Optional<MethodRefSpec> defaultValueSpec = property.getDefaultValueSpec();

        KoresType type = property.getType();

        if (defaultValueSpec.isPresent()) {
            if (invoker != null) {
                return invoker.apply(new Object[]{new VariableRef(type, property.getName())});
            } else {
                return MethodInvocationUtil.defaultValueToInvocation(false, defaultValueSpec.get().getMethodTypeSpec(), property);
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

    public static class Bytecode implements BuilderGenerator<List<BytecodeClass>> {

        @Override
        public List<BytecodeClass> generate(BuilderSpec builderSpec, Consumer<List<MethodTypeSpec>> verifier) {

            TypeDeclaration part = KoresBuilderGenerator.generate(builderSpec, verifier);

            BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

            bytecodeGenerator.getOptions().set(BytecodeOptions.VISIT_LINES, VisitLineType.GEN_LINE_INSTRUCTION);

            // Should we enable this? Compilation slowdown? hmmm, it is not good
            // Wait, CodeAPI does not support Javax Model elements, then it will have no effect at the moment
            // bytecodeGenerator.getOptions().set(BytecodeOptions.GENERATE_BRIDGE_METHODS, Boolean.TRUE);

            return bytecodeGenerator.process(part);
        }
    }

    public static class Source implements BuilderGenerator<Pair<TypeDeclaration, String>> {

        @Override
        public Pair<TypeDeclaration, String> generate(BuilderSpec builderSpec, Consumer<List<MethodTypeSpec>> verifier) {

            TypeDeclaration part = KoresBuilderGenerator.generate(builderSpec, verifier);

            PlainSourceGenerator sourceGenerator = new PlainSourceGenerator();

            return Pair.of(part, sourceGenerator.process(part));

        }

    }


}
