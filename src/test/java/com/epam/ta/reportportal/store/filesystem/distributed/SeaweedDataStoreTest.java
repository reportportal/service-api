package com.epam.ta.reportportal.store.filesystem.distributed;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lokra.seaweedfs.core.FileSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

@Ignore
public class SeaweedDataStoreTest {

	private static final String TEST_FILE = "test-file.txt";

	private SeaweedDataStore dataStore;

	@Before
	public void setUp() throws Exception {

		FileSource fileSource = new FileSource();
		// SeaweedFS master server host
		fileSource.setHost("localhost");
		// SeaweedFS master server port
		fileSource.setPort(9333);
		// Startup manager and listens for the change
		fileSource.startup();

		dataStore = new SeaweedDataStore(fileSource);
	}

	@Test
	public void save_load_delete() throws Exception {

		String savedFilePath = dataStore.save(TEST_FILE, new ByteArrayInputStream("test text".getBytes(Charsets.UTF_8)));

		System.out.println("savedFilePath " + savedFilePath);

		//		and: load it back
		InputStream loaded = dataStore.load(savedFilePath);

		//		then: saved and loaded files should be the same
		byte[] bytes = IOUtils.toByteArray(loaded);
		String result = new String(bytes, Charsets.UTF_8);
		assertEquals("saved and loaded files should be the same", "test text", result);

		//		when: delete saved file
		dataStore.delete(savedFilePath);

		//		and: load file again
		boolean isNotFound = false;
		try {

			dataStore.load(savedFilePath);
		} catch (ReportPortalException e) {

			isNotFound = true;
		}

		//		then: deleted file should not be found
		assertTrue("deleted file should not be found", isNotFound);
	}
}
