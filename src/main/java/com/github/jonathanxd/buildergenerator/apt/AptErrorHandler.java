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

import com.github.jonathanxd.buildergenerator.ErrorHandler;
import com.github.jonathanxd.buildergenerator.universal.ClassElementRef;
import com.github.jonathanxd.buildergenerator.universal.ElementRef;
import com.github.jonathanxd.buildergenerator.universal.MethodElementRef;
import com.github.jonathanxd.buildergenerator.util.TypeElementUtil;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.type.CodeType;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public final class AptErrorHandler implements ErrorHandler {

    private final ProcessingEnvironment processingEnvironment;
    private final Messager messager;

    public AptErrorHandler(ProcessingEnvironment processingEnvironment, Messager messager) {
        this.processingEnvironment = processingEnvironment;
        this.messager = messager;
    }

    @Override
    public void error(String message, ElementRef ref) {

        Element element;

        if (ref instanceof ClassElementRef) {
            element = this.processingEnvironment.getElementUtils().getTypeElement(((ClassElementRef) ref).getType().getCanonicalName());
        }

        if (ref instanceof MethodElementRef) {
            MethodElementRef methodElementRef = (MethodElementRef) ref;
            TypeElement enclosingElement = this.processingEnvironment.getElementUtils().getTypeElement(ref.getEnclosingType().getCanonicalName());
            element = this.processingEnvironment.getElementUtils().getAllMembers(enclosingElement)
                    .stream()
                    .filter(o -> {
                        if (o instanceof ExecutableElement) {
                            ExecutableElement executableElement = (ExecutableElement) o;

                            boolean nameMatch = executableElement.getKind() == ElementKind.CONSTRUCTOR && !methodElementRef.getName().isPresent()
                                    || executableElement.getSimpleName().contentEquals(methodElementRef.getName().get());

                            boolean returnMatch =
                                    executableElement.getKind() == ElementKind.CONSTRUCTOR && methodElementRef.getReturnType().is(Types.VOID)
                                            || TypeElementUtil.toCodeType(executableElement.getReturnType(), this.processingEnvironment.getElementUtils())
                                            .is(methodElementRef.getReturnType());

                            boolean parametersMatch = true;

                            List<CodeType> parameterTypes = methodElementRef.getParameterTypes();
                            List<? extends VariableElement> parameters = executableElement.getParameters();

                            if(parameters.size() == parameterTypes.size()) {
                                for (int i = 0; i < parameters.size(); i++) {
                                    CodeType codeType = TypeElementUtil.toCodeType(parameters.get(i).asType(), this.processingEnvironment.getElementUtils());
                                    if(!codeType.is(parameterTypes.get(i))) {
                                        parametersMatch = false;
                                        break;
                                    }

                                }
                            } else {
                                parametersMatch = false;
                            }

                            return nameMatch && returnMatch && parametersMatch;

                        }

                        return false;
                    })
                    .map(o -> (ExecutableElement) o)
                    .findFirst().orElse(null);

        }

        CodeType enclosingType = ref.getEnclosingType();


    }
}
