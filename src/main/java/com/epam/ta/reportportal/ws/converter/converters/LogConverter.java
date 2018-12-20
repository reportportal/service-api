package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.google.common.base.Preconditions;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
		resource.setId(model.getId());
		resource.setMessage(ofNullable(model.getLogMessage()).orElse("NULL"));
		resource.setLogTime(EntityUtils.TO_DATE.apply(model.getLogTime()));

		if (isBinaryDataExists(model)) {

			LogResource.BinaryContent binaryContent = new LogResource.BinaryContent();
			binaryContent.setBinaryDataId(model.getAttachment());
			binaryContent.setContentType(model.getContentType());
			binaryContent.setThumbnailId(model.getAttachmentThumbnail());
			resource.setBinaryContent(binaryContent);
		}

		ofNullable(model.getTestItem()).ifPresent(testItem -> resource.setTestItem(String.valueOf(testItem.getItemId())));
		ofNullable(model.getLogLevel()).ifPresent(level -> resource.setLevel(LogLevel.toLevel(level).toString()));
		return resource;
	};

	private static boolean isBinaryDataExists(Log log) {
		return isNotEmpty(log.getContentType()) || isNotEmpty(log.getAttachmentThumbnail()) || isNotEmpty(log.getAttachment());
	}

}
