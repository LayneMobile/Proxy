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

package com.laynemobile.proxy.model;

import com.laynemobile.proxy.annotations.Generated;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import sourcerer.processor.Env;

public abstract class AbstractTypeElementOutputStub extends AbstractTypeElementStub implements TypeElementOutputStub {
    protected AbstractTypeElementOutputStub(String packageName, String className) {
        super(packageName, className);
    }

    protected TypeSpec build(TypeSpec.Builder classBuilder) {
        throw new UnsupportedOperationException("must implement for default writeTo(env) implementation");
    }

    protected TypeSpec newTypeSpec() {
        return build(TypeSpec.classBuilder(className())
                .addAnnotation(Generated.class)
        );
    }

    @Override public TypeElementOutput writeTo(Env env) throws IOException {
        TypeSpec typeSpec = newTypeSpec();
        JavaFile javaFile = JavaFile.builder(packageName(), typeSpec)
                .build();
        env.log("writing %s -> \n%s", qualifiedName(), javaFile.toString());
        javaFile.writeTo(env.filer());
        return new AbstractTypeElementOutput(this, typeSpec) {
            @Override public boolean hasOutput() {
                return false;
            }
        };
    }
}
