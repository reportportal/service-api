/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * {@link FactoryBean} with access to {@link ApplicationContext} with lazy initialization
 *
 * @param <T> - type of bean
 * @author Andrei Varabyeu
 */
public abstract class ApplicationContextAwareFactoryBean<T> implements FactoryBean<T>,
    ApplicationContextAware, InitializingBean {

  /**
   * Application context holder
   */
  private ApplicationContext applicationContext;

  /**
   * Supplier of bean to be created
   */
  private Supplier<T> beanSupplier;

  /**
   * Whether is bean to be creates going to be singleton
   */
  private boolean singleton = true;

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  @Override
  public T getObject() {
    return beanSupplier.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  @Override
  public boolean isSingleton() {
    return this.singleton;
  }

  public void setSingleton(boolean singleton) {
    this.singleton = singleton;
  }

  /**
   * Instantiates supplier for bean to be created. This mades possible lazy-initialization
   */
  @Override
  public void afterPropertiesSet() {
    Supplier<T> supplier = this::createInstance;

    this.beanSupplier = isSingleton() ? Suppliers.memoize(supplier) : supplier;
  }

  protected ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.springframework.context.ApplicationContextAware#setApplicationContext
   * (org.springframework.context.ApplicationContext)
   */
  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  /**
   * Template method that subclasses must override to construct the object returned by this
   * factory.
   * <p>
   * Invoked on initialization of this FactoryBean in case of a singleton; else, on each
   * {@link #getObject()} call.
   *
   * @return the object returned by this factory
   * @see #getObject()
   */
  protected abstract T createInstance();
}
