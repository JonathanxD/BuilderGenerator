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
package com.github.jonathanxd.buildergenerator.annotation;

import com.github.jonathanxd.buildergenerator.unification.UnifiedDefaultImpl;
import com.github.jonathanxd.buildergenerator.unification.UnifiedMethodRef;
import com.github.jonathanxd.buildergenerator.unification.UnifiedValidator;
import com.github.jonathanxd.iutils.object.Default;
import com.github.jonathanxd.kores.base.Annotation;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.KoresTypes;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;

public final class DefaultUtil {
    private static final KoresType DEFAULT = KoresTypes.getKoresType(Default.class);

    private DefaultUtil() {
        throw new IllegalStateException();
    }


    /**
     * Returns true if {@code KoresType} is default.
     *
     * @param KoresType Type.
     * @return True if {@code KoresType} is default.
     */
    public static boolean isDefaultType(Type KoresType) {
        return ImplicitKoresType.is(KoresType, DefaultUtil.DEFAULT);
    }

    /**
     * Returns true if {@code validator} is default.
     *
     * @param validator Validator
     * @return True if {@code validator} is default.
     */
    public static boolean isDefault(Validator validator) {
        return ImplicitKoresType.is(validator.value().value(), Default.class);
    }

    /**
     * Returns true if {@code methodRef} is default.
     *
     * @param methodRef Method reference.
     * @return True if {@code methodRef} is default.
     */
    public static boolean isDefault(MethodRef methodRef) {
        return ImplicitKoresType.is(methodRef.value(), Default.class);
    }

    /**
     * Returns true if {@code methodRef} annotation is default.
     *
     * @param methodRef Method reference.
     * @return True if {@code methodRef} annotation is default.
     */
    public static boolean isDefaultMethodRef(Annotation methodRef) {
        return ((KoresType) methodRef.getValues().get("value")).isIdEq(Default.class);
    }

    /**
     * Returns true if {@code methodRef} annotation is default.
     *
     * @param methodRef Method reference.
     * @return True if {@code methodRef} annotation is default.
     */
    public static boolean isDefaultMethodRef(UnifiedMethodRef methodRef) {
        return ImplicitKoresType.is(methodRef.value(), Default.class);
    }

    /**
     * Returns true if {@code defaultImpl} annotation is default.
     *
     * @param defaultImpl Default impl annotation.
     * @return True if {@code defaultImpl} annotation is default.
     */
    public static boolean isDefaultDefaultImpl(Annotation defaultImpl) {
        return isDefaultMethodRef(((Annotation) defaultImpl.getValues().get("value")));
    }

    /**
     * Returns true if {@code defaultImpl} annotation is default.
     *
     * @param defaultImpl Default impl annotation.
     * @return True if {@code defaultImpl} annotation is default.
     */
    public static boolean isDefaultDefaultImpl(UnifiedDefaultImpl defaultImpl) {
        return isDefaultMethodRef(defaultImpl.value());
    }

    /**
     * Returns true if {@code validator} annotation is default.
     *
     * @param validator Validator
     * @return True if {@code validator} annotation is default.
     */
    public static boolean isDefaultValidator(Annotation validator) {
        return isDefaultMethodRef(((Annotation) validator.getValues().get("value")));
    }

    /**
     * Returns true if {@code validator} annotation is default.
     *
     * @param validator Validator
     * @return True if {@code validator} annotation is default.
     */
    public static boolean isDefaultValidator(UnifiedValidator validator) {
        return isDefaultMethodRef(validator.value());
    }

    public static Optional<UnifiedMethodRef> methodRefOptional(
            @NotNull UnifiedMethodRef unifiedMethodRef) {
        if (isDefaultMethodRef(unifiedMethodRef))
            return Optional.empty();

        return Optional.of(unifiedMethodRef);
    }

    public static Optional<String> stringOptional(@NotNull String s) {

        if (s.isEmpty())
            return Optional.empty();

        return Optional.of(s);
    }
}
