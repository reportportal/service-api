package com.epam.ta.reportportal.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class MultipartFileUtils {

	private static Tika tika = new Tika();

	private MultipartFileUtils() {
		//static only
	}

	public static CommonsMultipartFile getMultipartFile(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path);
		InputStream bufferedInputStream = new BufferedInputStream(resource.getInputStream());
		FileItem fileItem = new DiskFileItem("mainFile",
				tika.detect(bufferedInputStream),
				false,
				resource.getFilename(),
				bufferedInputStream.available(),
				null
		);
		IOUtils.copy(bufferedInputStream, fileItem.getOutputStream());
		return new CommonsMultipartFile(fileItem);
	}
}
