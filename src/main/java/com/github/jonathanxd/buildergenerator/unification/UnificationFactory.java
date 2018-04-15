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
package com.github.jonathanxd.buildergenerator.unification;

import com.github.jonathanxd.buildergenerator.annotation.DefaultImpl;
import com.github.jonathanxd.buildergenerator.annotation.DefaultUtil;
import com.github.jonathanxd.buildergenerator.annotation.GenBuilder;
import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.annotation.MethodRef;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.annotation.Validator;
import com.github.jonathanxd.kores.extra.AnnotationsKt;
import com.github.jonathanxd.kores.extra.UnifiedAnnotation;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;


// TODO: Doc
public class UnificationFactory {

    private static final Map<Type, Class<?>> UNIFICATION_CLASSES = new HashMap<>();

    static {
        UNIFICATION_CLASSES.put(DefaultImpl.class, UnifiedDefaultImpl.class);
        UNIFICATION_CLASSES.put(GenBuilder.class, UnifiedGenBuilder.class);
        UNIFICATION_CLASSES.put(Inline.class, UnifiedInline.class);
        UNIFICATION_CLASSES.put(MethodRef.class, UnifiedMethodRef.class);
        UNIFICATION_CLASSES.put(PropertyInfo.class, UnifiedPropertyInfo.class);
        UNIFICATION_CLASSES.put(Validator.class, UnifiedValidator.class);
    }


    public static <T, R extends UnifiedAnnotation> R create(T annotation, Class<R> unification, Elements elements) {
        return AnnotationsKt.getUnificationInstance(annotation, unification, UnificationClassFunction.INSTANCE,
                elements);
    }


    // Model

    public static UnifiedDefaultImpl createDefaultImpl(AnnotationMirror annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedDefaultImpl.class, elements);
    }

    public static UnifiedGenBuilder createGenBuilder(AnnotationMirror annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedGenBuilder.class, elements);
    }

    public static UnifiedInline createInline(AnnotationMirror annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedInline.class, elements);
    }

    public static UnifiedMethodRef createMethodRef(AnnotationMirror annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedMethodRef.class, elements);
    }

    public static UnifiedPropertyInfo createPropertyInfo(AnnotationMirror annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedPropertyInfo.class, elements);
    }

    public static UnifiedValidator createValidator(AnnotationMirror annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedValidator.class, elements);
    }

    // Java

    public static UnifiedDefaultImpl create(DefaultImpl annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedDefaultImpl.class, elements);
    }

    public static UnifiedGenBuilder create(GenBuilder annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedGenBuilder.class, elements);
    }

    public static UnifiedInline create(Inline annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedInline.class, elements);
    }

    public static UnifiedMethodRef create(MethodRef annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedMethodRef.class, elements);
    }

    public static UnifiedPropertyInfo create(PropertyInfo annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedPropertyInfo.class, elements);
    }

    public static UnifiedValidator create(Validator annotation, Elements elements) {
        return UnificationFactory.create(annotation, UnifiedValidator.class, elements);
    }

    private static class UnificationClassFunction implements Function1<Type, Class<?>> {

        public static final UnificationClassFunction INSTANCE = new UnificationClassFunction();

        @Override
        public Class<?> invoke(Type type) {
            for (Map.Entry<Type, Class<?>> typeClassEntry : UNIFICATION_CLASSES.entrySet()) {
                if (ImplicitKoresType.is(typeClassEntry.getKey(), type))
                    return typeClassEntry.getValue();
            }

            return null;
        }
    }

    private static class UnificationDefaultAsNull implements Function3<String, Object, Type, Object> {

        @Override
        public Object invoke(String s, Object o, Type type) {
            if (o instanceof UnifiedDefaultImpl) {
                if (DefaultUtil.isDefaultDefaultImpl((UnifiedDefaultImpl) o))
                    return null;
            }

            if (o instanceof UnifiedMethodRef) {
                if (DefaultUtil.isDefaultMethodRef((UnifiedMethodRef) o))
                    return null;
            }

            if (o instanceof UnifiedValidator) {
                if (DefaultUtil.isDefaultValidator((UnifiedValidator) o))
                    return null;
            }

            return o;
        }
    }


}
