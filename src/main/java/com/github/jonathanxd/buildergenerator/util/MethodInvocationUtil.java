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
package com.github.jonathanxd.buildergenerator.util;

import com.github.jonathanxd.buildergenerator.spec.BuilderSpec;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.base.MethodInvocation;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.common.TypeSpec;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.iutils.collection.CollectionUtils;

import java.util.List;
import java.util.Optional;

public final class MethodInvocationUtil {

    private MethodInvocationUtil() {
        throw new IllegalStateException();
    }

    public static MethodInvocation validationToInvocation(MethodTypeSpec methodTypeSpec, CodePart valueAccess, PropertySpec propertySpec) {
        return MethodInvocationUtil.toInvocation(methodTypeSpec, CollectionUtils.listOf(valueAccess, Literals.STRING(propertySpec.getName()), Literals.CLASS(propertySpec.getType())));
    }

    public static MethodInvocation defaultValueToInvocation(MethodTypeSpec methodTypeSpec, PropertySpec propertySpec) {
        return MethodInvocationUtil.toInvocation(methodTypeSpec, CollectionUtils.listOf(Literals.STRING(propertySpec.getName()), Literals.CLASS(propertySpec.getType())));
    }

    public static MethodInvocation toInvocation(MethodTypeSpec methodTypeSpec, List<CodePart> arguments) {
        return CodeAPI.invokeStatic(methodTypeSpec.getLocalization(), methodTypeSpec.getMethodName(), methodTypeSpec.getTypeSpec(), arguments);
    }

    public static MethodInvocation createFactoryInvocation(BuilderSpec builderSpec, List<CodeType> propertiesTypes, List<CodePart> arguments) {
        Optional<String> factoryMethodName = builderSpec.getFactoryMethodName();

        TypeSpec typeSpec = new TypeSpec(builderSpec.getFactoryResultType(), propertiesTypes);

        if(factoryMethodName.isPresent()) {
            return CodeAPI.invokeStatic(builderSpec.getFactoryClass(), factoryMethodName.get(), typeSpec, arguments);
        } else {
            return CodeAPI.invokeConstructor(builderSpec.getFactoryClass(), typeSpec, arguments);
        }

    }
}
