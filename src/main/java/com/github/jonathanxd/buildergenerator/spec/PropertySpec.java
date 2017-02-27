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
package com.github.jonathanxd.buildergenerator.spec;

import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.type.CodeType;

import java.util.Optional;

/**
 * Specification of a property.
 */
public final class PropertySpec {

    /**
     * Property name
     */
    private final String name;

    /**
     * Property type.
     */
    private final CodeType type;

    /**
     * Is property nullable (always true for {@link Optional} properties)
     */
    private final boolean isNullable;

    /**
     * Is property optional.
     */
    private final boolean isOptional;

    /**
     * Default value method provider.
     */
    private final MethodTypeSpec defaultValueSpec;

    /**
     * Validator provider.
     */
    private final MethodTypeSpec validatorSpec;

    public PropertySpec(String name, CodeType type, boolean isNullable, boolean isOptional, MethodTypeSpec defaultValueSpec, MethodTypeSpec validatorSpec) {
        this.name = name;
        this.type = type;
        this.isNullable = isNullable;
        this.isOptional = isOptional;
        this.defaultValueSpec = defaultValueSpec;
        this.validatorSpec = validatorSpec;
    }

    /**
     * Gets the property name.
     *
     * @return Property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the property type.
     *
     * @return Property type.
     */
    public CodeType getType() {
        return this.type;
    }

    /**
     * Returns true if the property is nullable (always true for {@link Optional} properties).
     *
     * @return True if the property is nullable (always true for {@link Optional} properties).
     */
    public boolean isNullable() {
        return this.isOptional() || this.isNullable;
    }

    /**
     * Returns true if the property is of type {@link Optional}.
     *
     * @return True if the property is of type {@link Optional}.
     */
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     * Gets default value provider specification.
     *
     * @return Default value provider specification.
     */
    public Optional<MethodTypeSpec> getDefaultValueSpec() {
        return Optional.ofNullable(this.defaultValueSpec);
    }

    /**
     * Gets the validator provider specification.
     *
     * @return Validator provider specification.
     */
    public Optional<MethodTypeSpec> getValidatorSpec() {
        return Optional.ofNullable(this.validatorSpec);
    }
}
