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
package com.github.jonathanxd.buildergenerator.spec;

import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.common.MethodTypeSpec;

import java.util.Objects;
import java.util.Optional;

/**
 * Method specification.
 */
public class MethodSpec {

    /**
     * Target method to apply specification.
     */
    private final MethodDeclaration targetMethod;

    /**
     * Default method specification.
     *
     * @see com.github.jonathanxd.buildergenerator.annotation.DefaultImpl
     */
    private final MethodRefSpec defaultMethod;

    public MethodSpec(MethodDeclaration targetMethod, MethodRefSpec defaultMethod) {
        Objects.requireNonNull(targetMethod);
        this.targetMethod = targetMethod;
        this.defaultMethod = defaultMethod;
    }

    /**
     * Gets the target method specification.
     *
     * @return Target method specification.
     */
    public MethodDeclaration getTargetMethod() {
        return this.targetMethod;
    }

    /**
     * Gets the default method specification.
     *
     * @return Default method specification.
     * @see com.github.jonathanxd.buildergenerator.annotation.DefaultImpl
     */
    public Optional<MethodRefSpec> getDefaultMethod() {
        return Optional.ofNullable(this.defaultMethod);
    }
}
