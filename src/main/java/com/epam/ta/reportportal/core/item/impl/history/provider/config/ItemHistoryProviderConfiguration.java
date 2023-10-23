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

package com.epam.ta.reportportal.core.item.impl.history.provider.config;

import com.epam.ta.reportportal.core.item.impl.history.ItemHistoryBaselineEnum;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProvider;
import com.epam.ta.reportportal.core.item.impl.history.provider.impl.ComparingBaselineHistoryProvider;
import com.epam.ta.reportportal.core.item.impl.history.provider.impl.FilterBaselineHistoryProvider;
import com.epam.ta.reportportal.core.item.impl.history.provider.impl.LaunchBaselineHistoryProvider;
import com.epam.ta.reportportal.core.item.impl.history.provider.impl.TestItemBaselineHistoryProvider;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class ItemHistoryProviderConfiguration implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Bean(name = "historyProviderMapping")
  public Map<ItemHistoryBaselineEnum, HistoryProvider> historyProviderMapping() {
    Map<ItemHistoryBaselineEnum, HistoryProvider> mapping = new HashMap<>();
    mapping.put(ItemHistoryBaselineEnum.COMPARING,
        applicationContext.getBean(ComparingBaselineHistoryProvider.class));
    mapping.put(ItemHistoryBaselineEnum.FILTER,
        applicationContext.getBean(FilterBaselineHistoryProvider.class));
    mapping.put(ItemHistoryBaselineEnum.ITEM,
        applicationContext.getBean(TestItemBaselineHistoryProvider.class));
    mapping.put(ItemHistoryBaselineEnum.LAUNCH,
        applicationContext.getBean(LaunchBaselineHistoryProvider.class));
    return mapping;
  }

}
