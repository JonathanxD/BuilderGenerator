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

import com.github.jonathanxd.buildergenerator.util.AnnotationMirrorUtil;
import com.github.jonathanxd.codeapi.base.Annotation;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

public final class AnnotationMirrorHelper {

    private final AnnotationMirror annotationMirror;
    private final Annotation annotation;

    public AnnotationMirrorHelper(AnnotationMirror annotationMirror, Elements elements) {
        this.annotationMirror = annotationMirror;
        this.annotation = (Annotation) AnnotationMirrorUtil.toCodeAPI(annotationMirror, elements);
    }

    /**
     * Gets annotation property value.
     *
     * The returned value matches the {@link AnnotationMirrorUtil#toCodeAPI(Object, Elements)}
     * conversion specification.
     *
     * @param name Annotation property name.
     * @return {@link Optional} of the value if the property is present, empty {@link Optional}
     * otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String name) {
        return Optional.ofNullable((T) this.annotation.getValues().get(name));
    }



    /**
     * {@link #get(String)}
     *
     * @param name Annotation property name.
     * @return Property value.
     * @throws NullPointerException If no one property with {@code name} is present.
     * @see #get(String)
     */
    public Object getUnsafe(String name) throws NullPointerException {
        return this.get(name).orElseThrow(() -> new NullPointerException("Required property '" + name + "' cannot be found in annotation '" + this.annotation + "' (Mirror: " + this.annotationMirror + ")"));
    }


    public AnnotationMirror getAnnotationMirror() {
        return this.annotationMirror;
    }
}
