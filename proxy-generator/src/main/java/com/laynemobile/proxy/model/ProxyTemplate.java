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

import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.annotations.ProxyFunctionImplementation;
import com.laynemobile.proxy.functions.Func0;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;
import sourcerer.processor.Template;

public class ProxyTemplate extends Template {
    private final TreeSet<ProxyElement> proxyElements = new TreeSet<>();

    public ProxyTemplate(Env env) {
        super(env);
    }

    @Override public Set<String> supportedAnnotationTypes() {
        return ImmutableSet.<String>builder()
                .add(GenerateProxyBuilder.class.getCanonicalName())
                .add(Generated.class.getCanonicalName())
                .add(ProxyFunctionImplementation.class.getCanonicalName())
                .build();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean processed = false;

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class)) {
            // Ensure it is an interface element
            if (element.getKind() != ElementKind.INTERFACE) {
                error(element, "Only interfaces can be annotated with @%s",
                        GenerateProxyBuilder.class.getSimpleName());
                return true; // Exit processing
            }

            if (!add(element)) {
                return true; // Exit processing
            }
            processed = true;
        }
        if (processed) {
            // Write
            try {
                write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Generated.class)) {
            // Ensure it is a class element
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only classes can be annotated with @%s",
                        Generated.class.getSimpleName());
                return true; // Exit processing
            }

            log(element, "say\n\n");
            log(element, "processing generated type!\n\n");

            final Generated annotation = element.getAnnotation(Generated.class);
            if (annotation != null) {
                log(element, "has annotation: %s", annotation);
            }

            processed = true;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(ProxyFunctionImplementation.class)) {
            // Ensure it is a class element
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only classes can be annotated with @%s",
                        ProxyFunctionImplementation.class.getSimpleName());
                return true; // Exit processing
            }

            log("\n\n");
            log(element, "processing abstract function type!\n\n");

            final ProxyFunctionImplementation annotation = element.getAnnotation(ProxyFunctionImplementation.class);
            if (annotation != null) {
                log(element, "has annotation: %s", annotation);
                TypeElement implementation = Util.parse(new Func0<Class<?>>() {
                    @Override public Class<?> call() {
                        return annotation.value();
                    }
                }, this);
                log(element, "implementation = %s", implementation);
            }

            processed = true;
        }

        return processed;
    }

    private boolean add(Element element) {
        ProxyElement proxyElement = ProxyElement.cache().parse(element, this);
        if (proxyElement == null) {
            return false;
        }
        synchronized (proxyElements) {
            if (!proxyElements.contains(proxyElement)) {
                proxyElements.add(proxyElement);
            }
        }
        return true;
    }

    private List<ProxyElement> proxyElements() {
        synchronized (proxyElements) {
            return new ArrayList<>(proxyElements);
        }
    }

    private void write() throws IOException {
        Filer filer = filer();
        for (ProxyElement proxyElement : proxyElements()) {
            for (ProxyFunctionElement functionElement : proxyElement.methods()) {
                JavaFile abstractProxyFunctionClass
                        = functionElement.newAbstractProxyFunctionTypeJavaFile();
                log("writing AbstractProxyFunctionClass -> \n" + abstractProxyFunctionClass.toString());
                abstractProxyFunctionClass.writeTo(filer);
            }
        }
    }
}
