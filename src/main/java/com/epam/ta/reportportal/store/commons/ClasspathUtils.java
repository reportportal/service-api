/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.commons;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

/**
 * Set of useful utils for working with classpath
 *
 * @author Andrei Varabyeu
 */
public class ClasspathUtils {

	/*
	 * Reflections object for scanning classpath of context class loader
	 */
	private static final Reflections REFLECTIONS = new Reflections(
			new ConfigurationBuilder().addClassLoader(ClasspathHelper.contextClassLoader())
					.addUrls(ClasspathHelper.forJavaClassPath())
					.addScanners(new SubTypesScanner()));

	/**
	 * Finds in classpath subclasses of provided class
	 *
	 * @param clazz Class to find subclasses of
	 * @return Set of sublcasses
	 */
	public static <T> Set<Class<? extends T>> findSubclassesOf(Class<T> clazz) {
		return REFLECTIONS.getSubTypesOf(clazz);
	}
}
