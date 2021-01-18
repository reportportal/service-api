/*
 * Copyright 2021 EPAM Systems
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
package com.epam.ta.reportportal.core.file;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.api.client.util.Lists;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class DeleteFilesHandler {

	private static final int FILE_ID = 1;
	private static final int CSV_SKIP_LINES = 1;
	private static final int THUMBNAIL_ID = 2;
	private static final int BATCH = 250;

	@Autowired
	private MessageBus messageBus;

	public OperationCompletionRS removeFilesByCsv(MultipartFile file) {
		CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
		try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)).withSkipLines(
				CSV_SKIP_LINES).withCSVParser(parser).build()) {
			List<String[]> attachments = csvReader.readAll();
			List<String> pathsForDelete = Lists.newArrayListWithCapacity(attachments.size());
			attachments.forEach(attachmentLine -> {
				pathsForDelete.add(attachmentLine[FILE_ID]);
				if (!StringUtils.isEmpty(attachmentLine[THUMBNAIL_ID])) {
					pathsForDelete.add(attachmentLine[THUMBNAIL_ID]);
				}
			});
			ListUtils.partition(pathsForDelete, BATCH).forEach(partition -> {
				DeleteAttachmentEvent deleteAttachmentEvent = new DeleteAttachmentEvent();
				deleteAttachmentEvent.setPaths(partition);
				messageBus.publishDeleteAttachmentEvent(deleteAttachmentEvent);
			});
			return new OperationCompletionRS("Csv file " + file.getName() + " is accepted for delete process");
		} catch (Exception e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
		}
	}

}
