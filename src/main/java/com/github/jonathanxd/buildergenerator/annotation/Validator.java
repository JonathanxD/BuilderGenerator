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

import java.lang.annotation.Target;

/**
 * Specify the validator method. Validator method normally throws {@link IllegalArgumentException}
 * or {@link IllegalStateException} when the input value is invalid.
 *
 * The validator method must be static, return {@code void} and receive a parameter of the same type
 * as {@code property type} or a {@code super type} of property type, a parameter of type {@link
 * String} to receive {@code property name} and a parameter of type {@link Class} to receive {@code
 * property type}. These rules only applies to non-inline validators. Validators annotated with
 * {@link Inline} has different rules, the rules depends on the implementation of the {@link
 * com.github.jonathanxd.buildergenerator.BuilderGenerator}. {@link Inline} documentation explain
 * these rules.
 *
 * Also you can use {@link Super} and implement the method with validation code instead of
 * annotating it with {@link Validator}.
 */
@Target({})
public @interface Validator {

    /**
     * Method reference.
     *
     * @return Method reference.
     */
    MethodRef value();

}
