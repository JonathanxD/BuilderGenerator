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
package com.github.jonathanxd.buildergenerator;

import com.github.jonathanxd.buildergenerator.spec.BuilderSpec;
import com.github.jonathanxd.codeapi.base.TypeDeclaration;

/**
 * Builder generator interface.
 */
public interface BuilderGenerator<T> {

    /**
     * Generates the builder class.
     *
     * @param builderSpec Builder specification.
     * @return Generated builder classes.
     */
    T generate(BuilderSpec builderSpec);

    /**
     * Specification of generated class.
     */
    final class ClassSpec {

        /**
         * Qualified name of the generated class.
         */
        private final String qualifiedName;

        /**
         * Class declaration.
         */
        private final TypeDeclaration declaration;

        /**
         * Bytecode of the generated class.
         */
        private final byte[] bytecode;

        /**
         * Source of generated class.
         */
        private final byte[] source;

        public ClassSpec(String qualifiedName, TypeDeclaration declaration, byte[] bytecode, byte[] source) {
            this.qualifiedName = qualifiedName;
            this.declaration = declaration;
            this.bytecode = bytecode;
            this.source = source;
        }

        /**
         * Gets the qualified name of the generated class.
         *
         * @return Qualified name of the generated class.
         */
        public String getQualifiedName() {
            return this.qualifiedName;
        }

        /**
         * Gets the class declaration.
         *
         * @return Class declaration.
         */
        public TypeDeclaration getDeclaration() {
            return this.declaration;
        }

        /**
         * Gets the bytecode of the generated class.
         *
         * @return Bytecode of the generated class.
         */
        public byte[] getBytecode() {
            return this.bytecode;
        }

        /**
         * Gets source of the generated class.
         *
         * @return Source of the generated class.
         */
        public byte[] getSource() {
            return this.source;
        }
    }

}
