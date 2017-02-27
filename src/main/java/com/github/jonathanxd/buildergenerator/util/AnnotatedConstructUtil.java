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

import java.util.Optional;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;

public final class AnnotatedConstructUtil {

    private AnnotatedConstructUtil() {
        throw new IllegalStateException();
    }

    /**
     * Gets the {@link AnnotationMirror} corresponding to {@code annotationName} in {@link
     * AnnotatedConstruct annotated element}.
     *
     * @param annotatedConstruct Annotated element.
     * @param annotationName     Annotation type name (full qualified).
     * @return {@link Optional} of found {@link AnnotationMirror}, or empty {@link Optional} if not
     * found.
     */
    public static Optional<AnnotationMirror> getAnnotationMirror(AnnotatedConstruct annotatedConstruct, String annotationName) {
        for (AnnotationMirror annotationMirror : annotatedConstruct.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotationName))
                return Optional.of(annotationMirror);
        }

        return Optional.empty();
    }
}