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
package com.github.jonathanxd.buildergenerator.annotation;

import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.Annotation;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.common.TypeSpec;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.iutils.condition.Conditions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
     * the {@code methodRef} {@link Default#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(MethodRef methodRef) {
        if (methodRef == null || Default.isDefault(methodRef))
            return Optional.empty();

        return Optional.of(new MethodTypeSpec(CodeAPI.getJavaType(methodRef.value()), methodRef.name(),
                new TypeSpec(CodeAPI.getJavaType(methodRef.returnType()), CodeAPI.getJavaTypeList(methodRef.parameterTypes()))
        ));
    }

    /**
     * Convert {@link MethodRef} annotation to {@link MethodTypeSpec}.
     *
     * @param methodRef Method reference to convert to {@link MethodTypeSpec}
     * @param rtype     Inferred return type.
     * @param ptypes    Inferred parameter types.
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@code methodRef} {@link Default#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(MethodRef methodRef, Class<?> rtype, Class<?>[] ptypes) {

        if (methodRef == null || Default.isDefault(methodRef))
            return Optional.empty();

        if (methodRef.parameterTypes().length > 0)
            Conditions.require(ptypes.length == methodRef.parameterTypes().length, "'methodRef.parameterTypes().length' must be equal to 'ptypes.length'!");

        return Optional.of(new MethodTypeSpec(CodeAPI.getJavaType(methodRef.value()), methodRef.name(),
                new TypeSpec(
                        Conversions.typeOr(methodRef.returnType(), rtype),
                        ptypes.length > 0 && methodRef.parameterTypes().length == 0
                                ? CodeAPI.getJavaTypeList(ptypes)
                                : ArraysKt.mapIndexed(methodRef.parameterTypes(), (integer, aClass) -> typeOr(aClass, ptypes[integer]))
                )
        ));
    }

    /**
     * Convert {@link Validator} instance to {@link MethodTypeSpec}.
     *
     * @param validator Validator instance to convert to {@link MethodTypeSpec}.
     * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional} if
     * the {@code validator} {@link Default#isDefault(Validator)} or the {@link Validator#value()}
     * is {@link Default#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(Validator validator) {
        if (validator == null || Default.isDefault(validator))
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
     * the {@code validator} {@link Default#isDefault(Validator)} or the {@link Validator#value()}
     * is {@link Default#isDefault(MethodRef)}.
     */
    public static Optional<MethodTypeSpec> toMethodSpec(Validator validator, Class<?> rtype, Class<?>[] ptypes) {
        if (validator == null || Default.isDefault(validator))
            return Optional.empty();

        return Conversions.toMethodSpec(validator.value(), rtype, ptypes);
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
    private static CodeType typeOr(Class<?> type, Class<?> alternative) {
        return CodeAPI.getJavaType((Class<?>) (type != Default.class ? type : alternative));
    }

    /**
     * CodeAPI version of methods.
     */
    public static class CAPI {


        /**
         * Convert {@link Validator} {@link Annotation} instance to {@link MethodTypeSpec}.
         *
         * This method is same as {@link #toMethodSpec(Validator)} but this method reads values from
         * {@link Annotation CodeAPI Annotation}.
         *
         * @param validator Validator instance to convert to {@link MethodTypeSpec}.
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code validator} {@link Default#isDefault(Validator)} or the {@link
         * Validator#value()} is {@link Default#isDefault(MethodRef)}.
         */
        public static Optional<MethodTypeSpec> validatorToMethodSpec(Annotation validator) {
            if (validator == null || Default.isDefaultValidator(validator))
                return Optional.empty();

            return Conversions.CAPI.toMethodSpec((Annotation) validator.getValues().get("value"));
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
         * if the {@code validator} {@link Default#isDefault(Validator)} or the {@link
         * Validator#value()} is {@link Default#isDefault(MethodRef)}.
         */
        public static Optional<MethodTypeSpec> validatorToMethodSpec(Annotation validator, CodeType rtype, CodeType[] ptypes) {
            if (validator == null || Default.isDefaultValidator(validator))
                return Optional.empty();

            return Conversions.CAPI.toMethodSpec((Annotation) validator.getValues().get("value"), rtype, ptypes);
        }

        /**
         * This method is identical to {@link #toMethodSpec(MethodRef)} but this method reads values
         * from {@link Annotation CodeAPI Annotation}.
         *
         * @param methodRef Method reference to convert to {@link MethodTypeSpec}
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code methodRef} {@link Default#isDefault(MethodRef)}.
         * @see #toMethodSpec(MethodRef)
         */
        @SuppressWarnings("unchecked")
        public static Optional<MethodTypeSpec> toMethodSpec(Annotation methodRef) {
            if (methodRef == null || Default.isDefaultMethodRef(methodRef))
                return Optional.empty();

            CodeType value = ((CodeType) methodRef.getValues().get("value"));
            String name = (String) methodRef.getValues().get("name");
            CodeType returnType = notNull(((CodeType) methodRef.getValues().get("returnType")), Types.VOID);
            List<? extends CodeType> parameterTypes = notNull(((List<? extends CodeType>) methodRef.getValues().get("parameterTypes")), Collections.emptyList());

            return Optional.of(new MethodTypeSpec(value, name, new TypeSpec(returnType, parameterTypes)));
        }

        /**
         * This method is identical to {@link #toMethodSpec(MethodRef, Class, Class[])} but this
         * method reads values from {@link Annotation CodeAPI Annotation}.
         *
         * @param rtype  Inferred return type.
         * @param ptypes Inferred parameter types.
         * @return {@link Optional} of {@link MethodTypeSpec} instance, or an empty {@link Optional}
         * if the {@code validator} {@link Default#isDefault(Validator)} or the {@link
         * Validator#value()} is {@link Default#isDefault(MethodRef)}.
         * @see #toMethodSpec(MethodRef, Class, Class[])
         */
        @SuppressWarnings("unchecked")
        public static Optional<MethodTypeSpec> toMethodSpec(Annotation methodRef, CodeType rtype, CodeType[] ptypes) {

            if (methodRef == null || Default.isDefaultMethodRef(methodRef))
                return Optional.empty();

            CodeType value = ((CodeType) methodRef.getValues().get("value"));
            String name = (String) methodRef.getValues().get("name");
            CodeType returnType = notNull(((CodeType) methodRef.getValues().get("returnType")), rtype);
            List<? extends CodeType> parameterTypes = notNull(((List<? extends CodeType>) methodRef.getValues().get("parameterTypes")), Arrays.asList(ptypes));

            if (parameterTypes.size() > 0)
                Conditions.require(ptypes.length == parameterTypes.size(), "'methodRef.parameterTypes().length' must be equal to 'ptypes.length'!");

            return Optional.of(new MethodTypeSpec(value, name,
                    new TypeSpec(
                            Conversions.CAPI.typeOr(returnType, rtype),
                            ptypes.length > 0 && parameterTypes.size() == 0
                                    ? Arrays.asList(ptypes)
                                    : CollectionsKt.mapIndexed(parameterTypes, (integer, aClass) -> typeOr(aClass, ptypes[integer]))
                    )
            ));
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
        private static CodeType typeOr(CodeType type, CodeType alternative) {
            return (!type.is(CodeAPI.getJavaType(Default.class)) ? type : alternative);
        }

        @Contract(pure = true)
        @NotNull
        private static <T> T notNull(@Nullable T input, @NotNull T other) {
            if(input == null)
                return other;

            return input;
        }
    }
}
