/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laynemobile.api;

import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.transforms.Action2Transform;
import com.laynemobile.proxy.internal.ConsoleLogger;
import com.laynemobile.proxy.internal.ProxyLog;

import org.junit.Test;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import rx.Subscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TypeTokenTest {
    private static final String TAG = TypeTokenTest.class.getSimpleName();

    static {
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Test public void testPrimitiveTypeToken() throws Exception {
        TypeToken<Integer> intType = TypeToken.get(int.class);
        TypeToken<Integer> integerType = TypeToken.get(Integer.class);
        assertNotEquals(intType, integerType);
        assertNotEquals(intType.getRawType(), integerType.getRawType());
    }

    @Test public void testAbstractValueToken() throws Exception {
        AbstractValue<String> stringValue = new AbstractValue<String>("string") {};
        assertStringType(stringValue);
    }

    @Test public void testConcreteValueToken() throws Exception {
        // Fails
        AbstractValue<String> stringValue = new ConcreteValue<>("string");
        assertStringType(stringValue);
    }

    @Test public void testStringValue() throws Exception {
        AbstractValue<String> stringValue = new StringValue("string");
        assertStringType(stringValue);
    }

    @Test public void testSourceValue() throws Exception {
        Action2Transform<NoParams, Subscriber<? super String>> transform = new Action2Transform<>(new Action0() {
            @Override public void call() {

            }
        });

        SourceValue<String, NoParams> sourceValue = new SourceValue<String, NoParams>(transform) {};
        ProxyLog.d(TAG, "paramTypes: %s", Arrays.toString(sourceValue.functionTypes));
    }

    static void assertStringType(AbstractValue<String> value) {
        assertType(String.class, value);
    }

    static <T> void assertType(Class<T> expectedType, AbstractValue<T> value) {
        assertEquals(TypeToken.get(expectedType), value.typeToken);
    }

    interface Value<T> {
        T get();
    }

    static abstract class AbstractValue<T> implements Value<T> {
        final T value;
        final TypeToken<T> typeToken;

        AbstractValue(T value) {
            this.value = value;
            this.typeToken = TypeToken.getSuperTypeParameter(getClass(), AbstractValue.class);
            if (typeToken.getType() instanceof TypeVariable) {
                throw new IllegalStateException(
                        "must create concrete subclass or anonymous instance with actual type embedded");
            }
        }

        @Override public T get() {
            return value;
        }
    }

    static class ConcreteValue<T> extends AbstractValue<T> {
        ConcreteValue(T value) {
            super(value);
        }
    }

    static class StringValue extends AbstractValue<String> {
        StringValue(String value) {
            super(value);
        }
    }

    static abstract class SourceValue<T, P extends Params> extends AbstractValue<Action2Transform<P, Subscriber<? super T>>>
            implements Action2<P, Subscriber<? super T>> {
        final TypeToken<?>[] functionTypes;

        protected SourceValue(Action2Transform<P, Subscriber<? super T>> value) {
            super(value);
            this.functionTypes = TypeToken.getSuperInterfaceTypeParameters(getClass(), Action2.class);

            TypeToken<?>[] paramTypes = TypeToken.getTypeParameters(getClass());
            ProxyLog.d(TAG, "source paramTypes: %s", Arrays.toString(paramTypes));
        }

        @Override public void call(P p, Subscriber<? super T> subscriber) {
            value.call(p, subscriber);
        }
    }

    static abstract class SimpleSourceValue<T> extends SourceValue<T, NoParams> {
        protected SimpleSourceValue(Action2Transform<NoParams, Subscriber<? super T>> value) {
            super(value);
        }
    }
}
