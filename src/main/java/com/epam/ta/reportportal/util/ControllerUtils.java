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

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.*;

/**
 * @author Konstantin Antipin
 */
public class ControllerUtils {

	/**
	 * Tries to find request part or file with specified name in multipart attachments
	 * map.
	 *
	 * @param filename File name
	 * @param files    Files map
	 * @return Found file
	 */
	public static MultipartFile findByFileName(String filename, Map<String, MultipartFile> files) {
		/* Request part name? */
		if (files.containsKey(filename)) {
			return files.get(filename);
		}
		/* Filename? */
		for (MultipartFile file : files.values()) {
			if (filename.equals(file.getOriginalFilename())) {
				return file;
			}
		}
		return null;
	}

	public static Long safeParseLong(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "The provided parameter must be a number");
		}
	}
	public static Integer safeParseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "The provided parameter must be a number");
		}
	}

	public static void validateSaveRQ(Validator validator, SaveLogRQ saveLogRQ) {
		Set<ConstraintViolation<SaveLogRQ>> constraintViolations = validator.validate(saveLogRQ);
		if (constraintViolations != null && !constraintViolations.isEmpty()) {
			StringBuilder messageBuilder = new StringBuilder();
			for (ConstraintViolation<SaveLogRQ> constraintViolation : constraintViolations) {
				messageBuilder.append("[");
				messageBuilder.append("Incorrect value in save log request '");
				messageBuilder.append(constraintViolation.getInvalidValue());
				messageBuilder.append("' in field '");
				Iterator<Path.Node> iterator = constraintViolation.getPropertyPath().iterator();
				messageBuilder.append(iterator.hasNext() ? iterator.next().getName() : "");
				messageBuilder.append("'.]");
			}
			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, messageBuilder.toString());
		}
	}

	public static Map<String, MultipartFile> getUploadedFiles(HttpServletRequest request) {
		Map<String, MultipartFile> uploadedFiles = new HashMap<>();
		if (request instanceof MultipartHttpServletRequest) {
			MultiValueMap<String, MultipartFile> multiFileMap = (((MultipartHttpServletRequest) request)).getMultiFileMap();
			for (List<MultipartFile> multipartFiles : multiFileMap.values()) {
				for (MultipartFile file : multipartFiles) {
					uploadedFiles.put(file.getOriginalFilename(), file);
				}
			}
		}
		return uploadedFiles;
	}
}
