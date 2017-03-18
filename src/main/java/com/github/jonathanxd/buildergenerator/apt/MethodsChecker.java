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
package com.github.jonathanxd.buildergenerator.apt;

import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.common.TypeSpec;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.util.ModelCodeTypesKt;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;

public final class MethodsChecker implements Consumer<List<MethodTypeSpec>> {

    private final AnnotationProcessor annotationProcessor;
    private final TypeElement impl;
    private final Consumer<String> errorHandler;

    public MethodsChecker(AnnotationProcessor annotationProcessor, TypeElement impl, Consumer<String> errorHandler) {
        this.annotationProcessor = annotationProcessor;
        this.impl = impl;
        this.errorHandler = errorHandler;
    }

    @Override
    public void accept(List<MethodTypeSpec> methodTypeSpecs) {
        this.annotationProcessor.consumeMethods(this.impl, executableElement -> {

            List<CodeType> parameterTypes = executableElement.getParameters().stream().map(o -> ModelCodeTypesKt.getCodeType(o.asType())).collect(Collectors.toList());

            MethodTypeSpec methodTypeSpec = new MethodTypeSpec(ModelCodeTypesKt.getCodeType(this.impl), executableElement.getSimpleName().toString(), new TypeSpec(ModelCodeTypesKt.getCodeType(executableElement.getReturnType()), parameterTypes));

            // This is a partial check until CodeAPI implements CodeType#isAssignableFrom
            boolean any = methodTypeSpecs.stream().anyMatch(generated ->
                    generated.getMethodName().equals(methodTypeSpec.getMethodName()) && generated.getTypeSpec().getParameterTypes().size() == methodTypeSpec.getTypeSpec().getParameterTypes().size()
            );

            if(!any)
                errorHandler.accept("Missing implementation of method '"+methodTypeSpec+"'!");

        });
    }
}
