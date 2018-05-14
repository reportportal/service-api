package com.epam.ta.reportportal.store.filesystem;

import com.epam.ta.reportportal.store.database.BinaryData;

public interface DataStore {

	String save(String fileName, BinaryData binaryData);
}
