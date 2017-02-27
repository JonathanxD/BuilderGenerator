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
package com.github.jonathanxd.buildergenerator.util;

import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.LoadedCodeType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import kotlin.collections.ArraysKt;

public final class MethodResolver {

    private MethodResolver() {
        throw new IllegalStateException();
    }


    public static Optional<InlineMethodInvoker> resolveValidator(PropertySpec propertySpec) {
        return resolve(propertySpec, propertySpec::getValidatorSpec);
    }

    public static Optional<InlineMethodInvoker> resolveDefaultMethod(PropertySpec propertySpec) {
        return resolve(propertySpec, propertySpec::getDefaultValueSpec);
    }


    public static Optional<InlineMethodInvoker> resolve(PropertySpec propertySpec, Supplier<Optional<MethodTypeSpec>> consumer) {
        Optional<MethodTypeSpec> validatorSpec = consumer.get();

        if (!validatorSpec.isPresent())
            return Optional.empty();

        Optional<Method> resolve = resolve(validatorSpec.get());

        if (!resolve.isPresent())
            return Optional.empty();

        return Optional.of((args) -> {
            try {
                return (CodePart) resolve.get().invoke(null, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }


    public static Optional<Method> resolve(MethodTypeSpec methodTypeSpec) {

        Class<?> localization = MethodResolver.getClassOrNull(methodTypeSpec.getLocalization());
        String methodName = methodTypeSpec.getMethodName();
        Class<?> returnType = MethodResolver.getClassOrNull(methodTypeSpec.getTypeSpec().getReturnType());
        Class<?>[] parameterTypes = methodTypeSpec.getTypeSpec().getParameterTypes().stream().map(MethodResolver::getClassOrNull).toArray(Class[]::new);

        if(localization == null || returnType == null || ArraysKt.any(parameterTypes, Objects::isNull))
            return Optional.empty();

        try {
            Method m = localization.getDeclaredMethod(methodName, parameterTypes);

            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers()))
                throw new IllegalArgumentException("Provided method must be public and static!");

            if (m.isAnnotationPresent(Inline.class)) {
                return Optional.of(m);
            }
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }

        return Optional.empty();

    }

    private static Class<?> getClassOrNull(CodeType codeType) {
        if (!(codeType instanceof LoadedCodeType<?>))
            return null;

        return ((LoadedCodeType<?>) codeType).getLoadedType();
    }

    private static Class<?> getClass(CodeType codeType) {
        if (!(codeType instanceof LoadedCodeType<?>))
            throw new IllegalArgumentException("Type '" + codeType + "' is not a runtime type.");

        return ((LoadedCodeType<?>) codeType).getLoadedType();
    }

}
