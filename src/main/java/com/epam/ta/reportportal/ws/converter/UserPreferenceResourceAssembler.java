/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.converter.converters.PreferenceConverter;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import org.springframework.stereotype.Service;

/**
 * Resource assembler for
 * {@link com.epam.ta.reportportal.database.entity.UserPreference} db entity
 *
 * @author Dzmitry_Kavalets
 * @author Pavel_Bortnik
 */
@Service
public class UserPreferenceResourceAssembler extends PagedResourcesAssembler<UserPreference, PreferenceResource> {

	@Override
	public PreferenceResource toResource(UserPreference entity) {
		return PreferenceConverter.TO_RESOURCE.apply(entity);
	}
}
