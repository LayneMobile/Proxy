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

package com.laynemobile.proxy;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.annotations.Generate.ProxyBuilder;
import com.laynemobile.proxy.annotations.Generate.ProxyFunction;
import com.laynemobile.proxy.functions.FunctionHandlers;
import com.laynemobile.proxy.internal.ProxyLog;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@AutoService(Processor.class)
public class ProxyGenerator extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        boolean processed = false;

        List<TypeElement> typeElements = new ArrayList<>();

        for (Element element : env.getElementsAnnotatedWith(ProxyBuilder.class)) {
            // Ensure it is a class element
            if (element.getKind() != ElementKind.INTERFACE) {
                error(element, "Only classes can be annotated with @%s", ProxyBuilder.class.getSimpleName());
                return true; // Exit processing
            }

            TypeElement typeElement = (TypeElement) element;
            typeElements.add(typeElement);
        }

        for (TypeElement typeElement : typeElements) {
            processed = true;

            log(typeElement, "processing element: %s", typeElement);

            DeclaredType containingType = (DeclaredType) typeElement.asType();

            List<? extends TypeMirror> interfaceTypes = typeElement.getInterfaces();
            log("interfaces: %s", interfaceTypes);
            boolean parent = true;
            for (TypeMirror interfaceType : interfaceTypes) {
                parent = false;
                TypeElement interfaceElement = (TypeElement) typeUtils.asElement(interfaceType);
                log("interface type: %s", interfaceType);
                log("interface element typeParameters: %s", interfaceElement.getTypeParameters());
                if (typeElements.contains(interfaceElement)) {
                    log("interface element already contained in top elements");
                }
                if (interfaceType.getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredType = (DeclaredType) interfaceType;
                    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                    log("typeArguments: %s", typeArguments);
                    for (TypeMirror typeArgument : typeArguments) {
                        TypeKind kind = typeArgument.getKind();
                        log("typeArgument: %s", typeArgument);
                        log("typeArgument kind: %s", kind);
                        if (kind == TypeKind.TYPEVAR) {
                            TypeVariable typeVariable = (TypeVariable) typeArgument;
                            log("typeVariable upperBound: %s", typeVariable.getUpperBound());
                            log("typeVariable lowerBound: %s", typeVariable.getLowerBound());
                        } else if (kind == TypeKind.DECLARED) {
                            DeclaredType declaredVar = (DeclaredType) typeArgument;
                            log("declaredTypeArgument typeArguments: %s", declaredVar.getTypeArguments());
                        } else if (kind == TypeKind.WILDCARD) {
                            WildcardType wildcardType = (WildcardType) typeArgument;
                            log("wildcardType extendsBound: %s", wildcardType.getExtendsBound());
                            log("wildcardType superBound: %s", wildcardType.getSuperBound());
                        }
                    }
                }
            }
            if (parent) {
                log("parent element");
            }

            List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
            List<TypeVariable> typeVariables = new ArrayList<>(typeParameters.size());
            log("typeParameters: %s", typeParameters);
            for (TypeParameterElement typeParameter : typeParameters) {
                log("typeParameter: %s", typeParameter);
                log("typeParameter bounds: %s", typeParameter.getBounds());
                ElementKind paramKind = typeParameter.getKind();
                log("typeParameter kind: %s", paramKind);
                TypeMirror paramType = typeParameter.asType();
                log("typeParameter type: %s", paramType);
                if (paramType.getKind() == TypeKind.TYPEVAR) {
                    typeVariables.add((TypeVariable) paramType);
                }
            }

            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() != ElementKind.METHOD) {
                    continue;
                }
                ExecutableElement methodElement = (ExecutableElement) enclosed;
                log(methodElement, "processing method element: %s", methodElement);
                ProxyFunction function = enclosed.getAnnotation(ProxyFunction.class);
                String name = function == null ? "" : function.value();
                if (name.isEmpty()) {
                    name = methodElement.getSimpleName().toString();
                }
                log("name: %s", name);

                List<? extends VariableElement> params = methodElement.getParameters();
                int length = params.size();
                String num;
                if (length > 9) {
                    length = 0;
                    num = "N";
                } else {
                    num = Integer.toString(length);
                }

                TypeMirror returnType = methodElement.getReturnType();
                String functionClass;
                TypeMirror[] paramTypes;
                log("returnType: %s", returnType);
                if (returnType.getKind() == TypeKind.VOID) {
                    log("void");
                    paramTypes = new TypeMirror[length];
                    functionClass = "Action";
                } else {
                    log("not void");
                    paramTypes = new TypeMirror[length + 1];
                    paramTypes[length] = returnType;
                    functionClass = "Func";
                }

                log("params length: %d", length);
                log("params: %s", params);

                for (int i = 0; i < length; i++) {
                    VariableElement param = params.get(i);
                    log("param: %s", param);
                    ElementKind paramKind = param.getKind();
                    log("param kind: %s", paramKind);
                    TypeMirror paramType = param.asType();
                    log("param type: %s", paramType);
                    paramTypes[i] = paramType;
                }
                log("paramTypes: %s", Arrays.toString(paramTypes));

                String packageName = "com.laynemobile.proxy.functions.";
                TypeElement functionElement = elementUtils.getTypeElement(packageName + functionClass + num);
                log("function element: %s", functionElement);
                DeclaredType functionType = typeUtils.getDeclaredType(functionElement, paramTypes);
                log("function type: %s", functionType);

                TypeElement abstractProxyFunctionElement = elementUtils.getTypeElement(
                        packageName + "AbstractProxyFunction");
                DeclaredType abstractProxyFunctionType
                        = typeUtils.getDeclaredType(abstractProxyFunctionElement, functionType);
                log("AbstractProxyFunction type: %s", abstractProxyFunctionType);
                log("AbstractProxyFunction type typeArguments: %s", abstractProxyFunctionType.getTypeArguments());

                List<? extends TypeParameterElement> typeParams = methodElement.getTypeParameters();
                log("typeParams length: %d", typeParams.size());
                log("typeParams: %s", typeParams);

                String className = "Abstract" + typeElement.getSimpleName() + "_" + methodElement.getSimpleName() + "Function";
                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                        .superclass(TypeName.get(abstractProxyFunctionType))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                for (TypeVariable typeVariable : typeVariables) {
                    classBuilder.addTypeVariable(TypeVariableName.get(typeVariable));
                }

                // Constructor
                classBuilder.addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(TypeName.get(functionType), name)
                        .addStatement("super($L)", name)
                        .build());

                // handler method
                ClassName NamedMethodHandler = ClassName.get(NamedMethodHandler.class);
                ClassName NamedMethodHandler_Builder = NamedMethodHandler.nestedClass("Builder");
                ClassName FunctionHandlers = ClassName.get(FunctionHandlers.class);
                classBuilder.addMethod(MethodSpec.methodBuilder("handler")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(NamedMethodHandler)
                        .addCode(CodeBlock.builder()
                                .add("$[")
                                .add("return new $T()\n", NamedMethodHandler_Builder)
                                .add(".setName($S)\n", methodElement.getSimpleName())
                                .add(".setMethodHandler($T.from(function()))\n", FunctionHandlers)
                                .add(".build()")
                                .add(";\n$]")
                                .build())
                        .build());

                packageName = ClassName.get(typeElement).packageName() + ".generated";

                // Write java file
                try {
                    JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                            .build();
                    log("writing abstract class type thing");
                    log(javaFile.toString());
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return processed;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ProxyBuilder.class.getCanonicalName());
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void log(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(NOTE, message, element);
    }

    private void log(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(NOTE, message);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message);
    }
}
