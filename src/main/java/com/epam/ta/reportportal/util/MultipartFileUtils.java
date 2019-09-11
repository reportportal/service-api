package com.epam.ta.reportportal.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

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
		InputStream inputStream = resource.getInputStream();
		FileItem fileItem = new DiskFileItem("mainFile",
				tika.detect(inputStream),
				false,
				resource.getFilename(),
				inputStream.available(),
				null
		);
		IOUtils.copy(inputStream, fileItem.getOutputStream());
		return new CommonsMultipartFile(fileItem);
	}
}
