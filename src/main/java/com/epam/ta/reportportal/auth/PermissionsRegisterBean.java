/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.auth.permissions.LookupPermission;
import com.epam.ta.reportportal.auth.permissions.Permission;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.Arrays;
import java.util.Map;

public class PermissionsRegisterBean implements BeanDefinitionRegistryPostProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Map<String, Permission> permissionsMap = beanFactory.getBean("permissionsMap", Map.class);
		beanFactory.getBeansOfType(Permission.class).entrySet().forEach(permission -> {
			/*
			 * There will be no NPE since we asked bean factory to get beans
			 * with this annotation
			 */
			Arrays.stream(permission.getValue().getClass().getAnnotation(LookupPermission.class).value()).forEach(permissionName -> {
				/*
				 * TODO add check for type before doing this
				 */
				Permission permissionBean = permission.getValue();
				beanFactory.autowireBeanProperties(permissionBean, AutowireCapableBeanFactory.AUTOWIRE_NO, true);
				permissionsMap.put(permissionName, permissionBean);
			});
		});
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		//nothing to do
	}
}