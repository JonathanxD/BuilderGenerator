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

import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.type.PlainCodeType;
import com.github.jonathanxd.codeapi.util.GenericTypeUtil;
import com.github.jonathanxd.iutils.type.TypeInfo;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class TypeElementUtil {


    public static CodeType toCodeType(TypeMirror typeMirror, Elements elements) {
        return GenericTypeUtil.fromSourceString(typeMirror.toString(), new TypeResolver(elements));
    }

    public static CodeType toCodeType(TypeElement typeElement) {
        return GenericTypeUtil.fromSourceString(typeElement.getQualifiedName().toString(), s -> {
            try {
                return CodeAPI.getJavaType(TypeInfo.resolveClass(s));
            } catch (Exception e) {
                return new PlainCodeType(s, typeElement.getKind() == ElementKind.INTERFACE);
            }
        });
    }

    public static TypeElement toTypeElement(TypeMirror typeMirror, Elements elements) {
        return elements.getTypeElement(GenericTypeUtil.fromSourceString(typeMirror.toString(), new TypeResolver(elements)).getCanonicalName());
    }
}
