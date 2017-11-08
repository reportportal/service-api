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