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
import com.github.jonathanxd.kores.type.PlainKoresType;
import com.github.jonathanxd.kores.util.KoresTypeResolverFunc;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Type resolver, resolves class literal.
 *
 * @see TypeUtil#resolveClass(String)
 */
public final class TypeResolver extends KoresTypeResolverFunc {

    private final Elements elements;

    public TypeResolver(Elements elements) {
        this.elements = elements;
    }

    @NotNull
    @Override
    protected KoresType resolve(String s) {
        try {
            return KoresTypes.getKoresType(TypeUtil.resolveClass(s));
        } catch (Exception e) {
            if (elements != null) {
                TypeElement typeElement = elements.getTypeElement(s);

                if (typeElement != null) {
                    return ModelKoresTypesKt.getKoresType(typeElement, this.elements);
                }
            }

            return new PlainKoresType(s, false);
        }
    }
}
