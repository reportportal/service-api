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