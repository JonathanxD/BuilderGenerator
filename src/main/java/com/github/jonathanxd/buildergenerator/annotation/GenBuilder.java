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
package com.github.jonathanxd.buildergenerator.annotation;

import com.github.jonathanxd.iutils.object.Default;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates a builder.
 *
 * Only classes, constructors and static factory methods is valid.
 *
 * If a class is annotated, a constructor is required. (Constructor with more parameters will be
 * used).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface GenBuilder {

    /**
     * Base class.
     *
     * The base class MUST have an inner-class interface 'Builder' that extends {@link
     * com.github.jonathanxd.buildergenerator.Builder}.
     *
     * If this annotation is present in a constructor, this property must be defined, if the
     * annotation is present in a factory method, the return type will be used as base class.
     *
     * @return Base class.
     */
    Class<?> base() default Default.class;

    /**
     * Qualified name of the builder.
     *
     * If this property is not defined, {@link com.github.jonathanxd.buildergenerator.apt.AnnotationProcessor}
     * will use the {@code factory result type} name as a base name and create a sub-package
     * 'builder' in the {@code factory result type} package.
     *
     * @return Qualified name of the builder.
     */
    String qualifiedName() default "";

}
