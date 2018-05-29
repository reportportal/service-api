/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.store.database.entity.log;

import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "log", schema = "public", indexes = { @Index(name = "log_pk", unique = true, columnList = "id ASC") })
public class Log implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "log_time", nullable = false)
	private LocalDateTime logTime;

	@Column(name = "log_message", nullable = false)
	private String logMessage;

	@LastModifiedDate
	@Column(name = "last_modified", nullable = false)
	private LocalDateTime lastModified;

	@Column(name = "log_level", nullable = false, precision = 32)
	private Integer logLevel;

	@ManyToOne
	@JoinColumn(name = "item_id")
	private TestItem testItem;

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "thumbnail_file_path")
	private String thumbnailFilePath;

	@Column(name = "content_type")
	private String contentType;

	public Log(Long id, LocalDateTime logTime, String logMessage, LocalDateTime lastModified, Integer logLevel, TestItem testItem) {
		this.id = id;
		this.logTime = logTime;
		this.logMessage = logMessage;
		this.lastModified = lastModified;
		this.logLevel = logLevel;
		this.testItem = testItem;
	}

	public Log() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TestItem getTestItem() {
		return testItem;
	}

	public void setTestItem(TestItem testItem) {
		this.testItem = testItem;
	}

	public LocalDateTime getLogTime() {
		return logTime;
	}

	public void setLogTime(LocalDateTime logTime) {
		this.logTime = logTime;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public Integer getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Integer logLevel) {
		this.logLevel = logLevel;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getThumbnailFilePath() {
		return thumbnailFilePath;
	}

	public void setThumbnailFilePath(String thumbnailFilePath) {
		this.thumbnailFilePath = thumbnailFilePath;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Log log = (Log) o;
		return Objects.equals(id, log.id) && Objects.equals(logTime, log.logTime) && Objects.equals(logMessage, log.logMessage)
				&& Objects.equals(lastModified, log.lastModified) && Objects.equals(logLevel, log.logLevel) && Objects.equals(testItem,
				log.testItem
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, logTime, logMessage, lastModified, logLevel, testItem);
	}

	@Override
	public String toString() {
		return "Log{" + "id=" + id + ", logTime=" + logTime + ", logMessage='" + logMessage + '\'' + ", lastModified=" + lastModified
				+ ", logLevel=" + logLevel + '}';
	}
}
