package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request to update the TMS list settings of the current user in a project. Only the given keys
 * change: a value sets the key, {@code null} removes it. Other stored keys stay as they are.
 *
 * @param settings keys to set or remove
 */
public record TmsUserSettingsRQ(
    @NotNull(message = "Settings must be specified")
    @Size(max = TmsUserSettingsRQ.MAX_KEYS_PER_REQUEST)
    Map<@NotBlank @Size(max = TmsUserSettingsRQ.MAX_KEY_LENGTH) String, Object> settings) {

  public static final int MAX_KEYS_PER_REQUEST = 50;
  public static final int MAX_KEY_LENGTH = 128;
}
