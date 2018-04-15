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
package com.github.jonathanxd.buildergenerator;

import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.base.VariableBase;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Default validators.
 *
 * All {@code DefaultValues providers} here are {@link Inline} to avoid dependency on {@code BytecodeGenerator}. This means that
 * you don't need to have {@code BytecodeGenerator} in class path even you reference these methods from {@link
 * com.github.jonathanxd.buildergenerator.annotation.MethodRef}.
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
    public static Instruction emptyList(VariableBase propertyInfo) {
        return InvocationFactory.invokeStatic(Collections.class, "emptyList", Factories.typeSpec(List.class),
                Collections.emptyList());
    }

    /**
     * Empty immutable set.
     *
     * {@link Collections#emptySet}
     */
    @Inline
    public static Instruction emptySet(VariableBase propertyInfo) {
        return InvocationFactory.invokeStatic(Collections.class, "emptySet", Factories.typeSpec(Set.class),
                Collections.emptyList());
    }

    /**
     * Empty array.
     */
    @Inline
    public static Instruction emptyArray(VariableBase propertyInfo) {
        int arrayDimension = ImplicitKoresType.getArrayDimension(propertyInfo.getVariableType());

        List<Instruction> dimensions = new ArrayList<>(arrayDimension);

        for (int i = 0; i < arrayDimension; i++) {
            dimensions.add(Literals.INT(0));
        }

        return Factories.createArray(propertyInfo.getVariableType(), dimensions, Collections.emptyList());
    }

    /**
     * Calls the static {@code empty} method of variable type.
     */
    @Inline
    public static Instruction empty(VariableBase propertyInfo) {
        return InvocationFactory.invokeStatic(propertyInfo.getVariableType(), "empty",
                Factories.typeSpec(propertyInfo.getVariableType()), Collections.emptyList());
    }

}
