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

import com.github.jonathanxd.iutils.object.Default;

import java.lang.annotation.Target;

/**
 * Reference to a method, at compile-time, if the method doesn't exists, the compilation will fail
 * with an error.
 *
 * @see com.github.jonathanxd.buildergenerator.apt.MethodRefValidator
 */
@Target({})
public @interface MethodRef {

    /**
     * Method class.
     *
     * @return Method class.
     */
    Class<?> value();

    /**
     * Method return type.
     *
     * @return Method return type.
     */
    Class<?> returnType() default Default.class;

    /**
     * Method parameter types.
     *
     * @return Method parameter types.
     */
    Class<?>[] parameterTypes() default {Default.class};

    /**
     * Name of the method.
     *
     * @return Name of the method.
     */
    String name();

}
