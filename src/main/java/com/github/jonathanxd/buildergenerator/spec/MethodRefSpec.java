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
package com.github.jonathanxd.buildergenerator.spec;

import com.github.jonathanxd.codeapi.common.MethodTypeSpec;

/**
 * Specification of a method reference.
 */
public class MethodRefSpec {

    /**
     * Referenced method is a local method.
     */
    private final boolean isThis;

    /**
     * Referenced method is marked to {@link com.github.jonathanxd.buildergenerator.annotation.Inline}.
     */
    private final boolean isInline;

    /**
     * Specification of method.
     */
    private final MethodTypeSpec methodTypeSpec;

    /**
     * Create a method reference specification.
     *
     * @param isThis         Referenced method is a local method.
     * @param isInline       Referenced method is marked to {@link com.github.jonathanxd.buildergenerator.annotation.Inline}.
     * @param methodTypeSpec Specification of method.
     */
    public MethodRefSpec(boolean isThis, boolean isInline, MethodTypeSpec methodTypeSpec) {
        this.isThis = isThis;
        this.isInline = isInline;
        this.methodTypeSpec = methodTypeSpec;
    }


    /**
     * Returns true if referenced method is a local method.
     * @return True if referenced method is a local method.
     */
    public boolean isThis() {
        return this.isThis;
    }

    /**
     * Returns true if this method is marked to {@link com.github.jonathanxd.buildergenerator.annotation.Inline}.
     *
     * @return True if this method is marked to {@link com.github.jonathanxd.buildergenerator.annotation.Inline}.
     */
    public boolean isInline() {
        return this.isInline;
    }

    /**
     * Gets the method reference specification.
     *
     * @return Method reference specification.
     */
    public MethodTypeSpec getMethodTypeSpec() {
        return this.methodTypeSpec;
    }
}
