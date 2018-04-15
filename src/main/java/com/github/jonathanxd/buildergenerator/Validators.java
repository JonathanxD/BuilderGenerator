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
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.Concat;
import com.github.jonathanxd.kores.base.ThrowException;
import com.github.jonathanxd.kores.base.VariableBase;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.helper.ConcatHelper;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.operator.Operators;

import java.util.Collections;

/**
 * Default validators.
 *
 * All validators here are {@link Inline} to avoid dependency on {@code BytecodeGenerator}. This means that you don't need to have
 * {@code BytecodeGenerator} in class path even you reference these methods from {@link com.github.jonathanxd.buildergenerator.annotation.MethodRef}.
 */
public final class Validators {
    private Validators() {

    }

    /**
     * Accept integer {@code i} only if the value is positive.
     *
     * Java code:
     * <pre>
     * {@code
     * if(i < 0)
     *     throw new IllegalArgumentException("The input integer '"+i+"' for property
     * '"+propertyName+"' must be positive.");
     * }
     * </pre>
     *
     * @param input        Input value.
     * @param propertyInfo Property info.
     */
    @Inline
    public static Instruction positiveInt(VariableBase propertyInfo, Instruction input) {

        Concat message = ConcatHelper.builder("The input integer '")
                .concat(input)
                .concat("' for property '")
                .concat(propertyInfo.getName())
                .concat("' must be positive.")
                .build();

        ThrowException throwException = Factories.throwException(
                InvocationFactory.invokeConstructor(IllegalArgumentException.class, Factories.constructorTypeSpec(String.class),
                        Collections.singletonList(message)
                )
        );

        return Factories.ifStatement(Factories.ifExprs(Factories.check(input, Operators.LESS_THAN, Literals.INT(0))),
                Instructions.fromPart(
                        throwException
                ));

    }


}
