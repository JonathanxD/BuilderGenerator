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
package com.github.jonathanxd.buildergenerator.universal;

import com.github.jonathanxd.codeapi.type.CodeType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Reference to a method.
 */
public interface MethodElementRef extends ElementRef {

    /**
     * Gets the method name.
     *
     * @return {@link Optional} of method name, or empty {@link Optional} for constructors.
     */
    Optional<String> getName();

    /**
     * Gets the method return type. (Always {@link com.github.jonathanxd.codeapi.Types#VOID} for
     * constructors.
     *
     * @return Method return type. (Always {@link com.github.jonathanxd.codeapi.Types#VOID} for
     * constructors.
     */
    CodeType getReturnType();

    /**
     * Gets parameter type list.
     *
     * @return Parameter type list.
     */
    List<CodeType> getParameterTypes();

    class Impl implements MethodElementRef {

        private final CodeType enclosingType;
        private final String name;
        private final CodeType returnType;
        private final List<CodeType> parameterTypes;

        public Impl(CodeType enclosingType, String name, CodeType returnType, List<CodeType> parameterTypes) {
            this.enclosingType = enclosingType;
            this.name = name;
            this.returnType = returnType;
            this.parameterTypes = Collections.unmodifiableList(parameterTypes);
        }

        @Override
        public CodeType getEnclosingType() {
            return this.enclosingType;
        }

        @Override
        public Optional<String> getName() {
            return Optional.ofNullable(this.name);
        }

        @Override
        public CodeType getReturnType() {
            return this.returnType;
        }

        @Override
        public List<CodeType> getParameterTypes() {
            return this.parameterTypes;
        }
    }
}
