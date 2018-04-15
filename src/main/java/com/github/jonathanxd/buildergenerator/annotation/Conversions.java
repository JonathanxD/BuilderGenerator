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
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.condition.Conditions;
import com.github.jonathanxd.iutils.object.Default;
import com.github.jonathanxd.iutils.opt.OptObject;
import com.github.jonathanxd.kores.base.Annotation;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.KoresType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;

public final class Conversions {

    private Conversions() {
        throw new IllegalStateException();
    }

    /**
     * Convert {@link MethodRef} annotation to {@link MethodTypeSpec}.
     *
     * @param methodRef Method reference to convert to {@link MethodTypeSpec}
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@code methodRef} {@link DefaultUtil#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(MethodRef methodRef) {
        if (methodRef == null || DefaultUtil.isDefault(methodRef))
            return Optional.empty();

        return Optional.of(new MethodTypeSpec(methodRef.value(), methodRef.name(),
                new TypeSpec(methodRef.returnType(),
                        Collections3.listOf(methodRef.parameterTypes()))
        ));
    }

    /**
     * Convert {@link MethodRef} annotation to {@link MethodTypeSpec}.
     *
     * @param methodRef Method reference to convert to {@link MethodTypeSpec}
     * @param rtype     Inferred return type.
     * @param ptypes    Inferred parameter types.
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@code methodRef} {@link DefaultUtil#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(MethodRef methodRef, Class<?> rtype,
                                                        Class<?>[] ptypes) {

        if (methodRef == null || DefaultUtil.isDefault(methodRef))
            return Optional.empty();

        if (methodRef.parameterTypes().length > 0)
            Conditions.require(ptypes.length == methodRef.parameterTypes().length,
                    "'methodRef.parameterTypes().length' must be equal to 'ptypes.length'!");

        return Optional
                .of(new MethodTypeSpec(methodRef.value(), methodRef.name(),
                        new TypeSpec(
                                Conversions.typeOr(methodRef.returnType(), rtype),
                                ptypes.length > 0 && methodRef.parameterTypes().length == 0
                                        ? Collections3.listOf(ptypes)
                                        : ArraysKt.mapIndexed(methodRef.parameterTypes(),
                                        (integer, aClass) -> typeOr(aClass, ptypes[integer]))
                        )
                ));
    }

    /**
     * Convert {@link Validator} instance to {@link MethodTypeSpec}.
     *
     * @param validator Validator instance to convert to {@link MethodTypeSpec}.
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@code validator} {@link DefaultUtil#isDefault(Validator)} or the {@link
     * Validator#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(Validator validator) {
        if (validator == null || DefaultUtil.isDefault(validator))
            return Optional.empty();

        return Conversions.toMethodSpec(validator.value());
    }

    /**
     * Convert {@link Validator} instance to {@link MethodTypeSpec}.
     *
     * @param validator Validator instance to convert to {@link MethodTypeSpec}.
     * @param rtype     Inferred return type.
     * @param ptypes    Inferred parameter types.
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@code validator} {@link DefaultUtil#isDefault(Validator)} or the {@link
     * Validator#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(Validator validator, Class<?> rtype,
                                                        Class<?>[] ptypes) {
        if (validator == null || DefaultUtil.isDefault(validator))
            return Optional.empty();

        return Conversions.toMethodSpec(validator.value(), rtype, ptypes);
    }

    /**
     * Convert {@link DefaultImpl} instance to {@link MethodTypeSpec}.
     *
     * @param defaultImpl DefaultImpl instance to convert to {@link MethodTypeSpec}.
     * @param rtype       Inferred return type.
     * @param ptypes      Inferred parameter types.
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@link DefaultImpl#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(DefaultImpl defaultImpl, Class<?> rtype,
                                                        Class<?>[] ptypes) {
        if (defaultImpl == null || DefaultUtil.isDefault(defaultImpl.value()))
            return Optional.empty();

        return Conversions.toMethodSpec(defaultImpl.value(), rtype, ptypes);
    }

    /**
     * If the {@code type} is {@link Default} returns {@code alternative}, if not returns {@code
     * type}.
     *
     * @param type        Type.
     * @param alternative Alternative type.
     * @return {@code alternative} if the {@code type} is {@link Default}, if not returns {@code
     * type}.
     */
    private static Type typeOr(Class<?> type, Class<?> alternative) {
        return DefaultUtil.isDefaultType(type) ? type : alternative;
    }

    /**
     * Kores version of methods.
     */
    public static class KRS {

        /**
         * Convert {@link Validator} {@link Annotation} instance to {@link MethodTypeSpec}.
         *
         * This method is same as {@link #toMethodSpec(Validator)} but this method reads values from
         * {@link Annotation CodeAPI Annotation}.
         *
         * @param validator Validator instance to convert to {@link MethodTypeSpec}.
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code validator} {@link DefaultUtil#isDefault(Validator)} or the {@link
         * Validator#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
         */
        public static Optional<MethodTypeSpec> validatorToMethodSpec(UnifiedValidator validator) {
            if (validator == null || DefaultUtil.isDefaultValidator(validator))
                return Optional.empty();

            return KRS.toMethodSpec(validator.value());
        }

        /**
         * Convert {@link Validator} {@link Annotation} instance to {@link MethodTypeSpec}.
         *
         * This method is same as {@link #toMethodSpec(Validator, Class, Class[])} but this method
         * reads values from {@link Annotation CodeAPI Annotation}.
         *
         * @param validator Validator instance to convert to {@link MethodTypeSpec}.
         * @param rtype     Inferred return type.
         * @param ptypes    Inferred parameter types.
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code validator} {@link DefaultUtil#isDefault(Validator)} or the {@link
         * Validator#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
         */
        public static Optional<MethodTypeSpec> validatorToMethodSpec(UnifiedValidator validator,
                                                                     KoresType rtype,
                                                                     KoresType[] ptypes) {
            if (validator == null || DefaultUtil.isDefaultValidator(validator))
                return Optional.empty();

            return KRS.toMethodSpec(validator.value(), rtype, ptypes);
        }

        /**
         * This method is identical to {@link #toMethodSpec(MethodRef)} but this method reads values
         * from {@link Annotation CodeAPI Annotation}.
         *
         * @param methodRef Method reference to convert to {@link MethodTypeSpec}
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code methodRef} {@link DefaultUtil#isDefault(MethodRef)}.
         * @see #toMethodSpec(MethodRef)
         */
        @SuppressWarnings("unchecked")
        public static Optional<MethodTypeSpec> toMethodSpec(UnifiedMethodRef methodRef) {
            if (methodRef == null || DefaultUtil.isDefaultMethodRef(methodRef))
                return Optional.empty();

            Type value = methodRef.value();
            String name = methodRef.name();
            Type returnType = methodRef.returnType().orElse(Void.TYPE);

            List<? extends Type> parameterTypes = methodRef.parameterTypes()
                    .orElse(Collections.emptyList());

            return Optional
                    .of(new MethodTypeSpec(value, name, new TypeSpec(returnType, parameterTypes)));
        }

        /**
         * This method is identical to {@link #toMethodSpec(MethodRef, Class, Class[])} but this
         * method reads values from {@link Annotation CodeAPI Annotation}.
         *
         * @param rtype  Inferred return type.
         * @param ptypes Inferred parameter types.
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code validator} {@link DefaultUtil#isDefault(Validator)} or the {@link
         * Validator#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
         * @see #toMethodSpec(MethodRef, Class, Class[])
         */
        @SuppressWarnings("unchecked")
        public static Optional<MethodTypeSpec> toMethodSpec(UnifiedMethodRef methodRef,
                                                            Type rtype, Type[] ptypes) {

            if (methodRef == null || DefaultUtil.isDefaultMethodRef(methodRef))
                return Optional.empty();

            Type value = methodRef.value();
            String name = methodRef.name();
            Type returnType = methodRef.returnType().orElse(rtype);

            List<? extends Type> parameterTypes = methodRef.parameterTypes()
                    .or(() -> ptypes == null ? OptObject.none() : OptObject
                            .some(Arrays.asList(ptypes)))
                    .orElseFailStupidly(() -> new IllegalArgumentException(
                            "'parameterTypes' property not set!"));

            if (parameterTypes.size() > 0) {
                if (ptypes != null)
                    Conditions.require(ptypes.length == parameterTypes.size(),
                            "'methodRef.parameterTypes().length' must be equal to 'ptypes.length'!");
            }

            return Optional.of(new MethodTypeSpec(value, name,
                    new TypeSpec(
                            KRS.typeOr(returnType, rtype),
                            ptypes != null && ptypes.length > 0 && parameterTypes.size() == 0
                                    ? Arrays.asList(ptypes)
                                    : CollectionsKt.mapIndexed(parameterTypes,
                                    (integer, aClass) -> typeOr(aClass,
                                            ptypes == null ? null : ptypes[integer]))
                    )
            ));
        }

        /**
         * This method is identical to {@link #toMethodSpec(DefaultImpl, Class, Class[])} but this
         * method reads values from {@link Annotation CodeAPI Annotation}.
         *
         * @param rtype  Inferred return type.
         * @param ptypes Inferred parameter types.
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@link DefaultImpl#value()} is {@link DefaultUtil#isDefault(MethodRef)}.
         * @see #toMethodSpec(DefaultImpl, Class, Class[])
         */
        @SuppressWarnings("unchecked")
        public static Optional<MethodTypeSpec> defaultImplToMethodSpec(
                UnifiedDefaultImpl defaultImpl, KoresType rtype, KoresType[] ptypes) {

            if (defaultImpl == null || DefaultUtil.isDefaultDefaultImpl(defaultImpl))
                return Optional.empty();

            return KRS.toMethodSpec(defaultImpl.value(), rtype, ptypes);
        }

        /**
         * If the {@code type} is {@link DefaultUtil} returns {@code alternative}, if not returns
         * {@code type}.
         *
         * @param type        Type.
         * @param alternative Alternative type.
         * @return {@code alternative} if the {@code type} is {@link DefaultUtil}, if not returns
         * {@code type}.
         */
        private static Type typeOr(Type type, Type alternative) {
            return (!ImplicitKoresType.is(type, Default.class)
                    ? type
                    : Objects.requireNonNull(alternative));
        }
    }
}
