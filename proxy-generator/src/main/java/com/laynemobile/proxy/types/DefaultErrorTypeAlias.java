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

package com.laynemobile.proxy.types;

import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeVisitor;

final class DefaultErrorTypeAlias extends AbstractDeclaredTypeAlias implements ErrorTypeAlias {
    private DefaultErrorTypeAlias(ErrorType declaredType) {
        super(declaredType);
    }

    static ErrorTypeAlias of(ErrorType declaredType) {
        if (declaredType instanceof DeclaredTypeAlias) {
            return (ErrorTypeAlias) declaredType;
        }
        return new DefaultErrorTypeAlias(declaredType);
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitError(this, p);
    }

    @Override public boolean equals(Object o) {
        return o instanceof DefaultErrorTypeAlias && super.equals(o);
    }
}
