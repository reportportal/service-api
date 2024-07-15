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

package com.epam.ta.reportportal.reporting.async.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class MessageRetriever {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      MessageRetriever.class);

  private final ObjectMapper objectMapper;
  private final Validator validator;

  public MessageRetriever(ObjectMapper objectMapper, Validator validator) {
    this.objectMapper = objectMapper;
    this.validator = validator;
  }

  public <T> Optional<T> retrieveValid(Message income, Class<T> type) {
    String incomeMessage = new String(income.getBody(), StandardCharsets.UTF_8);
    try {
      T object = objectMapper.readValue(incomeMessage, type);
      Set<ConstraintViolation<Object>> violations = validator.validate(object);
      if (violations.isEmpty()) {
        return Optional.of(object);
      }
      throw new ConstraintViolationException(violations);
    } catch (ConstraintViolationException e) {
      LOGGER.error("Incorrect incoming message. Message violations: {}",
          e.getMessage());
    } catch (JsonProcessingException e) {
      LOGGER.error("Incorrect json format of incoming message. Discarded message: {}",
          incomeMessage);
    }
    return Optional.empty();
  }

}
