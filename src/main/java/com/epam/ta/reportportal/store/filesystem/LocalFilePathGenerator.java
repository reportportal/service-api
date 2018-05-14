package com.epam.ta.reportportal.store.filesystem;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalFilePathGenerator {

	/**
	 * Generate relative file path for new local file.
	 *
	 * @return
	 */
	public String generate() {

		String uuid = UUID.randomUUID().toString();

		String levelOne = uuid.substring(0, 2);
		String levelTwo = uuid.substring(2, 4);
		String levelThree = uuid.substring(4, 6);
		String tail = uuid.substring(6);

		System.out.println(levelOne + levelTwo + levelThree + tail);

		return "/" + levelOne + "/" + levelTwo + "/" + levelThree + "/" + tail;
	}
}
