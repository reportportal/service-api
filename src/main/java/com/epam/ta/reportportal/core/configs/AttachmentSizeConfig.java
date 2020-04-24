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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Configuration
@ConditionalOnProperty(name = "rp.attachments.recalculate", havingValue = "true")
public class AttachmentSizeConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentSizeConfig.class);

	private static final int CHUNK_SIZE = 10;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier("attachmentDataStoreService")
	private DataStoreService dataStoreService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Bean
	public JdbcCursorItemReader<Attachment> reader() throws Exception {
		String query = "SELECT * from attachment order by id";
		JdbcCursorItemReader<Attachment> reader = new JdbcCursorItemReader<>();
		reader.setSql(query);
		reader.setDataSource(dataSource);
		reader.setRowMapper((rs, rowNum) -> {
			Attachment attachment = new Attachment();
			attachment.setId(rs.getLong("id"));
			attachment.setFileId(rs.getString("file_id"));
			return attachment;
		});
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean
	public ItemProcessor<Attachment, Attachment> processor() {
		return item -> {
			try {
				Optional<InputStream> file = dataStoreService.load(item.getFileId());
				try (final InputStream inputStream = file.get()) {
					item.setFileSize(StreamUtils.copyToByteArray(inputStream).length);
					return item;
				}
			} catch (ReportPortalException e) {
				LOGGER.debug(Suppliers.formattedSupplier("File with id {} is not presented at the file system. Removing from the database.",
						item.getId()
				).get());
				jdbcTemplate.update("DELETE FROM attachment WHERE id = ?", item.getId());
				return null;
			}
		};
	}

	@Bean
	public ItemWriter<Attachment> writer() {
		return items -> items.forEach(item -> {
			jdbcTemplate.update("UPDATE attachment SET file_size = ? WHERE id = ?", item.getFileSize(), item.getId());
		});
	}

	@Bean
	public Step attachmentSizeStep() throws Exception {
		return stepBuilderFactory.get("attachment").<Attachment, Attachment>chunk(CHUNK_SIZE).reader(reader())
				.processor(processor())
				.writer(writer())
				.allowStartIfComplete(true)
				.build();
	}

}
