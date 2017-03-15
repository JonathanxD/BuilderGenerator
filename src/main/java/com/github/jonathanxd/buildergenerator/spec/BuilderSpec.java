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

import com.github.jonathanxd.codeapi.type.CodeType;

import java.util.List;
import java.util.Optional;

/**
 * Specification of the builder.
 */
public final class BuilderSpec {

    /**
     * Builder name.
     */
    private final String builderQualifiedName;

    /**
     * Factory class.
     */
    private final CodeType factoryClass;

    /**
     * Factory result type (base class implementation/builder result)
     */
    private final CodeType factoryResultType;

    /**
     * Name of the factory method.
     */
    private final String factoryMethodName;

    /**
     * Base class.
     */
    private final CodeType baseClass;

    /**
     * Builder base class (inner class of base class)
     */
    private final CodeType builderBaseClass;

    /**
     * Properties to generate builder.
     */
    private final List<PropertySpec> properties;

    /**
     * Specification of non-property methods.
     */
    private final List<MethodSpec> methodSpecs;

    /**
     * Construct builder specification.
     *
     * @param builderQualifiedName Builder qualified name.
     * @param factoryClass         Factory class.
     * @param factoryResultType    Factory result type (base class implementation/builder result)
     * @param factoryMethodName    Name of the factory method.
     * @param baseClass            Base class.
     * @param properties           Properties to generate builder.
     * @param methodSpecs          Non-property method specification.
     */
    public BuilderSpec(String builderQualifiedName, CodeType factoryClass, CodeType factoryResultType, String factoryMethodName, CodeType baseClass, CodeType builderBaseClass, List<PropertySpec> properties, List<MethodSpec> methodSpecs) {
        this.builderQualifiedName = builderQualifiedName;
        this.factoryClass = factoryClass;
        this.factoryResultType = factoryResultType;
        this.factoryMethodName = factoryMethodName;
        this.baseClass = baseClass;
        this.builderBaseClass = builderBaseClass;
        this.properties = properties;
        this.methodSpecs = methodSpecs;
    }

    /**
     * Gets the builder qualified name.
     *
     * @return Builder qualified name.
     */
    public String getBuilderQualifiedName() {
        return this.builderQualifiedName;
    }

    /**
     * Gets the factory class.
     *
     * @return Factory class.
     */
    public CodeType getFactoryClass() {
        return this.factoryClass;
    }

    /**
     * Gets the factory result type/builder result/base class implementation.
     *
     * @return Factory result type/builder result/base class implementation.
     */
    public CodeType getFactoryResultType() {
        return this.factoryResultType;
    }

    /**
     * Gets the method factory name.
     *
     * @return {@link Optional} of method factory name or an empty {@link Optional} if the method is
     * a constructor.
     */
    public Optional<String> getFactoryMethodName() {
        return Optional.ofNullable(this.factoryMethodName);
    }

    /**
     * Gets the base class.
     *
     * @return Base class.
     */
    public CodeType getBaseClass() {
        return this.baseClass;
    }

    /**
     * Gets the 'Builder' base class (inner class of base class).
     *
     * @return 'Builder' base class (inner class of base class)..
     */
    public CodeType getBuilderBaseClass() {
        return this.builderBaseClass;
    }

    /**
     * Gets property list.
     *
     * @return Property list.
     */
    public List<PropertySpec> getProperties() {
        return this.properties;
    }

    /**
     * Gets the method specification of non-property methods.
     *
     * @return Method specification of non-property methods.
     */
    public List<MethodSpec> getMethodSpecs() {
        return this.methodSpecs;
    }
}
