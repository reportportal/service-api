/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.service;

import com.epam.reportportal.commons.ContentTypeResolver;
import com.epam.reportportal.commons.Thumbnailator;
import com.epam.ta.reportportal.BinaryData;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.filesystem.FilePathGenerator;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Dzianis_Shybeka
 */
@Service
public class DataStoreService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreService.class);

	private final DataStore dataStore;

	private final Thumbnailator thumbnailator;

	private final ContentTypeResolver contentTypeResolver;

	private final FilePathGenerator filePathGenerator;

	private final DataEncoder dataEncoder;

	public DataStoreService(DataStore dataStore, Thumbnailator thumbnailator, ContentTypeResolver contentTypeResolver,
			FilePathGenerator filePathGenerator, DataEncoder dataEncoder) {
		this.dataStore = dataStore;
		this.thumbnailator = thumbnailator;
		this.contentTypeResolver = contentTypeResolver;
		this.filePathGenerator = filePathGenerator;
		this.dataEncoder = dataEncoder;
	}

	public Optional<BinaryDataMetaInfo> save(Long projectId, MultipartFile file) {

		Optional<BinaryDataMetaInfo> maybeResult = Optional.empty();

		try {

			BinaryData binaryData = getBinaryData(file);

			String generatedFilePath = filePathGenerator.generate();
			String commonPath = Paths.get(projectId.toString(), generatedFilePath).toString();
			Path targetPath = Paths.get(commonPath, file.getOriginalFilename());

			String thumbnailFilePath = null;
			if (isImage(binaryData.getContentType())) {

				try {

					Path thumbnailTargetPath = Paths.get(commonPath, "thumbnail-".concat(file.getName()));

					InputStream thumbnailStream = thumbnailator.createThumbnail(file.getInputStream());

					thumbnailFilePath = dataStore.save(thumbnailTargetPath.toString(), thumbnailStream);
				} catch (IOException e) {
					// do not propogate. Thumbnail is not so critical
					LOGGER.error("Thumbnail is not created for file [{}]. Error:\n{}", file.getOriginalFilename(), e);
				}
			}

			/*
			 * Saves binary data into storage
			 */
			String filePath = dataStore.save(targetPath.toString(), binaryData.getInputStream());

			maybeResult = Optional.of(BinaryDataMetaInfo.BinaryDataMetaInfoBuilder.aBinaryDataMetaInfo()
					.withFileId(dataEncoder.encode(filePath))
					.withThumbnailFileId(dataEncoder.encode(thumbnailFilePath))
					.build());

		} catch (IOException e) {
			LOGGER.error("Unable to save binary data", e);
		} finally {
			if (file instanceof CommonsMultipartFile) {
				((CommonsMultipartFile) file).getFileItem().delete();
			}
		}

		return maybeResult;
	}

	public InputStream load(String fileId) {

		return dataStore.load(dataEncoder.decode(fileId));
	}

	public void delete(String filePath) {

		dataStore.delete(dataEncoder.decode(filePath));
	}

	private BinaryData getBinaryData(MultipartFile file) throws IOException {

		BinaryData binaryData;
		boolean isContentTypePresented =
				!Strings.isNullOrEmpty(file.getContentType()) && !MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(file.getContentType());
		if (isContentTypePresented) {
			binaryData = new BinaryData(file.getContentType(), file.getSize(), file.getInputStream());
		} else {
			binaryData = new BinaryData(contentTypeResolver.detectContentType(file.getInputStream()),
					file.getSize(),
					file.getInputStream()
			);
		}
		return binaryData;
	}

	private boolean isImage(String contentType) {
		return contentType != null && contentType.contains("image");
	}
}
