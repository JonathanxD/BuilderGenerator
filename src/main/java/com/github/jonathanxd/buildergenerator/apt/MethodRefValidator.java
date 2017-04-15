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
import com.github.jonathanxd.buildergenerator.spec.MethodRefSpec;
import com.github.jonathanxd.buildergenerator.unification.UnifiedInline;
import com.github.jonathanxd.buildergenerator.unification.UnifiedMethodRef;
import com.github.jonathanxd.buildergenerator.util.AnnotatedConstructUtil;
import com.github.jonathanxd.buildergenerator.util.CTypeUtil;
import com.github.jonathanxd.buildergenerator.util.TypeElementUtil;
import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.VariableBase;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.extra.AnnotationsKt;
import com.github.jonathanxd.codeapi.extra.UnifiedAnnotationsUtilKt;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.LoadedCodeType;
import com.github.jonathanxd.codeapi.util.CodeTypes;
import com.github.jonathanxd.iutils.object.Pair;
import com.github.jonathanxd.iutils.type.TypeInfo;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import kotlin.Unit;

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
 * Attention: Signature properties defined in {@link com.github.jonathanxd.buildergenerator.annotation.MethodRef
 * MethodRef Annotation} overrides signatures specified by the {@link PropertyInfo}.
 */
public class MethodRefValidator {

    /**
     * Validates the method.
     *
     * @param annotated Annotated element.
     * @param methodRef MethodRef Annotation mirror.
     * @param messager  Messager to log errors.
     * @param elements  Element utilities to resolve types.
     * @param type      Type of the annotation to validate.
     * @return True if success, false if validation failed.
     */
    public static boolean validate(ExecutableElement annotated, UnifiedMethodRef methodRef, Messager messager, Elements elements, Type type) {

        Object original = AnnotationsKt.getHandlerOfAnnotation(methodRef).getOriginal();

        AnnotationMirror mirror = original instanceof AnnotationMirror ? (AnnotationMirror) original : null;

        try {
            MethodRefValidator.get(annotated, methodRef, elements, type);
        } catch (IllegalArgumentException e) {

            if (mirror == null || Options.isThrowExceptions())
                throw e;

            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), annotated, mirror);
            return false;
        } catch (ReferencedMethodException e) {
            if (mirror != null)
                messager.printMessage(Diagnostic.Kind.WARNING, e.getMessage(), annotated, mirror);

            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.getReferencedMethod());
            if (mirror == null || Options.isThrowExceptions())
                throw e;

            return false;
        }

        return true;
    }

    /**
     * Validates the method.
     *
     * @param annotated Annotated element.
     * @param elements  Element utilities to resolve types.
     * @param type      Type of the annotation to validate.
     * @return True if success, false if validation failed.
     */
    public static MethodRefSpec get(ExecutableElement annotated, UnifiedMethodRef unifiedMethodRef, Elements elements, Type type) {

        CodeType codePart = CodeAPI.getJavaType(CodePart.class);
        CodeType varBase = CodeAPI.getJavaType(VariableBase.class);

        Pair<MethodTypeSpec, ExecutableElement> resolvedMethodRef;
        boolean reqPropertyType = type == Type.VALIDATOR || type == Type.DEFAULT_VALUE;
        boolean isThis = false;

        if (reqPropertyType) {
            if (annotated.getParameters().size() != 1) {
                throw new IllegalArgumentException("Builder property method MUST have only one argument.");
            }
        }

        // Inline method resolver
        switch (type) {
            case VALIDATOR: {
                resolvedMethodRef = AptResolver.resolveMethodRef(unifiedMethodRef, codePart, new CodeType[]{varBase, codePart}, elements);
                break;
            }
            case DEFAULT_VALUE: {
                resolvedMethodRef = AptResolver.resolveMethodRef(unifiedMethodRef, codePart, new CodeType[]{varBase}, elements);
                break;
            }
            case DEFAULT_IMPL: {
                if (!unifiedMethodRef.name().startsWith(":"))
                    resolvedMethodRef = AptResolver.resolveMethodRef(unifiedMethodRef, codePart, new CodeType[]{CodeAPI.getJavaType(MethodDeclaration.class), Types.LIST /* List<CodePart> */}, elements);
                else
                    resolvedMethodRef = null;
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid annotation type: '" + type + "'!");
            }
        }

        Pair<MethodTypeSpec, ExecutableElement> first = resolvedMethodRef;

        boolean isInline = resolvedMethodRef != null
                && resolvedMethodRef._2() != null
                && AnnotatedConstructUtil.getUnifiedAnnotation(resolvedMethodRef._2(), "com.github.jonathanxd.buildergenerator.annotation.Inline", UnifiedInline.class).isPresent();

        if (resolvedMethodRef == null || resolvedMethodRef._2() == null) {
            CodeType baseRetType;
            CodeType[] ptypes;

            CodeType propertyType = null;

            if (reqPropertyType)
                propertyType = TypeElementUtil.toCodeType(annotated.getParameters().get(0).asType(), elements);

            switch (type) {
                case VALIDATOR: {
                    baseRetType = Types.VOID;
                    ptypes = new CodeType[]{propertyType, Types.STRING, Types.CLASS};
                    break;
                }
                case DEFAULT_VALUE: {
                    baseRetType = propertyType;
                    ptypes = new CodeType[]{Types.STRING, Types.CLASS};
                    break;
                }
                case DEFAULT_IMPL: {
                    TypeElement receiverType = (TypeElement) annotated.getEnclosingElement();
                    CodeType receiver = TypeElementUtil.toCodeType(receiverType);

                    List<CodeType> typeList = new ArrayList<>();

                    typeList.add(receiver);

                    annotated.getParameters().stream()
                            .map(o -> CTypeUtil.resolve(TypeElementUtil.toCodeType(o.asType(), elements)))
                            .forEach(typeList::add);

                    CodeType rtype = CTypeUtil.resolve(TypeElementUtil.fromGenericMirror(annotated.getReturnType()));

                    ptypes = typeList.toArray(new CodeType[typeList.size()]);
                    baseRetType = rtype;
                    break;
                }
                default: {
                    String methodRef = resolvedMethodRef == null ? "?" : resolvedMethodRef._1().toMethodString();

                    throw new IllegalArgumentException("Cannot find referenced method '" + methodRef + "'!");
                }
            }

            resolvedMethodRef = AptResolver.resolveMethodRef(unifiedMethodRef, baseRetType, ptypes, elements);

            if (resolvedMethodRef == null && type == Type.DEFAULT_IMPL) {
                resolvedMethodRef = MethodRefValidator.resolveThis(unifiedMethodRef, annotated, elements);
                if (resolvedMethodRef != null)
                    isThis = true;
            }

        }

        if (resolvedMethodRef == null) {
            throw new IllegalArgumentException("Invalid provided method reference!");
        } else if (resolvedMethodRef._2() == null) {
            String additional = first == resolvedMethodRef || first == null ? "" : "(or '" + first._1().toMethodString() + "')";

            throw new IllegalArgumentException("Cannot find referenced method '" + resolvedMethodRef._1().toMethodString() + "'" + additional + "!");
        } else {
            MethodTypeSpec spec = resolvedMethodRef._1();

            ExecutableElement executableElement = resolvedMethodRef._2();

            CodeType localization = spec.getLocalization();

            CodeType concreteType = CodeTypes.getConcreteType(localization);

            try {
                concreteType = CodeAPI.getJavaType(TypeInfo.resolveClass(concreteType.getCanonicalName()));
            } catch (Throwable ignored) {
            }

            if (isInline) {
                if (!(concreteType instanceof LoadedCodeType<?>)) {
                    throw new ReferencedMethodException("Referenced inline method '" + spec.toMethodString() + "' is not compiled yet. This method cannot be inlined!", executableElement);
                }
            }

            if (!isThis && (!executableElement.getModifiers().contains(Modifier.PUBLIC)
                    || !executableElement.getModifiers().contains(Modifier.STATIC))) {
                throw new ReferencedMethodException("Referenced method '" + spec.toMethodString() + "' must be public and static!", executableElement);
            }

        }

        return new MethodRefSpec(isThis, isInline, resolvedMethodRef._1());
    }

    private static Pair<MethodTypeSpec, ExecutableElement> resolveThis(UnifiedMethodRef unifiedMethodRef, ExecutableElement annotated, Elements elements) {

        CodeType[] ptypes = annotated.getParameters().stream()
                .map(o -> TypeElementUtil.toCodeType(o.asType(), elements))
                .toArray(CodeType[]::new);

        CodeType rtype = TypeElementUtil.toCodeType(annotated.getReturnType(), elements);

        Pair<MethodTypeSpec, ExecutableElement> resolve = AptResolver.resolveMethodRef(unifiedMethodRef, rtype, ptypes, elements);

        if (resolve != null && resolve._1() != null && resolve._2() == null) {
            String methodName = resolve._1().getMethodName();

            if (methodName.startsWith(":") && methodName.length() > 1) {

                UnifiedMethodRef map = UnifiedAnnotationsUtilKt.map(unifiedMethodRef, stringObjectMap -> {
                    stringObjectMap.put("name", methodName.substring(1));
                    return Unit.INSTANCE;
                });

                return AptResolver.resolveMethodRef(map, rtype, ptypes, elements);
            }
        }

        return null;
    }


    public enum Type {
        /**
         * Validate {@link PropertyInfo#validator()} annotation.
         */
        VALIDATOR,

        /**
         * Validate {@link PropertyInfo#defaultValue()} annotation.
         */
        DEFAULT_VALUE,

        /**
         * Validate {@link com.github.jonathanxd.buildergenerator.annotation.DefaultImpl}
         * annotation.
         */
        DEFAULT_IMPL
    }

    static class ReferencedMethodException extends RuntimeException {

        private final ExecutableElement referencedMethod;

        public ReferencedMethodException(ExecutableElement referencedMethod) {
            super();
            this.referencedMethod = referencedMethod;
        }

        public ReferencedMethodException(@NonNls String message, ExecutableElement referencedMethod) {
            super(message);
            this.referencedMethod = referencedMethod;
        }

        public ReferencedMethodException(String message, Throwable cause, ExecutableElement referencedMethod) {
            super(message, cause);
            this.referencedMethod = referencedMethod;
        }

        public ReferencedMethodException(Throwable cause, ExecutableElement referencedMethod) {
            super(cause);
            this.referencedMethod = referencedMethod;
        }

        protected ReferencedMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ExecutableElement referencedMethod) {
            super(message, cause, enableSuppression, writableStackTrace);
            this.referencedMethod = referencedMethod;
        }

        public ExecutableElement getReferencedMethod() {
            return this.referencedMethod;
        }
    }

}
