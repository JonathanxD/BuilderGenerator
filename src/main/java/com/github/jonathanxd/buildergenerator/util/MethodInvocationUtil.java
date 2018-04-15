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
package com.github.jonathanxd.buildergenerator.util;

import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.spec.BuilderSpec;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.MethodInvocation;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.KoresType;

import java.util.List;
import java.util.Optional;

/**
 * Conversion to {@link MethodInvocation}.
 */
public final class MethodInvocationUtil {

    private MethodInvocationUtil() {
        throw new IllegalStateException();
    }

    /**
     * Converts a {@link PropertyInfo#validator()} to {@link MethodInvocation}.
     *
     * @param methodTypeSpec Validator method specification.
     * @param valueAccess    Access to value to validate.
     * @param propertySpec   Property specification.
     * @return Invocation of the validator method.
     */
    public static MethodInvocation validationToInvocation(boolean isThis, MethodTypeSpec methodTypeSpec, Instruction valueAccess,
                                                          PropertySpec propertySpec) {
        return MethodInvocationUtil.toInvocation(isThis, methodTypeSpec,
                Collections3.listOf(valueAccess, Literals.STRING(propertySpec.getName()),
                        Literals.CLASS(propertySpec.getType())));
    }

    /**
     * Converts a {@link PropertyInfo#defaultValue()} to {@link MethodInvocation}.
     *
     * @param methodTypeSpec Default value method specification.
     * @param propertySpec   Property specification.
     * @return Invocation of default value method.
     */
    public static MethodInvocation defaultValueToInvocation(boolean isThis, MethodTypeSpec methodTypeSpec,
                                                            PropertySpec propertySpec) {
        return MethodInvocationUtil.toInvocation(isThis, methodTypeSpec,
                Collections3.listOf(Literals.STRING(propertySpec.getName()), Literals.CLASS(propertySpec.getType())));
    }

    /**
     * Convert {@link MethodTypeSpec} to {@link MethodInvocation}.
     *
     * @param methodTypeSpec Method specification.
     * @param arguments      Arguments to pass to method.
     * @return Method invocation.
     */
    public static MethodInvocation toInvocation(boolean isThis, MethodTypeSpec methodTypeSpec, List<Instruction> arguments) {

        InvokeType type = InvokeType.INVOKE_STATIC;
        Instruction access = Access.STATIC;

        if (isThis) {
            type = InvokeType.get(methodTypeSpec.getLocalization());
            access = Factories.accessThis();
        }

        return InvocationFactory.invoke(type, methodTypeSpec.getLocalization(), access, methodTypeSpec.getMethodName(),
                methodTypeSpec.getTypeSpec(), arguments);
    }

    /**
     * Convert {@link BuilderSpec} factory method to {@link MethodInvocation}.
     *
     * @param builderSpec     Builder specification.
     * @param propertiesTypes Property types.
     * @param arguments       Arguments to pass to factory method.
     */
    public static MethodInvocation createFactoryInvocation(BuilderSpec builderSpec, List<KoresType> propertiesTypes,
                                                           List<Instruction> arguments) {
        Optional<String> factoryMethodName = builderSpec.getFactoryMethodName();

        TypeSpec typeSpec = new TypeSpec(builderSpec.getFactoryResultType(), propertiesTypes);

        return factoryMethodName
                .map(s -> InvocationFactory.invokeStatic(builderSpec.getFactoryClass(), s, typeSpec, arguments))
                .orElseGet(() -> InvocationFactory.invokeConstructor(builderSpec.getFactoryClass(), typeSpec, arguments));

    }
}
