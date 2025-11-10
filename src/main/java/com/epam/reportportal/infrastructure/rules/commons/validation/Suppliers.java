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

package com.epam.reportportal.infrastructure.rules.commons.validation;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import java.util.Arrays;
import java.util.function.Supplier;
import org.slf4j.helpers.MessageFormatter;

/**
 * Several useful suppliers.
 *
 * @author Andrei Varabyeu
 */
public class Suppliers {

  private static final CharMatcher REPLACEMENT_SYMBOLS = CharMatcher.anyOf("{}");

  private Suppliers() {

  }

  /**
   * Preemptive supplier for string. Do not do any actions with string
   *
   * @param string String to be supplied
   * @return Supplied String
   */
  public static Supplier<String> stringSupplier(final String string) {
    return () -> string;
  }

  /**
   * Formatted supplier. Applies {@link MessageFormatter} to be able to pass formatted string with parameters as we did
   * int slf4j. Good approach to avoid string concatenation before we really need it
   *
   * @param string     String to be supplied
   * @param parameters Formatter parameters
   * @return Supplied String
   */
  public static Supplier<String> formattedSupplier(final String string,
      final Object... parameters) {
    return new Supplier<String>() {
      @Override
      public String get() {
        return clearPlaceholders(MessageFormatter.arrayFormat(string, parameters).getMessage());
      }

      @Override
      public String toString() {
        return get();
      }
    };
  }

  /**
   * Formatted supplier. Applies {@link MessageFormatter} to be able to pass formatted string with parameters as we did
   * int slf4j. Good approach to avoid string concatenation before we really need it
   *
   * @param string     String to be supplied
   * @param parameters Formatter parameters
   * @return Supplied String
   */
  public static Supplier<String> formattedSupplier(final String string,
      final Supplier<?>... parameters) {
    return new Supplier<String>() {
      @Override
      public String get() {
        return clearPlaceholders(MessageFormatter.arrayFormat(string,
                Arrays.stream(parameters).map(Supplier::get).toArray())
            .getMessage());
      }

      @Override
      public String toString() {
        return get();
      }
    };
  }

  /**
   * Clears placeholders in the message
   *
   * @param message Message to be cleared
   * @return Cleared string
   */
  @VisibleForTesting
  public static String clearPlaceholders(String message) {
    String cleared = message;
    if (formattedMessage(message)) {
      cleared = REPLACEMENT_SYMBOLS.removeFrom(message).trim();
    }
    return cleared;
  }

  /**
   * Checks whether placeholder characters are present in the string
   *
   * @param str String to check
   * @return TRUE if at least one placeholder symbol is present
   */
  public static boolean formattedMessage(String str) {
    return !isNullOrEmpty(str) && REPLACEMENT_SYMBOLS.matchesAnyOf(str);
  }

  public static String trimMessage(String message, int maxLength) {
    if (message.length() > maxLength) {
      return message.substring(0, maxLength);
    }
    return message;
  }

}
