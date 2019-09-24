package com.epam.ta.reportportal.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class MultipartFileUtilsTest {

	@Test
	void getMultipartFile() throws IOException {
		String path = "image/image.png";
		File expected = new ClassPathResource(path).getFile();
		CommonsMultipartFile file = MultipartFileUtils.getMultipartFile(path);
		assertEquals(expected.length(), file.getSize());
		assertEquals(expected.getName(), file.getFileItem().getName());
		assertEquals("image/png", file.getContentType());
		assertTrue(IOUtils.contentEquals(new FileInputStream(expected), file.getInputStream()));
	}
}