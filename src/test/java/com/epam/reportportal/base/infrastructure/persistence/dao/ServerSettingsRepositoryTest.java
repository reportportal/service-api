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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepositoryCustomImpl.SERVER_SETTING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ServerSettingsRepositoryTest extends BaseMvcTest {

  @Autowired
  private ServerSettingsRepository repository;

  @Test
  public void findSettings() {
    final List<ServerSettings> settings = repository.selectServerSettings();
    assertEquals(4L, settings.size());
    settings.forEach(setting -> assertTrue(setting.getKey().startsWith(SERVER_SETTING_KEY)));
  }

  @Test
  public void generateSecret() {
    final String s = repository.generateSecret();
    final Optional<ServerSettings> byKey = repository.findByKey("secret.key");
    assertTrue(byKey.isPresent());
    assertEquals(s, byKey.get().getValue());
  }
}
