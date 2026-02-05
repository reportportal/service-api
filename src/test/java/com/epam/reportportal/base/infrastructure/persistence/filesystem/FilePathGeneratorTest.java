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

package com.epam.reportportal.base.infrastructure.persistence.filesystem;

import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.AttachmentMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.util.DateTimeProvider;
import java.io.File;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FilePathGeneratorTest {

  private DateTimeProvider dateTimeProvider;

  @BeforeEach
  void setUp() {
    dateTimeProvider = Mockito.mock(DateTimeProvider.class);
  }

  @Test
  void generate_different_even_for_same_date() {

    //given:
    AttachmentMetaInfo metaInfo = AttachmentMetaInfo.builder().withProjectId(1L)
        .withLaunchUuid("271b5881-9a62-4df4-b477-335a96acbe14").build();

    LocalDateTime date = LocalDateTime.of(2018, 5, 28, 3, 3);
    when(dateTimeProvider.localDateTimeNow()).thenReturn(date);
    //

    //when:
    String pathOne = new FilePathGenerator(dateTimeProvider).generate(metaInfo);

    Assertions.assertThat(pathOne).isEqualTo(
        "1" + File.separator + "2018-5" + File.separator + "271b5881-9a62-4df4-b477-335a96acbe14");
  }
}
