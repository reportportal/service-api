package com.epam.ta.reportportal.store.filesystem;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class LocalDataStoreTest {

	private static final String ROOT_PATH = System.getProperty("java.io.tmpdir");
	private static final String TEST_FILE = "test-file.txt";

	private LocalDataStore localDataStore;

	private LocalFilePathGenerator fileNameGenerator;

	@Before
	public void setUp() throws Exception {

		fileNameGenerator = Mockito.mock(LocalFilePathGenerator.class);

		localDataStore = new LocalDataStore(fileNameGenerator, ROOT_PATH);
	}

	@Test
	public void save_load() throws Exception {

		//  given:
		String generatedDirectory = "/test";
		when(fileNameGenerator.generate()).thenReturn(generatedDirectory);
		FileUtils.deleteDirectory(new File(Paths.get(ROOT_PATH, generatedDirectory).toUri()));

		//  when:
		String savedFilePath = localDataStore.save(TEST_FILE, new ByteArrayInputStream("test text".getBytes(Charsets.UTF_8)));

		System.out.println("saved " + savedFilePath);

		InputStream loaded = localDataStore.load(savedFilePath);

		byte[] bytes = IOUtils.toByteArray(loaded);

		String result = new String(bytes, Charsets.UTF_8);

		assertEquals("test text", result);
	}
}
