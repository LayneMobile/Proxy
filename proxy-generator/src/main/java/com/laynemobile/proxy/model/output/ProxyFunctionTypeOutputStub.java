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
import com.laynemobile.proxy.model.ProxyEnv;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import sourcerer.processor.Env;

import static javax.lang.model.util.ElementFilter.constructorsIn;

public class ProxyFunctionTypeOutputStub extends AbstractTypeElementOutputStub {
    private final Env env;
    private final ProxyFunctionAbstractTypeOutput parentOutput;
    private final DeclaredType superClass;

    ProxyFunctionTypeOutputStub(ProxyFunctionAbstractTypeOutput parentOutput, Env env) {
        super(parentOutput.source().basePackageName() + ".templates", parentOutput.source().baseClassName());
        this.parentOutput = parentOutput;
        this.superClass = (DeclaredType) parentOutput.element(env).asType();
        this.env = ProxyEnv.wrap(env);
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
        for (ExecutableElement constructor : constructorsIn(superClass.asElement().getEnclosedElements())) {
            MethodSpec.Builder method = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            Set<String> paramNames = new HashSet<>();
            for (VariableElement parameter : constructor.getParameters()) {
//                TypeMirror paramType = env.types().asMemberOf(superClass, parameter);
                String paramName = parameter.getSimpleName().toString();
                paramNames.add(paramName);
                method.addParameter(TypeName.get(parameter.asType()), paramName);
            }

            boolean first = true;
            StringBuilder params = new StringBuilder();
            for (String paramName : paramNames) {
                if (!first) {
                    params.append(", ");
                }
                first = false;
                params.append(paramName);
            }

            method.addStatement("super($L)", params.toString());

            classBuilder.addMethod(method.build());
        }
//        classBuilder.addMethod(MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(TypeName.get(function.functionType()), name)
//                .addStatement("super($L)", name)
//                .build());

        return classBuilder.build();
    }
}
