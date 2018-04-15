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

import com.github.jonathanxd.buildergenerator.annotation.DefaultImpl;
import com.github.jonathanxd.buildergenerator.annotation.GenBuilder;
import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.annotation.MethodRef;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.annotation.Validator;
import com.github.jonathanxd.buildergenerator.unification.UnificationFactory;
import com.github.jonathanxd.buildergenerator.unification.UnifiedDefaultImpl;
import com.github.jonathanxd.buildergenerator.unification.UnifiedGenBuilder;
import com.github.jonathanxd.buildergenerator.unification.UnifiedInline;
import com.github.jonathanxd.buildergenerator.unification.UnifiedMethodRef;
import com.github.jonathanxd.buildergenerator.unification.UnifiedPropertyInfo;
import com.github.jonathanxd.buildergenerator.unification.UnifiedValidator;
import com.github.jonathanxd.kores.extra.UnifiedAnnotation;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.type.ModelKoresTypesKt;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

public final class AnnotatedConstructUtil {

    private static final Map<Class<?>, Type> UNIFICATION_MAP = new HashMap<>();

    static {
        UNIFICATION_MAP.put(UnifiedDefaultImpl.class, DefaultImpl.class);
        UNIFICATION_MAP.put(UnifiedGenBuilder.class, GenBuilder.class);
        UNIFICATION_MAP.put(UnifiedInline.class, Inline.class);
        UNIFICATION_MAP.put(UnifiedMethodRef.class, MethodRef.class);
        UNIFICATION_MAP.put(UnifiedPropertyInfo.class, PropertyInfo.class);
        UNIFICATION_MAP.put(UnifiedValidator.class, Validator.class);
    }

    private final Elements elements;

    public AnnotatedConstructUtil(Elements elements) {
        this.elements = elements;
    }

    /**
     * Gets the {@link UnifiedAnnotation} corresponding to {@code annotationName} in {@link AnnotatedConstruct annotated
     * element}.
     *
     * @param annotatedConstruct Annotated element.
     * @param annotationType     Annotation type.
     * @param type               Type of unification class.
     * @return {@link Optional} of found {@link AnnotationMirror}, or empty {@link Optional} if not found.
     */
    public static <T extends UnifiedAnnotation> Optional<T> getUnifiedAnnotation(
            AnnotatedConstruct annotatedConstruct, Type annotationType, Class<T> type,
            Elements elements) {

        for (AnnotationMirror annotationMirror : annotatedConstruct.getAnnotationMirrors()) {
            Type kType = ModelKoresTypesKt
                    .getKoresType(annotationMirror.getAnnotationType(), elements);

            if (ImplicitKoresType.is(kType, annotationType)) {
                return Optional.of(UnificationFactory.create(annotationMirror, type, elements));
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the {@link UnifiedAnnotation} corresponding to {@code annotationName} in {@link AnnotatedConstruct annotated
     * element}.
     *
     * @param annotatedConstruct Annotated element.
     * @param type               Type of unification class.
     * @return {@link Optional} of found {@link AnnotationMirror}, or empty {@link Optional} if not found.
     */
    public static <T extends UnifiedAnnotation> Optional<T> getUnifiedAnnotation(
            AnnotatedConstruct annotatedConstruct, Class<T> type,
            Elements elements) {

        Type annotationType = Objects.requireNonNull(UNIFICATION_MAP.get(type),
                "Missing annotation type for unification type '" + type.getCanonicalName() + "'!");

        for (AnnotationMirror annotationMirror : annotatedConstruct.getAnnotationMirrors()) {
            Type kType = ModelKoresTypesKt
                    .getKoresType(annotationMirror.getAnnotationType(), elements);

            if (ImplicitKoresType.is(kType, annotationType)) {
                return Optional.of(UnificationFactory.create(annotationMirror, type, elements));
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the {@link UnifiedAnnotation} corresponding to {@code annotationName} in {@link AnnotatedConstruct annotated
     * element}.
     *
     * @param annotatedConstruct Annotated element.
     * @param annotationType     Annotation type.
     * @param type               Type of unification class.
     * @return {@link Optional} of found {@link AnnotationMirror}, or empty {@link Optional} if not found.
     */
    public <T extends UnifiedAnnotation> Optional<T> getUnifiedAnnotation(
            AnnotatedConstruct annotatedConstruct, Type annotationType, Class<T> type) {
        return AnnotatedConstructUtil.getUnifiedAnnotation(annotatedConstruct, annotationType, type,
                this.elements);
    }

    /**
     * Gets the {@link UnifiedAnnotation} corresponding to {@code annotationName} in {@link AnnotatedConstruct annotated
     * element}.
     *
     * @param annotatedConstruct Annotated element.
     * @param type               Type of unification class.
     * @return {@link Optional} of found {@link AnnotationMirror}, or empty {@link Optional} if not found.
     */
    public <T extends UnifiedAnnotation> Optional<T> getUnifiedAnnotation(
            AnnotatedConstruct annotatedConstruct, Class<T> type) {
        return AnnotatedConstructUtil.getUnifiedAnnotation(annotatedConstruct, type, this.elements);
    }
}
