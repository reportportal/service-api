package com.epam.ta.reportportal.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class MultipartFileUtils {

	private MultipartFileUtils() {
		//static only
	}

	public static CommonsMultipartFile getMultipartFile(String path) throws IOException {
		ClassPathResource classPathResource = new ClassPathResource(path);
		FileItem fileItem = new DiskFileItem(
				"mainFile",
				Files.probeContentType(Paths.get(classPathResource.getURI())),
				false,
				classPathResource.getFilename(),
				(int) classPathResource.getInputStream().available(),
				null
		);
		IOUtils.copy(classPathResource.getInputStream(), fileItem.getOutputStream());
		return new CommonsMultipartFile(fileItem);
	}
}
