/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.user;

import com.epam.ta.reportportal.ws.model.ApiKeyRQ;
import com.epam.ta.reportportal.ws.model.ApiKeyRS;
import com.epam.ta.reportportal.ws.model.ApiKeysRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Api keys handler.
 *
 * @author Andrei Piankouski
 */
public interface ApiKeyHandler {

  /**
   * Generate api key for user.
   *
   * @param keyName name of Api Key
   * @param userId User id
   * @return generated api key.
   */
  ApiKeyRS createApiKey(String keyName, Long userId);

  /**
   * Delete users ApiKey by id.
   *
   * @param id Api Key id
   * @return {@link OperationCompletionRS}
   */
  OperationCompletionRS deleteApiKey(Long id);

  /**
   * Return all users ApiKeys.
   *
   * @param userId User id
   * @return list of {@link ApiKeyRQ}
   */
  ApiKeysRS getAllUsersApiKeys(Long userId);

}
