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

import com.google.common.base.StandardSystemProperty;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Convenience class for working with resources. Copies {@link #source} input
 * stream to {@link #destination}
 *
 * @author Andrei Varabyeu
 */
public class ResourceCopierBean implements InitializingBean {

	@Autowired
	private ResourceLoader resourceLoader;

	private ByteSource source;

	private ByteSink destination;

	/**
	 * Sets destination as simple file name
	 *
	 * @param filename
	 */
	public void setDestination(String filename) {
		File destination = new File(filename);
		this.destination = Files.asByteSink(destination);
	}

	/**
	 * Sets destination as folder and filename
	 *
	 * @param directory
	 * @param to
	 */
	public void setDestination(File directory, String to) {
		this.destination = Files.asByteSink(new File(directory, to));
	}

	/**
	 * Sets destination as system temp directory and specified file name into
	 *
	 * @param to
	 */
	public void setTempDirDestination(String to) {
		this.destination = Files.asByteSink(new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), to));
	}

	/**
	 * Sets source as springs {@link org.springframework.core.io.Resource}
	 *
	 * @param from
	 */
	public void setSource(final String from) {
		this.source = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return resourceLoader.getResource(from).getInputStream();
			}
		};
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		destination.writeFrom(source.openStream());
	}
}