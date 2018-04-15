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
import com.github.jonathanxd.buildergenerator.annotation.MethodRef;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;
import com.github.jonathanxd.buildergenerator.annotation.Validator;
import com.github.jonathanxd.kores.extra.UnifiedAnnotation;

/**
 * Marks the {@link Validator} or {@link MethodRef} to be inlined.
 *
 * This operation depends on the implementation of the {@link com.github.jonathanxd.buildergenerator.BuilderGenerator},
 * the default generator will require that method return a {@link com.github.jonathanxd.kores.Instruction}.
 *
 * For {@link PropertyInfo#validator()} and {@link PropertyInfo#defaultValue()} a parameter of type
 * {@link com.github.jonathanxd.kores.base.VariableBase} is required, this parameter is the {@code
 * property info}
 *
 * For {@link PropertyInfo#validator()}, a second parameter is required, this parameter must be of
 * {@link com.github.jonathanxd.kores.Instruction} type, this parameter is the access to value to
 * validate.
 *
 * For {@link PropertyInfo#defaultValue()} no additional parameters is required.
 *
 * For {@link DefaultImpl#value()} two parameters are required, first is the {@code annotatedMethod}
 * and the type is {@link com.github.jonathanxd.kores.base.MethodDeclaration}, the second is
 * {@code parameters}, the type is a {@link java.util.List} of {@link
 * com.github.jonathanxd.kores.Instruction}.
 *
 * Note: This annotation only works for compiled methods, {@link com.github.jonathanxd.buildergenerator.apt.MethodRefValidator}
 * will throw a exception if a non-compiled method is referenced from a {@link MethodRef}
 * annotation.
 */
public interface UnifiedInline extends UnifiedAnnotation {
}
