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

package com.epam.ta.reportportal.util;

import com.google.common.base.Charsets;
import com.google.common.base.StandardSystemProperty;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Resource copier bean test
 *
 * @author Andrei Varabyeu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ResourceCopierBeanTest.TestConfig.class })
public class ResourceCopierBeanTest {

	private static final String RANDOM_NAME = RandomStringUtils.randomAlphabetic(5);

	private static final String RESOURCE_TO_BE_COPIED = "classpath:logback.xml";

	@Autowired
	private ResourceLoader resourceLoader;

	@Test
	public void testResourceCopierBean() throws IOException {
		File createdFile = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), RANDOM_NAME);
		Resource resource = resourceLoader.getResource(RESOURCE_TO_BE_COPIED);
		String copied = Files.toString(createdFile, Charsets.UTF_8);
		String fromResource = CharStreams.toString(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
		Assert.assertEquals("Copied file is not equal to resource source", fromResource, copied);
	}

	@After
	public void deleteCreatedFile() throws IOException {
		FileUtils.deleteQuietly(new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), RANDOM_NAME));
	}

	@Configuration
	public static class TestConfig {

		@Bean
		ResourceCopierBean resourceCopier() {
			ResourceCopierBean rcb = new ResourceCopierBean();
			rcb.setSource(RESOURCE_TO_BE_COPIED);
			rcb.setTempDirDestination(RANDOM_NAME);
			return rcb;
		}
	}

}