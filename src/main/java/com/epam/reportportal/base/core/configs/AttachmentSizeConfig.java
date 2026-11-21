/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.InputStream;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StreamUtils;

/**
 * Optional batch to recalculate stored attachment sizes.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "rp.attachments.recalculate", havingValue = "true")
public class AttachmentSizeConfig {

  private static final int CHUNK_SIZE = 10;

  private final DataSource dataSource;
  private final DataStoreService dataStoreService;
  private final JdbcTemplate jdbcTemplate;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  public AttachmentSizeConfig(DataSource dataSource,
      @Qualifier("attachmentDataStoreService") DataStoreService dataStoreService,
      JdbcTemplate jdbcTemplate, JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    this.dataSource = dataSource;
    this.dataStoreService = dataStoreService;
    this.jdbcTemplate = jdbcTemplate;
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  @Bean
  public JdbcCursorItemReader<Attachment> reader() {
    String query = "SELECT * from attachment order by id";
    return new JdbcCursorItemReaderBuilder<Attachment>()
        .name("attachmentReader")
        .dataSource(dataSource)
        .sql(query)
        .rowMapper((rs, rowNum) -> {
          Attachment attachment = new Attachment();
          attachment.setId(rs.getLong("id"));
          attachment.setFileId(rs.getString("file_id"));
          return attachment;
        })
        .build();
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
      } catch (ReportPortalException _) {
        log.debug(Suppliers.formattedSupplier(
            "File with id {} is not presented at the file system. Removing from the database.",
            item.getId()
        ).get());
        jdbcTemplate.update("DELETE FROM attachment WHERE id = ?", item.getId());
        return null;
      }
    };
  }

  @Bean
  public ItemWriter<Attachment> writer() {
    return items -> items
        .forEach(item -> jdbcTemplate
            .update("UPDATE attachment SET file_size = ? WHERE id = ?", item.getFileSize(), item.getId()));
  }

  @Bean
  public Step attachmentSizeStep() {
    return new StepBuilder("attachment", jobRepository)
        .<Attachment, Attachment>chunk(CHUNK_SIZE)
        .transactionManager(transactionManager)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .allowStartIfComplete(true)
        .build();
  }

}
