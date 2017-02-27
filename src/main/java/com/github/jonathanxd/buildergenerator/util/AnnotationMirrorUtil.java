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

import com.github.jonathanxd.codeapi.builder.AnnotationBuilder;
import com.github.jonathanxd.codeapi.builder.EnumValueBuilder;
import com.github.jonathanxd.codeapi.util.GenericTypeUtil;
import com.github.jonathanxd.iutils.function.collector.BiCollectors;
import com.github.jonathanxd.iutils.function.stream.MapStream;
import com.github.jonathanxd.iutils.object.Node;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class AnnotationMirrorUtil {


    /**
     * Convert annotation value {@code o} to CodeAPI annotation style values.
     *
     * Returns {@link String} or a boxed version of primitive value for Java Literals, {@link
     * com.github.jonathanxd.codeapi.type.CodeType} (normally {@link com.github.jonathanxd.codeapi.type.GenericType})
     * for Java types, {@link com.github.jonathanxd.codeapi.base.EnumValue} for enum constants,
     * {@link com.github.jonathanxd.codeapi.base.Annotation} for Java annotations or a {@link List}
     * of one of the preceding types (list represents arrays, which elements appear in their
     * declaration order).
     *
     * @param o        Javax Model element.
     * @param elements Elements utils
     * @return CodeAPI Object representing the annotation value or a List of CodeAPI Objects
     * representing an Array value.
     */
    @SuppressWarnings("unchecked")
    public static Object toCodeAPI(Object o, Elements elements) {

        if (o instanceof String
                || o instanceof Boolean
                || o instanceof Byte
                || o instanceof Short
                || o instanceof Character
                || o instanceof Integer
                || o instanceof Float
                || o instanceof Double
                || o instanceof Long)
            return o;

        if (o instanceof TypeMirror)
            return GenericTypeUtil.fromSourceString(o.toString(), new TypeResolver(elements));

        if (o instanceof VariableElement) {
            VariableElement variableElement = (VariableElement) o;

            return EnumValueBuilder.builder()
                    .withEnumType(TypeElementUtil.toCodeType(variableElement.asType(), elements))
                    .withEnumEntry(variableElement.getSimpleName().toString())
                    .build();
        }

        if (o instanceof AnnotationMirror) {
            AnnotationMirror mirror = (AnnotationMirror) o;

            return AnnotationBuilder.builder()
                    .withType(TypeElementUtil.toCodeType(mirror.getAnnotationType(), elements))
                    .withValues(
                            MapStream.of(mirror.getElementValues())
                                    .map((eElement, aValue) -> new Node<>(eElement.getSimpleName().toString(), AnnotationMirrorUtil.toCodeAPI(aValue.getValue(), elements)))
                                    .collect(BiCollectors.toMap()))
                    .build();
        }

        if (o instanceof List<?>)
            return ((List<? extends AnnotationValue>) o).stream().map(o1 -> AnnotationMirrorUtil.toCodeAPI(o1.getValue(), elements)).toArray(Object[]::new);

        throw new IllegalArgumentException("Cannot convert '" + o + "' to object");

    }

}
