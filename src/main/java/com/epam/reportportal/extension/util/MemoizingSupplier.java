/*
 * Copyright 2018 EPAM Systems
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

package com.epam.reportportal.extension.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class MemoizingSupplier<T> implements Supplier<T> {

  private final Supplier<T> delegate;
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private T value;

  public MemoizingSupplier(Supplier<T> delegate) {
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public T get() {
    if (!initialized.get()) {
      synchronized (this) {
        if (!initialized.get()) {
          T t = delegate.get();
          value = t;
          initialized.set(true);
          return t;
        }
      }
    }
    return value;
  }
}
