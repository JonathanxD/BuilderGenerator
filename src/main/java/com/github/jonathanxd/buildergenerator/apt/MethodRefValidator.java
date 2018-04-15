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
package com.github.jonathanxd.buildergenerator.apt;

import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.spec.MethodRefSpec;
import com.github.jonathanxd.buildergenerator.unification.UnifiedInline;
import com.github.jonathanxd.buildergenerator.unification.UnifiedMethodRef;
import com.github.jonathanxd.buildergenerator.util.AnnotatedConstructUtil;
import com.github.jonathanxd.buildergenerator.util.CTypeUtil;
import com.github.jonathanxd.buildergenerator.util.TypeElementUtil;
import com.github.jonathanxd.iutils.object.Pair;
import com.github.jonathanxd.iutils.type.TypeUtil;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.VariableBase;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.extra.AnnotationsKt;
import com.github.jonathanxd.kores.extra.UnifiedAnnotationsUtilKt;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.KoresTypes;
import com.github.jonathanxd.kores.type.LoadedKoresType;

import org.jetbrains.annotations.NonNls;

import java.lang.reflect.Type;
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
 * In the first step the validator tries to resolve the method using {@link com.github.jonathanxd.buildergenerator.annotation.Inline}
 * rules for {@link PropertyInfo#validator()} and {@link PropertyInfo#defaultValue()}.
 *
 * In the second step the validator tries to resolve the method using rules specified in {@link PropertyInfo#validator()} and
 * {@link PropertyInfo#defaultValue()}.
 *
 * In the third step, the validator will check if the method is public and static, and for {@link
 * com.github.jonathanxd.buildergenerator.annotation.Inline inline} methods, the validator will check if the method is compiled.
 *
 * Attention: Signature properties defined in {@link com.github.jonathanxd.buildergenerator.annotation.MethodRef MethodRef
 * Annotation} overrides signatures specified by the {@link PropertyInfo}.
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
    public static boolean validate(ExecutableElement annotated, UnifiedMethodRef methodRef,
                                   Messager messager, Elements elements, VType type) {

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
    public static MethodRefSpec get(ExecutableElement annotated, UnifiedMethodRef unifiedMethodRef,
                                    Elements elements, VType type) {

        KoresType insn = KoresTypes.getKoresType(Instruction.class);
        KoresType varBase = KoresTypes.getKoresType(VariableBase.class);

        Pair<MethodTypeSpec, ExecutableElement> resolvedMethodRef;
        boolean reqPropertyType = type == VType.VALIDATOR || type == VType.DEFAULT_VALUE;
        boolean isThis = false;

        if (reqPropertyType) {
            if (annotated.getParameters().size() != 1) {
                throw new IllegalArgumentException(
                        "Builder property method MUST have only one argument.");
            }
        }

        // Inline method resolver
        switch (type) {
            case VALIDATOR: {
                resolvedMethodRef = AptResolver
                        .resolveMethodRef(unifiedMethodRef, insn, new KoresType[]{varBase, insn},
                                elements);
                break;
            }
            case DEFAULT_VALUE: {
                resolvedMethodRef = AptResolver
                        .resolveMethodRef(unifiedMethodRef, insn, new KoresType[]{varBase},
                                elements);
                break;
            }
            case DEFAULT_IMPL: {
                if (!unifiedMethodRef.name().startsWith(":"))
                    resolvedMethodRef = AptResolver.resolveMethodRef(unifiedMethodRef, insn,
                            new KoresType[]{KoresTypes.getKoresType(
                                    MethodDeclaration.class), Types.LIST /* List<CodePart> */},
                            elements);
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
                && resolvedMethodRef.getSecond() != null
                && AnnotatedConstructUtil
                .getUnifiedAnnotation(resolvedMethodRef.getSecond(), Inline.class,
                        UnifiedInline.class, elements)
                .isPresent();

        if (resolvedMethodRef == null || resolvedMethodRef.getSecond() == null) {
            Type baseRetType;
            Type[] ptypes;

            Type propertyType = null;

            if (reqPropertyType)
                propertyType = TypeElementUtil
                        .toKoresType(annotated.getParameters().get(0).asType(), elements);

            switch (type) {
                case VALIDATOR: {
                    baseRetType = Types.VOID;
                    ptypes = new Type[]{propertyType, Types.STRING, Types.CLASS};
                    break;
                }
                case DEFAULT_VALUE: {
                    baseRetType = propertyType;
                    ptypes = new Type[]{Types.STRING, Types.CLASS};
                    break;
                }
                case DEFAULT_IMPL: {
                    TypeElement receiverType = (TypeElement) annotated.getEnclosingElement();
                    Type receiver = TypeElementUtil.toKoresType(receiverType, elements);

                    List<Type> typeList = new ArrayList<>();

                    typeList.add(receiver);

                    annotated.getParameters().stream()
                            .map(o -> CTypeUtil
                                    .resolve(TypeElementUtil.toKoresType(o.asType(), elements)))
                            .forEach(typeList::add);

                    Type rtype = CTypeUtil
                            .resolve(TypeElementUtil.fromGenericMirror(annotated.getReturnType(), elements));

                    ptypes = typeList.toArray(new Type[0]);
                    baseRetType = rtype;

                    if (unifiedMethodRef.name().startsWith(":")) {
                        resolvedMethodRef = MethodRefValidator
                                .resolveThis(unifiedMethodRef, annotated, elements);
                        if (resolvedMethodRef != null)
                            isThis = true;
                    }
                    break;
                }
                default: {
                    String methodRef = resolvedMethodRef == null ? "?" : resolvedMethodRef
                            .getFirst()
                            .toMethodString();

                    throw new IllegalArgumentException(
                            "Cannot find referenced method '" + methodRef + "'!");
                }
            }

            if (resolvedMethodRef == null || resolvedMethodRef.getSecond() == null) {
                resolvedMethodRef = AptResolver
                        .resolveMethodRef(unifiedMethodRef, baseRetType, ptypes, elements);
            }


        }

        if (resolvedMethodRef == null) {
            throw new IllegalArgumentException("Invalid provided method reference!");
        } else if (resolvedMethodRef.getSecond() == null) {
            String additional =
                    first == resolvedMethodRef || first == null
                    ? ""
                    : "(or '" + first.getFirst().toMethodString() + "')";

            throw new IllegalArgumentException(
                    "Cannot find referenced method '" + resolvedMethodRef.getFirst()
                            .toMethodString() + "'" + additional + "!");
        } else {
            MethodTypeSpec spec = resolvedMethodRef.getFirst();

            ExecutableElement executableElement = resolvedMethodRef.getSecond();

            Type localization = spec.getLocalization();

            KoresType concreteType = KoresTypes.getConcreteType(
                    KoresTypes.getKoresType(localization));

            try {
                concreteType = KoresTypes.getKoresType(
                        TypeUtil.resolveClass(concreteType.getCanonicalName()));
            } catch (Throwable ignored) {
            }

            if (isInline) {
                if (!(concreteType instanceof LoadedKoresType<?>)) {
                    throw new ReferencedMethodException("Referenced inline method '" + spec
                            .toMethodString() + "' is not compiled yet. This method cannot be inlined!",
                            executableElement);
                }
            }

            if (!isThis && (!executableElement.getModifiers().contains(Modifier.PUBLIC)
                    || !executableElement.getModifiers().contains(Modifier.STATIC))) {
                throw new ReferencedMethodException("Referenced method '" + spec
                        .toMethodString() + "' must be public and static!", executableElement);
            }

        }

        return new MethodRefSpec(isThis, isInline, resolvedMethodRef.getFirst());
    }

    private static Pair<MethodTypeSpec, ExecutableElement> resolveThis(
            UnifiedMethodRef unifiedMethodRef, ExecutableElement annotated, Elements elements) {

        KoresType[] ptypes = annotated.getParameters().stream()
                .map(o -> TypeElementUtil.toKoresType(o.asType(), elements))
                .toArray(KoresType[]::new);

        KoresType rtype = TypeElementUtil.toKoresType(annotated.getReturnType(), elements);

        String methodName = unifiedMethodRef.name();

        if (methodName.startsWith(":") && methodName.length() > 1) {

            UnifiedMethodRef map = UnifiedAnnotationsUtilKt
                    .map(unifiedMethodRef, stringObjectMap -> {
                        stringObjectMap.put("name", methodName.substring(1));
                        return Unit.INSTANCE;
                    });

            return AptResolver.resolveMethodRef(map, rtype, ptypes, elements);
        }

        return null;
    }


    public enum VType {
        /**
         * Validate {@link PropertyInfo#validator()} annotation.
         */
        VALIDATOR,

        /**
         * Validate {@link PropertyInfo#defaultValue()} annotation.
         */
        DEFAULT_VALUE,

        /**
         * Validate {@link com.github.jonathanxd.buildergenerator.annotation.DefaultImpl} annotation.
         */
        DEFAULT_IMPL
    }

    static class ReferencedMethodException extends RuntimeException {

        private final ExecutableElement referencedMethod;

        public ReferencedMethodException(ExecutableElement referencedMethod) {
            super();
            this.referencedMethod = referencedMethod;
        }

        public ReferencedMethodException(@NonNls String message,
                                         ExecutableElement referencedMethod) {
            super(message);
            this.referencedMethod = referencedMethod;
        }

        public ReferencedMethodException(String message, Throwable cause,
                                         ExecutableElement referencedMethod) {
            super(message, cause);
            this.referencedMethod = referencedMethod;
        }

        public ReferencedMethodException(Throwable cause, ExecutableElement referencedMethod) {
            super(cause);
            this.referencedMethod = referencedMethod;
        }

        protected ReferencedMethodException(String message, Throwable cause,
                                            boolean enableSuppression, boolean writableStackTrace,
                                            ExecutableElement referencedMethod) {
            super(message, cause, enableSuppression, writableStackTrace);
            this.referencedMethod = referencedMethod;
        }

        public ExecutableElement getReferencedMethod() {
            return this.referencedMethod;
        }
    }

}
