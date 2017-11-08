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

package com.epam.ta.reportportal.database.fixture;

import com.google.common.base.Throwables;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * JUnit's rule. Imports demo data for testing purposes. Implements
 * {@link ApplicationContextAware} and that's why contains
 * {@link ApplicationContext} automatically
 *
 * @author Andrei Varabyeu
 */
public class SpringFixtureRule extends ExternalResource implements ApplicationContextAware {

	/**
	 * Spring's Application context
	 */
	private ApplicationContext applicationContext;

	@Autowired
	private CacheManager cacheManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.junit.rules.TestRule#apply(org.junit.runners.model.Statement,
	 * org.junit.runner.Description)
	 */
	@Override
	public Statement apply(Statement statement, Description description) {
		String bean = getFixtureAnnotation(description).value();
		Object toBeImported = applicationContext.getBean(bean);
		FixtureImporter fixtureImporter = applicationContext.getBean(FixtureImporter.class);
		fixtureImporter.dropDatabase();
		cacheManager.getCacheNames().forEach(it -> cacheManager.getCache(it).clear());
		fixtureImporter.importFixture(toBeImported);

		return statement;
	}

	/**
	 * Looking for {@link SpringFixture} annotation on test method and test
	 * class
	 *
	 * @param description
	 * @return
	 */
	private SpringFixture getFixtureAnnotation(Description description) {
		SpringFixture fixture = null;
		try {
			Class<?> testClass = description.getTestClass();

			Method method = testClass.getDeclaredMethod(description.getMethodName());

			/* Try to find fixture on method */
			fixture = method.getAnnotation(SpringFixture.class);

			/*
			 * if method is not annotated tries to find annotation on test class
			 */
			if (!isTestAnnotated(fixture)) {
				fixture = AnnotationUtils.findAnnotation(testClass, SpringFixture.class);

			}

		} catch (Exception e) {
			Throwables.propagate(e);
		}

		if (null == fixture) {
			throw new RuntimeException("You should put 'Fixture' annotation on test class or test method");
		}
		return fixture;

	}

	/**
	 * Is annotated with fixture
	 *
	 * @param fixture
	 * @return
	 */
	private boolean isTestAnnotated(SpringFixture fixture) {
		return fixture != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}