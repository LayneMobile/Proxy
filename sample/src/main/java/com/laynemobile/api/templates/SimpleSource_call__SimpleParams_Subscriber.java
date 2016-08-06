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

import com.laynemobile.api.SimpleParams;
import com.laynemobile.api.generated.AbstractSimpleSource_call__SimpleParams_Subscriber;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action2;

import rx.Subscriber;

@Generated
public class SimpleSource_call__SimpleParams_Subscriber<T> extends AbstractSimpleSource_call__SimpleParams_Subscriber<T> {
  public SimpleSource_call__SimpleParams_Subscriber(Action2<SimpleParams, Subscriber<? super T>> source) {
    super(source);
  }
}
