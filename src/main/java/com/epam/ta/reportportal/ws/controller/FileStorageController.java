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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.BinaryData;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.service.DataStoreService;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

/**
 * @author Dzianis_Shybeka
 */
@RestController
@RequestMapping("/data")
public class FileStorageController {

	private final DataStoreService dataStoreService;

	public FileStorageController(DataStoreService dataStoreService) {
		this.dataStoreService = dataStoreService;
	}

	@GetMapping(value = "/{dataId}")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public void getFile(@PathVariable("dataId") String dataId, HttpServletResponse response) {
		toResponse(response, dataStoreService.load(dataId));
	}

	/**
	 * Copies provided {@link BinaryData} to Response
	 *
	 * @param response    Response
	 * @param inputStream Stored data
	 */
	private void toResponse(HttpServletResponse response, InputStream inputStream) {
		if (inputStream != null) {

			try {
				IOUtils.copy(inputStream, response.getOutputStream());
			} catch (IOException e) {
				throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
			}
		} else {
			response.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}
}
