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
import com.laynemobile.proxy.annotations.Generate.ProxyBuilder;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Template;

public class ProxyTemplate extends Template {
    private final Proxies proxies;

    public ProxyTemplate(ProxyTemplate template) {
        super(template);
        this.proxies = new Proxies(this);
    }

    public ProxyTemplate(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.proxies = new Proxies(this);
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Proxies proxies = this.proxies;
        boolean processed = false;

        for (Element element : roundEnv.getElementsAnnotatedWith(ProxyBuilder.class)) {
            if (!proxies.add(element)) {
                return true; // Exit processing
            }
            processed = true;
        }

        if (processed) {
            // Write
            try {
                proxies.writeTo(filer());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return processed;
    }

    @Override public Set<String> supportedAnnotationTypes() {
        return ImmutableSet.of(ProxyBuilder.class.getCanonicalName());
    }
}
