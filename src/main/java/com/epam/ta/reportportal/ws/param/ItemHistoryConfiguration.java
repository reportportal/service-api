package com.epam.ta.reportportal.ws.param;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class ItemHistoryConfiguration implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean(name = "historyProviderMapping")
	public Map<ItemHistoryBaselineEnum, HistoryProvider> historyProviderMapping() {
		Map<ItemHistoryBaselineEnum, HistoryProvider> mapping = new HashMap<>();
		mapping.put(ItemHistoryBaselineEnum.DEFAULT, applicationContext.getBean(DefaultBaselineHistoryProvider.class));
		mapping.put(ItemHistoryBaselineEnum.FILTER, applicationContext.getBean(FilterBaselineHistoryProvider.class));
		return mapping;
	}

}
