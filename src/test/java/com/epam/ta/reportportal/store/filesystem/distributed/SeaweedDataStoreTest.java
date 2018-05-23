package com.epam.ta.reportportal.store.filesystem.distributed;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.lokra.seaweedfs.core.FileSource;
import org.testcontainers.containers.GenericContainer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SeaweedDataStoreTest {

	private static final String TEST_FILE = "test-file.txt";

	private SeaweedDataStore dataStore;

	@ClassRule
	public static GenericContainer seaweedMaster = new GenericContainer("chrislusf/seaweedfs:latest").withExposedPorts(8080, 9333)
			.withCommand("server")
			.withCreateContainerCmdModifier(new Consumer<CreateContainerCmd>() {
				@Override
				public void accept(CreateContainerCmd cmd) {

					cmd.withHostName("localhost");
				}
			});

	@Before
	public void setUp() throws Exception {

		FileSource fileSource = new FileSource();
		fileSource.setHost(seaweedMaster.getContainerIpAddress());
		fileSource.setPort(seaweedMaster.getMappedPort(9333));
		fileSource.startup();

		dataStore = new SeaweedDataStore(fileSource);
	}

	@Test
	public void save_load_delete() throws Exception {

		String savedFilePath = dataStore.save(TEST_FILE, new ByteArrayInputStream("test text".getBytes(Charsets.UTF_8)));

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
