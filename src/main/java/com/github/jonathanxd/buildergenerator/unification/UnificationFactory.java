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
package com.github.jonathanxd.buildergenerator.unification;

import com.github.jonathanxd.buildergenerator.annotation.Default;
import com.github.jonathanxd.buildergenerator.annotation.DefaultImpl;
import com.github.jonathanxd.buildergenerator.annotation.GenBuilder;
import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.annotation.MethodRef;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.annotation.Validator;
import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.extra.AnnotationsKt;
import com.github.jonathanxd.codeapi.extra.UnifiedAnnotation;
import com.github.jonathanxd.codeapi.type.CodeType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;


// TODO: Doc
public class UnificationFactory {

    private static final Map<CodeType, Class<?>> UNIFICATION_CLASSES = new HashMap<>();

    static {
        UNIFICATION_CLASSES.put(CodeAPI.getJavaType(DefaultImpl.class), UnifiedDefaultImpl.class);
        UNIFICATION_CLASSES.put(CodeAPI.getJavaType(GenBuilder.class), UnifiedGenBuilder.class);
        UNIFICATION_CLASSES.put(CodeAPI.getJavaType(Inline.class), UnifiedInline.class);
        UNIFICATION_CLASSES.put(CodeAPI.getJavaType(MethodRef.class), UnifiedMethodRef.class);
        UNIFICATION_CLASSES.put(CodeAPI.getJavaType(PropertyInfo.class), UnifiedPropertyInfo.class);
        UNIFICATION_CLASSES.put(CodeAPI.getJavaType(Validator.class), UnifiedValidator.class);
    }


    public static <T, R extends UnifiedAnnotation> R create(T annotation, Class<R> unification) {
        return AnnotationsKt.getUnificationInstance(annotation, unification, UnificationClassFunction.INSTANCE);
    }


    // Model

    public static UnifiedDefaultImpl createDefaultImpl(AnnotationMirror annotation) {
        return UnificationFactory.create(annotation, UnifiedDefaultImpl.class);
    }

    public static UnifiedGenBuilder createGenBuilder(AnnotationMirror annotation) {
        return UnificationFactory.create(annotation, UnifiedGenBuilder.class);
    }

    public static UnifiedInline createInline(AnnotationMirror annotation) {
        return UnificationFactory.create(annotation, UnifiedInline.class);
    }

    public static UnifiedMethodRef createMethodRef(AnnotationMirror annotation) {
        return UnificationFactory.create(annotation, UnifiedMethodRef.class);
    }

    public static UnifiedPropertyInfo createPropertyInfo(AnnotationMirror annotation) {
        return UnificationFactory.create(annotation, UnifiedPropertyInfo.class);
    }

    public static UnifiedValidator createValidator(AnnotationMirror annotation) {
        return UnificationFactory.create(annotation, UnifiedValidator.class);
    }

    // Java

    public static UnifiedDefaultImpl create(DefaultImpl annotation) {
        return UnificationFactory.create(annotation, UnifiedDefaultImpl.class);
    }

    public static UnifiedGenBuilder create(GenBuilder annotation) {
        return UnificationFactory.create(annotation, UnifiedGenBuilder.class);
    }

    public static UnifiedInline create(Inline annotation) {
        return UnificationFactory.create(annotation, UnifiedInline.class);
    }

    public static UnifiedMethodRef create(MethodRef annotation) {
        return UnificationFactory.create(annotation, UnifiedMethodRef.class);
    }

    public static UnifiedPropertyInfo create(PropertyInfo annotation) {
        return UnificationFactory.create(annotation, UnifiedPropertyInfo.class);
    }

    public static UnifiedValidator create(Validator annotation) {
        return UnificationFactory.create(annotation, UnifiedValidator.class);
    }

    private static class UnificationClassFunction implements Function1<CodeType, Class<?>> {

        public static final UnificationClassFunction INSTANCE = new UnificationClassFunction();

        @Override
        public Class<?> invoke(CodeType codeType) {

            return UNIFICATION_CLASSES.get(codeType);
        }
    }

    private static class UnificationDefaultAsNull implements Function3<String, Object, CodeType, Object> {

        @Override
        public Object invoke(String s, Object o, CodeType codeType) {
            if(o instanceof UnifiedDefaultImpl) {
                if(Default.isDefaultDefaultImpl((UnifiedDefaultImpl) o))
                    return null;
            }

            if(o instanceof UnifiedMethodRef) {
                if(Default.isDefaultMethodRef((UnifiedMethodRef) o))
                    return null;
            }

            if(o instanceof UnifiedValidator) {
                if(Default.isDefaultValidator((UnifiedValidator) o))
                    return null;
            }

            return o;
        }
    }


}
