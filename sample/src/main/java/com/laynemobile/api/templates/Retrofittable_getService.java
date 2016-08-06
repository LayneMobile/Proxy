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

package com.laynemobile.api.templates;

import com.laynemobile.api.generated.AbstractRetrofittable_getService;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0;

import retrofit.RestAdapter;

@Generated
public class Retrofittable_getService<S> extends AbstractRetrofittable_getService<S> {
    public Retrofittable_getService(Func0<S> service) {
        super(service);
    }

    public Retrofittable_getService(final Class<S> serviceType, final Func0<RestAdapter> restAdapter) {
        super(new Func0<S>() {
            @Override public S call() {
                return createService(serviceType, restAdapter.call());
            }
        });
    }

    public Retrofittable_getService(final Class<S> serviceType, final RestAdapter restAdapter) {
        super(new Func0<S>() {
            @Override public S call() {
                return createService(serviceType, restAdapter);
            }
        });
    }

    private static <S> S createService(Class<S> serviceType, RestAdapter restAdapter) {
        return restAdapter.create(serviceType);
    }
}
