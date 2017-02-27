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
package com.github.jonathanxd.buildergenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Property specifications.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PropertyInfo {

    /**
     * Is property nullable (can receive null values).
     *
     * This property will be ignored if the property is of Optional type.
     *
     * @return Is property nullable (can receive null values).
     */
    boolean isNullable() default false;

    /**
     * Default property value provider method.
     *
     * If the target method is annotated with {@link Inline} the method MUST follow the rules of
     * inlining.
     *
     * Unless the target method is annotated with {@link Inline}, it must return a value of a type
     * assignable to property type and take two parameters: property name of type {@link String} and
     * property type of type {@link Class}).
     *
     * @return Default value provider method.
     */
    MethodRef defaultValue() default @MethodRef(value = Default.class, name = "");

    /**
     * Validator specification.
     *
     * If the target method is annotated with {@link Inline} the method MUST follow the rules of
     * inlining.
     *
     * Unless the target method is annotated with {@link Inline}, it must have return void type take
     * three parameters: value to validate (type must be the same as the property type or a super
     * type of property type), property name of type {@link String} and Property type of type {@link
     * Class}.
     *
     * @return Validator specification.
     */
    Validator validator() default @Validator(value = @MethodRef(value = Default.class, name = ""));

}
