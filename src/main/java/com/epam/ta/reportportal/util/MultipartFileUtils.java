/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		//TODO investigate stream closing requirement
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
