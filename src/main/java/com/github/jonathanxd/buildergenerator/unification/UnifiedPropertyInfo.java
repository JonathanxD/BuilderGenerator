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

import com.github.jonathanxd.buildergenerator.annotation.GenBuilder;
import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.spec.PropertySpec;
import com.github.jonathanxd.codeapi.extra.UnifiedAnnotation;

/**
 * Property specification.
 *
 * To specify specification, annotated the {@code with} method of the {@code Builder} inner class of
 * the {@link GenBuilder#base() base class}.
 *
 * Unification version of {@link com.github.jonathanxd.buildergenerator.annotation.PropertyInfo}
 */
public interface UnifiedPropertyInfo extends UnifiedAnnotation {

    /**
     * Is property nullable (can receive null values).
     *
     * This property will be ignored if the property is of Optional type.
     *
     * @return Is property nullable (can receive null values).
     */
    boolean isNullable();

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
    String defaultsPropertyName();

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
    UnifiedMethodRef defaultValue();

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
    UnifiedValidator validator();


}
