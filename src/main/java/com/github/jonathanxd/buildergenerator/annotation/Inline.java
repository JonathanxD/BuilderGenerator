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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the {@link Validator} or {@link MethodRef} to be inlined.
 *
 * This operation depends on the implementation of the {@link com.github.jonathanxd.buildergenerator.BuilderGenerator},
 * the default generator will require that method return a {@link com.github.jonathanxd.codeapi.CodePart},
 * and have at least 1 parameter of type {@link com.github.jonathanxd.codeapi.base.VariableBase},
 * this parameter is the {@code property info}.
 *
 * For {@link PropertyInfo#validator()}, a second parameter is required, this parameter must be of
 * {@link com.github.jonathanxd.codeapi.CodePart} type, this parameter is the access to value to
 * validate.
 *
 * For {@link PropertyInfo#defaultValue()} no additional parameters is required.
 *
 * Note: This annotation only works for compiled methods, {@link com.github.jonathanxd.buildergenerator.apt.MethodRefValidator}
 * will throw a exception if a non-compiled method is referenced from a {@link MethodRef} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Inline {
}
