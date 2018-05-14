package com.epam.ta.reportportal.store.filesystem;

import java.io.InputStream;

public interface DataStore {

	String save(String fileName, InputStream inputStream);

	InputStream load(String filePath);
}
