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

import com.github.jonathanxd.buildergenerator.annotation.Conversions;
import com.github.jonathanxd.buildergenerator.unification.UnifiedMethodRef;
import com.github.jonathanxd.buildergenerator.util.TypeElementUtil;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.Annotation;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.iutils.object.Pair;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Method resolver. This class finds the referenced method.
 */
public class AptResolver {

    /**
     * Resolves the method reference.
     *
     * @param unifiedMethodRef {@link com.github.jonathanxd.buildergenerator.annotation.MethodRef}
     *                            annotation.
     * @param rtype               Inferred return type (if annotation property is defined, this
     *                            value will be ignored).
     * @param ptypes              Inferred parameter types (if annotation property is defined, this
     *                            value will be ignored).
     * @param elements            Element utilities.
     * @return Null if cannot convert the annotation to {@link MethodTypeSpec} or a Pair of {@link
     * MethodTypeSpec method specification} and {@link ExecutableElement found method} (or null
     * {@link ExecutableElement} if the method cannot be found).
     * @see Conversions.CAPI#toMethodSpec(Annotation, CodeType, CodeType[])
     */
    @Nullable
    public static Pair<MethodTypeSpec, ExecutableElement> resolveMethodRef(UnifiedMethodRef unifiedMethodRef, CodeType rtype, CodeType[] ptypes, Elements elements) {

        Optional<MethodTypeSpec> methodTypeSpec = Conversions.CAPI.toMethodSpec(unifiedMethodRef, rtype, ptypes);

        if (methodTypeSpec.isPresent()) {
            MethodTypeSpec spec = methodTypeSpec.get();

            CodeType enclosingType = spec.getLocalization();
            String name = spec.getMethodName();
            CodeType returnType = spec.getTypeSpec().getReturnType();
            List<CodeType> parameterTypes = spec.getTypeSpec().getParameterTypes();

            TypeElement enclosingElement = elements.getTypeElement(enclosingType.getCanonicalName());

            return Pair.of(spec, elements.getAllMembers(enclosingElement)
                    .stream()
                    .filter(o -> {
                        if (o instanceof ExecutableElement) {
                            ExecutableElement executableElement = (ExecutableElement) o;

                            boolean nameMatch = executableElement.getKind() == ElementKind.CONSTRUCTOR && name.equals("<init>")
                                    || executableElement.getSimpleName().contentEquals(name);

                            boolean returnMatch =
                                    executableElement.getKind() == ElementKind.CONSTRUCTOR && returnType.is(Types.VOID)
                                            || TypeElementUtil.toCodeType(executableElement.getReturnType(), elements)
                                            .getJavaSpecName().equals(returnType.getJavaSpecName());

                            boolean parametersMatch = true;

                            List<? extends VariableElement> parameters = executableElement.getParameters();

                            if (parameters.size() == parameterTypes.size()) {
                                for (int i = 0; i < parameters.size(); i++) {
                                    CodeType codeType = TypeElementUtil.toCodeType(parameters.get(i).asType(), elements);
                                    if (!codeType.getJavaSpecName().equals(parameterTypes.get(i).getJavaSpecName())) {
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
                    .findFirst()
                    .orElse(null));
        }

        return null;
    }

}
