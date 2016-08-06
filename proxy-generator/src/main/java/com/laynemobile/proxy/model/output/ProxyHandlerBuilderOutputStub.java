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

import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.model.ProxyElement;
import com.laynemobile.proxy.model.ProxyEnv;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildList;
import static com.laynemobile.proxy.Util.typeMirrorArray;
import static com.laynemobile.proxy.Util.typeNameArray;

public final class ProxyHandlerBuilderOutputStub extends AbstractTypeElementOutputStub {
    private final ProxyElement proxyElement;
    private final ProxyEnv env;
    private final ImmutableSet<ProxyFunctionOutput> functions;

    private ProxyHandlerBuilderOutputStub(Env env, ProxyElement proxyElement, Set<ProxyFunctionOutput> functions) {
        super(proxyElement.packageName() + ".generated", proxyElement.className().simpleName() + "HandlerBuilder");
        this.env = ProxyEnv.wrap(env);
        this.proxyElement = proxyElement;
        this.functions = ImmutableSet.copyOf(functions);
    }

    static ProxyHandlerBuilderOutputStub create(Env env, ProxyElement proxyElement,
            Set<ProxyFunctionOutput> functions) {
        return new ProxyHandlerBuilderOutputStub(env, proxyElement, functions);
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
        DeclaredType proxyType = (DeclaredType) proxyElement.element().asType().actual();
        TypeMirror[] typeParams = typeMirrorArray(proxyType.getTypeArguments());
        TypeElement abstractBuilderElement = env.elements()
                .getTypeElement("com.laynemobile.proxy.AbstractProxyHandlerBuilder");
        DeclaredType abstractBuilderType = env.types().getDeclaredType(abstractBuilderElement, proxyType);

        classBuilder.superclass(TypeName.get(abstractBuilderType))
                .addModifiers(Modifier.PUBLIC);
        // TODO: add annotation!
//                .addAnnotation(AnnotationSpec.builder(ProxyFunctionImplementation.class)
//                        .addMember("value", "$T.class", subclass)
//                        .build())
        ;

        List<TypeVariableAlias> typeVariables = Util.buildList(proxyElement.element().getTypeParameters(),
                new Transformer<TypeVariableAlias, TypeParameterElementAlias>() {
                    @Override
                    public TypeVariableAlias transform(TypeParameterElementAlias typeParameterElementAlias) {
                        TypeMirrorAlias type = typeParameterElementAlias.asType();
                        if (type.getKind() == TypeKind.TYPEVAR) {
                            return AliasTypes.get((TypeVariable) type.actual());
                        }
                        return null;
                    }
                });

        List<TypeVariableName> typeVariableNames
                = buildList(typeVariables, new Transformer<TypeVariableName, TypeVariableAlias>() {
            @Override public TypeVariableName transform(TypeVariableAlias typeVariableAlias) {
                return TypeVariableName.get(typeVariableAlias.actual());
            }
        });
        classBuilder.addTypeVariables(typeVariableNames);

        TypeName outputType = ParameterizedTypeName.get(typeName(), typeNameArray(typeVariableNames));

        Set<FieldSpec> handlerFields = new HashSet<>();
        for (ProxyFunctionOutput function : functions) {
            ProxyFunctionElement element = function.element();
            String fieldName = element.name();

            TypeElement fieldElement = function.typeOutputStub().element(env);
            DeclaredType fieldType = env.types().getDeclaredType(fieldElement, typeParams);

            // create field
            FieldSpec fieldSpec = FieldSpec.builder(TypeName.get(fieldType), fieldName)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            classBuilder.addField(fieldSpec);
            handlerFields.add(fieldSpec);

            // create method for each constructor
            for (ExecutableElement constructor : ElementFilter.constructorsIn(fieldElement.getEnclosedElements())) {
                List<? extends VariableElement> params = constructor.getParameters();
                MethodSpec.Builder method = MethodSpec.methodBuilder(fieldName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(outputType);
                if (params.size() == 0) {
                    method.addStatement("this.$N = new $T()", fieldSpec, fieldType);
                } else if (params.size() == 1) {
                    VariableElement param = params.get(0);
                    TypeName paramType = TypeName.get(param.asType());
                    method.addParameter(paramType, fieldName)
                            .addStatement("this.$N = new $T($L)", fieldSpec, fieldType, fieldName);
                } else {
                    throw new IllegalArgumentException("no more than 1 param allowed");
                }

                classBuilder.addMethod(method
                        .addStatement("return this")
                        .build());
            }
        }

        TypeElement typeTokenElement = env.elements().getTypeElement("com.laynemobile.proxy.TypeToken");
        DeclaredType typeTokenType = env.types().getDeclaredType(typeTokenElement, proxyType);

        // build function
        TypeElement handlerElement = env.elements().getTypeElement("com.laynemobile.proxy.ProxyHandler");
        DeclaredType handlerType = env.types().getDeclaredType(handlerElement, proxyType);
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(handlerType));

        CodeBlock.Builder returnCode = CodeBlock.builder()
                .add("$[")
                .add("return $T.builder(new $T() {})\n", ClassName.get(handlerElement), TypeName.get(typeTokenType));

        for (FieldSpec handlerField : handlerFields) {
            returnCode.add(".handle(handler($N))\n", handlerField);
        }

        buildMethod.addCode(returnCode
                .add(".build()")
                .add(";\n$]")
                .build());
        return classBuilder.addMethod(buildMethod.build())
                .build();
    }
}
