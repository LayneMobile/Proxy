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

package com.laynemobile.proxy.model.output;

import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sourcerer.processor.Env;

public class ProxyFunctionTypeOutputStub extends AbstractTypeElementOutputStub {
    private final ProxyFunctionAbstractTypeOutput parentOutput;
    private final TypeMirror superClass;

    ProxyFunctionTypeOutputStub(ProxyFunctionAbstractTypeOutput parentOutput, Env env) {
        super(parentOutput.source().basePackageName() + ".templates", parentOutput.source().baseClassName());
        this.parentOutput = parentOutput;
        this.superClass = parentOutput.element(env).asType();
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
        classBuilder = classBuilder.superclass(TypeName.get(superClass))
                .addModifiers(Modifier.PUBLIC);

        ProxyFunctionAbstractTypeOutputStub stub = parentOutput.source();
        List<TypeVariableAlias> typeVariables = Util.buildList(stub.parent().element().getTypeParameters(),
                new Util.Transformer<TypeVariableAlias, TypeParameterElementAlias>() {
                    @Override
                    public TypeVariableAlias transform(TypeParameterElementAlias typeParameterElementAlias) {
                        TypeMirrorAlias type = typeParameterElementAlias.asType();
                        if (type.getKind() == TypeKind.TYPEVAR) {
                            return AliasTypes.get((TypeVariable) type.actual());
                        }
                        return null;
                    }
                });

        for (TypeVariableAlias typeVariable : typeVariables) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeVariable.actual()));
        }

        ProxyFunctionElement function = stub.function();
        String name = function.name();

        // Constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(function.functionType()), name)
                .addStatement("super($L)", name)
                .build());

        return classBuilder.build();
    }
}
