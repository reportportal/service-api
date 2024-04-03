/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.validation.constraints.NotNull;

/**
 * JSON Representation of ReportPortal's Log domain object
 *
 * @author Andrei Varabyeu
 */
@JsonInclude(Include.NON_NULL)
public class LogResource {

	@JsonInclude(Include.NON_NULL)
	public static class BinaryContent {

		@NotNull
		@JsonProperty(value = "id", required = true)
		private String binaryDataId;

		@JsonProperty(value = "thumbnailId", required = true)
		private String thumbnailId;

		@JsonProperty(value = "contentType", required = true)
		private String contentType;

		/**
		 * @return the binaryDataId
		 */
		public String getBinaryDataId() {
			return binaryDataId;
		}

		/**
		 * @param binaryDataId the binaryDataId to set
		 */
		public void setBinaryDataId(String binaryDataId) {
			this.binaryDataId = binaryDataId;
		}

		/**
		 * @return the thumbnailId
		 */
		public String getThumbnailId() {
			return thumbnailId;
		}

		/**
		 * @param thumbnailId the thumbnailId to set
		 */
		public void setThumbnailId(String thumbnailId) {
			this.thumbnailId = thumbnailId;
		}

		/**
		 * @return the contentType
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * @param contentType the contentType to set
		 */
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("BinaryContent{");
			sb.append("binaryDataId='").append(binaryDataId).append('\'');
			sb.append(", thumbnailId='").append(thumbnailId).append('\'');
			sb.append(", contentType='").append(contentType).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	@JsonProperty(value = "id", required = true)
	private Long id;

	@JsonProperty(value = "uuid", required = true)
	private String uuid;

	@JsonProperty(value = "time")
	private Date logTime;

	@JsonProperty(value = "message")
	private String message;

	@JsonProperty(value = "binaryContent")
	private BinaryContent binaryContent;

	@JsonProperty(value = "thumbnail")
	private String thumbnail;

	@JsonProperty(value = "level")
	@Schema(allowableValues = "error, warn, info, debug, trace, fatal, unknown")
	private String level;

	@JsonProperty(value = "itemId")
	private Long itemId;

	@JsonProperty(value = "launchId")
	private Long launchId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLogTime() {
		return logTime;
	}

	public String getUuid() {
		return uuid;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public void setBinaryContent(BinaryContent binaryContent) {
		this.binaryContent = binaryContent;
	}

	public BinaryContent getBinaryContent() {
		return binaryContent;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("LogResource{");
		sb.append("id=").append(id);
		sb.append(", uuid='").append(uuid).append('\'');
		sb.append(", logTime=").append(logTime);
		sb.append(", message='").append(message).append('\'');
		sb.append(", binaryContent=").append(binaryContent);
		sb.append(", thumbnail='").append(thumbnail).append('\'');
		sb.append(", level='").append(level).append('\'');
		sb.append(", itemId=").append(itemId);
		sb.append(", launchId=").append(launchId);
		sb.append('}');
		return sb.toString();
	}
}
