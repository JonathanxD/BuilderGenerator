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
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.condition.Conditions;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Alias;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.type.KoresType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Common default implementations.
 *
 * All {@code DefaultImpl providers} here are {@link Inline} to avoid dependency on {@code BytecodeGenerator}. This means that you
 * don't need to have {@code BytecodeGenerator} in class path even you reference these methods from {@link
 * com.github.jonathanxd.buildergenerator.annotation.MethodRef}.
 */
public final class Defaults {
    private Defaults() {

    }

    /**
     * Returns self instance.
     */
    @Inline
    public static Instruction self(MethodDeclaration methodDeclaration, List<Instruction> arguments) {
        return Factories.returnValue(methodDeclaration.getReturnType(), Factories.accessThis());
    }

    /**
     * Convert box vararg into a list and call the same method with {@link List} as parameter type instead of vararg.
     *
     * {@link Arrays#asList}
     */
    @Inline
    public static Instruction varArgToList(MethodDeclaration methodDeclaration, List<Instruction> arguments) {
        Conditions.require(arguments.size() == 1, "Only method with single parameter is supported!");

        return Defaults.transform(methodDeclaration, arguments, Types.LIST, codePart -> Collections.singletonList(
                InvocationFactory.invokeStatic(Arrays.class, "asList", Factories.typeSpec(Object[].class), arguments)));
    }

    /**
     * Convert box vararg into a set and call the same method with {@link Set} as parameter type instead of vararg.
     *
     * {@link Arrays#asList} {@link HashSet}
     */
    @Inline
    public static Instruction varArgToSet(MethodDeclaration methodDeclaration, List<Instruction> arguments) {
        Conditions.require(arguments.size() == 1, "Only method with single parameter is supported!");

        return Defaults.transform(methodDeclaration, arguments, Types.SET, codePart -> Collections.singletonList(
                InvocationFactory.invokeConstructor(HashSet.class, Factories.constructorTypeSpec(Collection.class),
                        Collections.singletonList(
                                InvocationFactory.invokeStatic(Arrays.class, "asList", Factories.typeSpec(Object[].class),
                                        arguments)))));
    }

    /**
     * Transform input arguments.
     */
    private static Instruction transform(MethodDeclaration methodDeclaration, List<Instruction> arguments, KoresType type,
                                         Function<List<Instruction>, List<Instruction>> transformer) {

        TypeSpec typeSpec = new TypeSpec(methodDeclaration.getReturnType(), Collections3.listOf(type));

        return Factories.returnValue(methodDeclaration.getReturnType(),
                InvocationFactory.invokeInterface(Alias.THIS.INSTANCE, Factories.accessThis(), methodDeclaration.getName(),
                        typeSpec,
                        transformer.apply(arguments)));
    }


}
