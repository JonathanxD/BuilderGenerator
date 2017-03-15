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
import com.github.jonathanxd.codeapi.base.Annotation;
import com.github.jonathanxd.codeapi.type.CodeType;

public final class Default {
    private Default() {
        throw new IllegalStateException();
    }

    /**
     * Returns true if {@code validator} is default.
     *
     * @param validator Validator
     * @return True if {@code validator} is default.
     */
    public static boolean isDefault(Validator validator) {
        return validator.value().value() == Default.class;
    }

    /**
     * Returns true if {@code methodRef} is default.
     *
     * @param methodRef Method reference.
     * @return True if {@code methodRef} is default.
     */
    public static boolean isDefault(MethodRef methodRef) {
        return methodRef.value() == Default.class;
    }

    /**
     * Returns true if {@code methodRef} annotation is default.
     *
     * @param methodRef Method reference.
     * @return True if {@code methodRef} annotation is default.
     */
    public static boolean isDefaultMethodRef(Annotation methodRef) {
        return ((CodeType) methodRef.getValues().get("value")).is(CodeAPI.getJavaType(Default.class));
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
     * Returns true if {@code validator} annotation is default.
     *
     * @param validator Validator
     * @return True if {@code validator} annotation is default.
     */
    public static boolean isDefaultValidator(Annotation validator) {
        return isDefaultMethodRef(((Annotation) validator.getValues().get("value")));
    }

}
