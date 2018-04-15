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
package com.github.jonathanxd.buildergenerator.util;

import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.spec.MethodRefSpec;
import com.github.jonathanxd.buildergenerator.spec.MethodSpec;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.iutils.type.TypeUtil;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.KoresTypes;
import com.github.jonathanxd.kores.type.LoadedKoresType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import kotlin.collections.ArraysKt;

/**
 * Resolves {@link Inline Inlinable method}.
 */
public final class MethodResolver {

    private MethodResolver() {
        throw new IllegalStateException();
    }

    /**
     * Resolve the {@link PropertyInfo#validator()} method.
     *
     * @param propertySpec Property specification.
     * @return {@link Optional} of the validator invoker, or an empty {@link Optional} if the method cannot be found.
     */
    public static Optional<InlineMethodInvoker> resolveValidator(PropertySpec propertySpec) {
        return MethodResolver.resolve(propertySpec::getValidatorSpec);
    }

    /**
     * Resolve the {@link PropertyInfo#defaultValue()} method.
     *
     * @param propertySpec Property specification.
     * @return {@link Optional} of the default value invoker, or an empty {@link Optional} if the method cannot be found.
     */
    public static Optional<InlineMethodInvoker> resolveDefaultMethod(PropertySpec propertySpec) {
        return MethodResolver.resolve(propertySpec::getDefaultValueSpec);
    }

    /**
     * Resolve the {@link com.github.jonathanxd.buildergenerator.annotation.DefaultImpl#value()} method.
     *
     * @param methodSpec Method specification.
     * @return {@link Optional} of the default implementation invoker, or an empty {@link Optional} if the method cannot be found.
     */
    public static Optional<InlineMethodInvoker> resolveDefaultImpl(MethodSpec methodSpec) {
        return MethodResolver.resolve(methodSpec::getDefaultMethod);
    }


    /**
     * Resolve the {@link Inline inlinable} and returns the invoker of the method.
     *
     * @param supplier Provider of {@link MethodRefSpec inlinable method specification}.
     * @return {@link Optional} of the inline method invoker, or an empty {@link Optional} if the method cannot be found.
     */
    public static Optional<InlineMethodInvoker> resolve(Supplier<Optional<MethodRefSpec>> supplier) {
        Optional<MethodRefSpec> validatorSpec = supplier.get();

        if (!validatorSpec.isPresent())
            return Optional.empty();

        Optional<Method> resolve = resolve(validatorSpec.get());

        return resolve.map(method -> (args) -> {
            try {
                return (Instruction) method.invoke(null, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }


    /**
     * Resolves the {@link Method} instance from {@link MethodTypeSpec}.
     *
     * @param methodRefSpec Method specification.
     * @return {@link Optional} of the found {@link Method}, or empty {@link Optional} if method cannot be found.
     */
    public static Optional<Method> resolve(MethodRefSpec methodRefSpec) {

        if (!methodRefSpec.isInline())
            return Optional.empty();

        MethodTypeSpec methodTypeSpec = methodRefSpec.getMethodTypeSpec();

        Class<?> localization = MethodResolver.getClassOrNull(methodTypeSpec.getLocalization());
        String methodName = methodTypeSpec.getMethodName();
        Class<?> returnType = MethodResolver.getClassOrNull(methodTypeSpec.getTypeSpec().getReturnType());
        Class<?>[] parameterTypes = methodTypeSpec.getTypeSpec().getParameterTypes().stream().map(
                MethodResolver::getClassOrNull).toArray(Class[]::new);

        if (localization == null || returnType == null || ArraysKt.any(parameterTypes, Objects::isNull))
            throw new IllegalArgumentException(
                    "Failed to find runtime types of inline method! MethodSpec: '" + methodTypeSpec.toMethodString() + "'!");

        try {
            Method m = localization.getDeclaredMethod(methodName, parameterTypes);

            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers()))
                throw new IllegalArgumentException("Provided method must be public and static!");

            return Optional.of(m);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to find inline method! MethodSpec: '" + methodTypeSpec.toMethodString() + "'!", e);
        }

    }

    /**
     * Gets {@link Class} instance from {@link KoresType}.
     *
     * @param koresType Type to extract {@link Class} instance.
     * @return {@link Class} instance if the {@link KoresType} is an {@link LoadedKoresType}, null otherwise.
     */
    private static Class<?> getClassOrNull(Type koresType) {

        koresType = KoresTypes.getConcreteType(KoresTypes.getKoresType(koresType));

        if (!(koresType instanceof LoadedKoresType<?>)) {
            try {
                return TypeUtil.resolveClass(ImplicitKoresType.getCanonicalName(koresType));
            } catch (Throwable ignored) {
                return null;
            }
        }

        return ((LoadedKoresType<?>) koresType).getLoadedType();
    }

}
