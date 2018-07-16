package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;
import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class LogConverter {

	private LogConverter() {
		//static only
	}

	public static final Function<Log, LogResource> TO_RESOURCE = model -> {

		Preconditions.checkNotNull(model);

		LogResource resource = new LogResource();
		resource.setIdLog(String.valueOf(model.getId()));
		resource.setMessage(Optional.ofNullable(model.getLogMessage()).orElse("NULL"));
		resource.setLogTime(EntityUtils.TO_DATE.apply(model.getLogTime()));

		if (isBinaryDataExists(model)) {

			LogResource.BinaryContent binaryContent = new LogResource.BinaryContent();
			binaryContent.setBinaryDataId(model.getAttachment());
			binaryContent.setContentType(model.getContentType());
			binaryContent.setThumbnailId(model.getAttachmentThumbnail());
			resource.setBinaryContent(binaryContent);
		}

		Optional.ofNullable(model.getTestItem()).ifPresent(testItem -> {

			resource.setTestItem(String.valueOf(testItem.getItemId()));
		});

		Optional.ofNullable(model.getLogLevel()).ifPresent(level -> {
			resource.setLevel(level.toString());
		});
		return resource;
	};

	private static boolean isBinaryDataExists(Log log) {

		return StringUtils.isNotEmpty(log.getContentType()) || StringUtils.isNotEmpty(log.getAttachmentThumbnail())
				|| StringUtils.isNotEmpty(log.getAttachment());
	}

}
