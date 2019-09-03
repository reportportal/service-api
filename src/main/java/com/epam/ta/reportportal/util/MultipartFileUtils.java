package com.epam.ta.reportportal.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class MultipartFileUtils {

	private MultipartFileUtils() {
		//static only
	}

	public static CommonsMultipartFile getMultipartFile(String path) throws IOException {
		File file = new ClassPathResource(path).getFile();
		FileItem fileItem = new DiskFileItem("mainFile",
				Files.probeContentType(file.toPath()),
				false,
				file.getName(),
				(int) file.length(),
				file.getParentFile()
		);
		IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
		return new CommonsMultipartFile(fileItem);
	}
}
