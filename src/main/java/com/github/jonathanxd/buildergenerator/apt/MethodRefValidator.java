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

import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.annotation.Validator;
import com.github.jonathanxd.buildergenerator.util.AnnotatedConstructUtil;
import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.Annotation;
import com.github.jonathanxd.codeapi.base.VariableBase;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.LoadedCodeType;
import com.github.jonathanxd.iutils.object.Pair;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Validates the reference to a method.
 *
 * The validation is divided in three steps:
 *
 * In the first step the validator tries to resolve the method using {@link
 * com.github.jonathanxd.buildergenerator.annotation.Inline} rules for {@link
 * PropertyInfo#validator()} and {@link PropertyInfo#defaultValue()}.
 *
 * In the second step the validator tries to resolve the method using rules specified in {@link
 * PropertyInfo#validator()} and {@link PropertyInfo#defaultValue()}.
 *
 * In the third step, the validator will check if the method is public and static, and for {@link
 * com.github.jonathanxd.buildergenerator.annotation.Inline inline} methods, the validator will
 * check if the method is compiled.
 *
 * Attention: {@link com.github.jonathanxd.buildergenerator.annotation.MethodRef Annotation}
 * properties override return type and parameter type rules.
 */
public class MethodRefValidator {


    /**
     * Validates the method.
     *
     * @param annotated    Annotated element.
     * @param mirror       MethodRef Annotation mirror.
     * @param annotation   Annotation instance.
     * @param propertyType Property type.
     * @param messager     Messager to log errors.
     * @param elements     Element utilities to resolve types.
     * @param isValidator  True if the method to validate is the {@link Validator#value() validator
     *                     method}.
     * @return True if success, false if validation failed.
     */
    public static boolean validate(ExecutableElement annotated, AnnotationMirror mirror, Annotation annotation, CodeType propertyType, Messager messager, Elements elements, boolean isValidator) {

        CodeType codePart = CodeAPI.getJavaType(CodePart.class);
        CodeType varBase = CodeAPI.getJavaType(VariableBase.class);

        Pair<MethodTypeSpec, ExecutableElement> resolvedMethodRef = isValidator
                ? AptResolver.resolveMethodRef(annotation, codePart, new CodeType[]{varBase, codePart}, elements)
                : AptResolver.resolveMethodRef(annotation, codePart, new CodeType[]{varBase}, elements);

        Pair<MethodTypeSpec, ExecutableElement> first = resolvedMethodRef;

        boolean isInline = resolvedMethodRef != null
                && resolvedMethodRef._2() != null
                && AnnotatedConstructUtil.getAnnotationMirror(resolvedMethodRef._2(), "com.github.jonathanxd.buildergenerator.annotation.Inline").isPresent();

        if (resolvedMethodRef == null || resolvedMethodRef._2() == null) {
            CodeType baseRetType = isValidator ? Types.VOID : propertyType;
            CodeType[] ptypes = isValidator
                    ? new CodeType[]{propertyType, Types.STRING, Types.CLASS}
                    : new CodeType[]{Types.STRING, Types.CLASS};

            resolvedMethodRef = AptResolver.resolveMethodRef(annotation, baseRetType, ptypes, elements);
        }

        if (resolvedMethodRef == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Invalid provided method reference!", annotated, mirror);
            return true;
        } else if (resolvedMethodRef._2() == null) {
            String additional = first == resolvedMethodRef || first == null ? "" : "(or '" + first._1().toMethodString() + "')";

            messager.printMessage(Diagnostic.Kind.ERROR, "Cannot find referenced method '" + resolvedMethodRef._1().toMethodString() + "'" + additional + "!", annotated, mirror);
            return true;
        } else {
            MethodTypeSpec spec = resolvedMethodRef._1();

            ExecutableElement executableElement = resolvedMethodRef._2();

            CodeType localization = spec.getLocalization();

            if (isInline) {
                if (!(localization instanceof LoadedCodeType<?>)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Referenced inline method '" + spec.toMethodString() + "' is not compiled yet. This method cannot be inlined!", annotated, mirror);
                    messager.printMessage(Diagnostic.Kind.ERROR, "Inline method referenced from another context is not compiled yet!", executableElement);
                    return false;
                }
            }

            if (!executableElement.getModifiers().contains(Modifier.PUBLIC) || !executableElement.getModifiers().contains(Modifier.STATIC)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Referenced method '" + spec.toMethodString() + "' must be public and static!", annotated, mirror);
                messager.printMessage(Diagnostic.Kind.ERROR, "Method referenced from another context is not public and static!", executableElement);
                return false;
            }

        }

        return true;
    }

}