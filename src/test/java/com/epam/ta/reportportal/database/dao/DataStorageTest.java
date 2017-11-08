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

package com.epam.ta.reportportal.database.dao;

import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.exception.DataStorageException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.*;

public class DataStorageTest extends BaseDaoContextTest {

	@Autowired
	private DataStorage dataStorage;

	@Test
	public void testWithEmptyFile() throws IOException, DataStorageException {
		String dataId = saveDemoFile(null);
		BinaryData data = dataStorage.fetchData(dataId);
		Assert.assertThat("Data is not present in the storage", data, notNullValue());
		Assert.assertThat("Size should be empty", data.getLength(), equalTo(0L));
	}

	@Test
	public void testFileSizeNotNull() throws IOException {
		String dataId = saveDemoFile("Let's make file size greater than zero");
		BinaryData data = dataStorage.fetchData(dataId);

		Assert.assertThat("Data is not present in the storage", data, notNullValue());

		Assert.assertThat("Size should not be empty", data.getLength(), greaterThan(0L));
	}

	@Test
	public void testFileDeleted() throws IOException {
		String dataId = saveDemoFile(null);
		dataStorage.deleteData(dataId);

		BinaryData data = dataStorage.fetchData(dataId);
		Assert.assertThat("Data is not deleted", data, nullValue());
	}

	@Test
	public void testDeleteAll() throws IOException {
		String dataId = saveDemoFile(null);
		dataStorage.deleteAll();

		BinaryData data = dataStorage.fetchData(dataId);
		Assert.assertThat("Data is not deleted", data, nullValue());
	}

	private String saveDemoFile(String content) throws IOException {
		File file = File.createTempFile("rp-DataStorageTest", ".tmp");
		FileUtils.write(file, content);
		String dataId = dataStorage.saveData(new BinaryData("application/json", 0L, new FileInputStream(file)), "rp-DataStorageTest.tmp");
		return dataId;

	}
}