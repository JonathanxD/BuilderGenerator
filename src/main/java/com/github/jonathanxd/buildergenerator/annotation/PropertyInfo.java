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

import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.iutils.object.Default;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Property specification.
 *
 * To specify specification, annotated the {@code with} method of the {@code Builder} inner class of
 * the {@link GenBuilder#base() base class}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PropertyInfo {

    /**
     * Whether property is nullable or not.
     *
     * This property will be ignored if the property is of Optional type.
     *
     * @return Whether property nullable (can receive null values).
     */
    boolean isNullable() default false;

    /**
     * Name of property that should be used as default value.
     *
     * Name of the property where builder should extract the default value.
     *
     * {@code BuilderGenerator} generates a secondary constructor that extracts default values from
     * instance, the default the property name is the {@link PropertySpec#getName()}.
     *
     * @return Name of property that should be used as default value.
     */
    String defaultsPropertyName() default "";

    /**
     * Default property value provider method.
     *
     * If the target method is annotated with {@link Inline} the method must follow the rules of
     * inlining.
     *
     * Unless the target method is annotated with {@link Inline}, it must return a value of a type
     * assignable to property type and take two parameters: {@code property name} of type {@link
     * String} and {@code property type} of type {@link Class}).
     *
     * @return Default value provider method.
     */
    MethodRef defaultValue() default @MethodRef(value = Default.class, name = "");

    /**
     * Validator specification.
     *
     * If the target method is annotated with {@link Inline} the method must follow the rules of
     * inlining.
     *
     * Unless the target method is annotated with {@link Inline}, it must return {@code void} and
     * take three parameters: value to validate (type must be the same as the {@code property type}
     * or a super type of property type), {@code property name} of type {@link String} and {@code
     * property type} of type {@link Class}.
     *
     * @return Validator specification.
     */
    Validator validator() default @Validator(value = @MethodRef(value = Default.class, name = ""));

}
