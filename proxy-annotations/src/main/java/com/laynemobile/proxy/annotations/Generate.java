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

package com.laynemobile.proxy.annotations;

import com.laynemobile.proxy.Builder;
import com.laynemobile.proxy.functions.AbstractProxyFunction;
import com.laynemobile.proxy.processor.ProcessorHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Generate {
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface ProxyBuilder {
        boolean parent() default false;

        Class<?>[] dependsOn() default {};

        Class<?> replaces() default Object.class;

        Class<?> extendsFrom() default Object.class;
    }

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface ProxyFunction {
        String value() default "";
    }

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @interface ProxyFunctionImplementation {
        Class<? extends AbstractProxyFunction> value();
    }

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    @interface ProcessorBuilder {
        Class<? extends Builder<? extends ProcessorHandler.Parent<?, ?, ?>>> value();

        Class<? extends Builder<? extends ProcessorHandler<?, ?, ?>>>[] extensions() default {};
    }
}
