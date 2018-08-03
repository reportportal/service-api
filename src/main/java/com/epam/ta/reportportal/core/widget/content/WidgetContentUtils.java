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

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.entity.widget.ContentField;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public final class WidgetContentUtils {

	private WidgetContentUtils() {
		//static only
	}

	public static final Function<Set<ContentField>, Map<String, List<String>>> GROUP_CONTENT_FIELDS = contentFields -> Optional.ofNullable(
			contentFields)
			.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Content fields shouldn't be null"))
			.stream().collect(Collectors.toMap(ContentField::getFieldName, v -> Lists.newArrayList(v.getValues())));

	public static final Function<Set<WidgetOption>, Map<String, List<String>>> GROUP_WIDGET_OPTIONS = widgetOptions -> Optional.ofNullable(
			widgetOptions)
			.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Widget options shouldn't be null"))
			.stream()
			.collect(Collectors.toMap(WidgetOption::getWidgetOption, v -> Lists.newArrayList(v.getValues())));

}
