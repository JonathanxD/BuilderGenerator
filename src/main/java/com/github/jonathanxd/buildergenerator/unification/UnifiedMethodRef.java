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
package com.github.jonathanxd.buildergenerator.unification;

import com.github.jonathanxd.codeapi.extra.UnifiedAnnotation;
import com.github.jonathanxd.codeapi.type.CodeType;

/**
 * Reference to a method, at compile-time, if the method doesn't exists, the compilation will fail
 * with an error.
 *
 * @see com.github.jonathanxd.buildergenerator.apt.MethodRefValidator
 */
public interface UnifiedMethodRef extends UnifiedAnnotation {

    /**
     * Method class.
     *
     * @return Method class.
     */
    CodeType value();

    /**
     * Method return type.
     *
     * @return Method return type.
     */
    CodeType returnType();

    /**
     * Method parameter types.
     *
     * @return Method parameter types.
     */
    CodeType[] parameterTypes();

    /**
     * Name of the method.
     *
     * @return Name of the method.
     */
    String name();

}
