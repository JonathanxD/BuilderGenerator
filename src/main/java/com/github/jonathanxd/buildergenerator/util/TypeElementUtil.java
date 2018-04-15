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

import com.github.jonathanxd.iutils.type.TypeUtil;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.KoresTypes;
import com.github.jonathanxd.kores.type.ModelKoresTypesKt;
import com.github.jonathanxd.kores.util.GenericTypeUtil;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * {@link TypeElement} and {@link TypeMirror} conversion to {@link KoresType}.
 */
public class TypeElementUtil {


    /**
     * Convert {@link TypeMirror} to {@link KoresType}.
     *
     * @param typeMirror Type mirror.
     * @return {@link KoresType} corresponding to {@link TypeMirror}.
     */
    public static KoresType fromGenericMirror(TypeMirror typeMirror, Elements elements) {
        KoresType KoresType = ModelKoresTypesKt.toKoresType(typeMirror, false, elements);

        /*while (KoresType instanceof GenericType)
            KoresType = ((GenericType) KoresType).getKoresType();*/

        return KoresType;
    }

    /**
     * Convert {@link TypeMirror} to {@link KoresType}.
     *
     * @param typeMirror Type mirror.
     * @param elements   Element utils to resolve type.
     * @return {@link KoresType} corresponding to {@link TypeMirror}.
     */
    public static KoresType toKoresType(TypeMirror typeMirror, Elements elements) {
        return GenericTypeUtil.fromSourceString(typeMirror.toString(), new TypeResolver(elements));
    }

    /**
     * Convert {@link TypeElement} to {@link KoresType}.
     *
     * @param typeElement Type element.
     * @return {@link KoresType} corresponding to {@link TypeElement}.
     */
    public static KoresType toKoresType(TypeElement typeElement, Elements elements) {
        return GenericTypeUtil.fromSourceString(typeElement.getQualifiedName().toString(), s -> {
            try {
                return KoresTypes.getKoresType(TypeUtil.resolveClass(s));
            } catch (Exception e) {
                return ModelKoresTypesKt.getKoresType(typeElement, elements);
            }
        });
    }

    /**
     * Convert {@link TypeMirror} to {@link TypeElement}.
     *
     * @param typeMirror Type mirror.
     * @param elements   Element utils to resolve type.
     * @return Type element.
     * @see Elements#getTypeElement(CharSequence)
     */
    public static TypeElement toTypeElement(TypeMirror typeMirror, Elements elements) {
        return elements
                .getTypeElement(GenericTypeUtil.fromSourceString(typeMirror.toString(), new TypeResolver(elements))
                        .getCanonicalName());
    }
}
