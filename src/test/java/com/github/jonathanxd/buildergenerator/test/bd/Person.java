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
package com.github.jonathanxd.buildergenerator.test.bd;

import com.github.jonathanxd.buildergenerator.DefaultValues;
import com.github.jonathanxd.buildergenerator.Defaults;
import com.github.jonathanxd.buildergenerator.annotation.DefaultImpl;
import com.github.jonathanxd.buildergenerator.annotation.Inline;
import com.github.jonathanxd.buildergenerator.annotation.MethodRef;
import com.github.jonathanxd.buildergenerator.annotation.PropertyInfo;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public interface Person {
    String getName();

    int getAge();

    Person.Image getImage();

    Set<? extends Person> getParents();

    Type getType();

    interface Builder<T extends Person, S extends Person.Builder<T, S>> extends com.github.jonathanxd.buildergenerator.Builder<T, S> {

        @DefaultImpl(value = @MethodRef(value = Person.Builder.DefaultImpls.class, name = "withName", parameterTypes = {Person.Builder.class, Object.class}))
        S withName(Object o);

        @DefaultImpl(value = @MethodRef(value = Person.Builder.class, name = ":withName", parameterTypes = String.class))
        S withName(CharSequence sequence);

        @DefaultImpl(value = @MethodRef(value = Person.Builder.class, name = ":withName"))
        S name(String s);

        S withName(String name);

        S withAge(int age);

        @PropertyInfo(defaultValue = @MethodRef(value = DefaultValues.class, name = "empty"))
        S withImage(Person.Image imagem);

        S withParents(Set<? extends Person> parents);

        @DefaultImpl(@MethodRef(value = Defaults.class, name = "varArgToSet"))
        S withParents(Person... parents);

        S withType(Type type);

        public static class DefaultImpls {

            public static Person.Builder<Person, ?> withName(Person.Builder<Person, ?> builder, Object o) {
                return builder.withName((String) o);
            }

        }
    }


    class Image {
        public static final Person.Image EMPTY = new Person.Image(new byte[0]);

        private final byte[] image;

        public Image(byte[] image) {
            this.image = image;
        }

        public static Person.Image empty() {
            return Person.Image.EMPTY;
        }


        public byte[] getImage() {
            return this.image.clone();
        }
    }
}
