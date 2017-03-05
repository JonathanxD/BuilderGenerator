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

import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.base.VariableBase;
import com.github.jonathanxd.codeapi.literal.Literals;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Default validators.
 *
 * All {@code DefaultValues providers} here are {@link Inline} to avoid dependency on {@code
 * BytecodeGenerator}. This means that you don't need to have {@code BytecodeGenerator} in class
 * path even you reference these methods from {@link com.github.jonathanxd.buildergenerator.annotation.MethodRef}.
 */
public final class DefaultValues {
    private DefaultValues() {

    }

    /**
     * Empty immutable list.
     *
     * {@link Collections#emptyList}
     */
    @Inline
    public static CodePart emptyList(VariableBase propertyInfo) {
        return CodeAPI.invokeStatic(Collections.class, "emptyList", CodeAPI.typeSpec(List.class), Collections.emptyList());
    }

    /**
     * Empty immutable set.
     *
     * {@link Collections#emptySet}
     */
    @Inline
    public static CodePart emptySet(VariableBase propertyInfo) {
        return CodeAPI.invokeStatic(Collections.class, "emptySet", CodeAPI.typeSpec(Set.class), Collections.emptyList());
    }

    /**
     * Empty array.
     */
    @Inline
    public static CodePart emptyArray(VariableBase propertyInfo) {
        int arrayDimension = propertyInfo.getVariableType().getArrayDimension();

        CodePart[] dimensions = new CodePart[arrayDimension];

        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = Literals.INT(0);
        }

        return CodeAPI.arrayConstruct(propertyInfo.getVariableType(), dimensions);
    }

    /**
     * Calls the static {@code empty} method of variable type.
     */
    @Inline
    public static CodePart empty(VariableBase propertyInfo) {
        return CodeAPI.invokeStatic(propertyInfo.getVariableType(), "empty", CodeAPI.typeSpec(propertyInfo.getVariableType()), Collections.emptyList());
    }

}
