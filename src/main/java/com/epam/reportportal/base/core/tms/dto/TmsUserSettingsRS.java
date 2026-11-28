package com.epam.reportportal.base.core.tms.dto;

import java.util.Map;

/**
 * TMS list settings of the current user in a project. Empty when nothing is stored yet.
 *
 * @param settings stored keys and values
 */
public record TmsUserSettingsRS(Map<String, Object> settings) {

}
