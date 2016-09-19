/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
 
package com.epam.ta.reportportal.ws.converter.builders;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

/**
 * Builder for spring resources. Resource is a paged entity
 * 
 * @author Andrei Varabyeu
 * 
 * @param <T>
 */
public abstract class ResourceBuilder<T extends ResourceSupport> extends Builder<T> {

	public ResourceBuilder<T> addLink(Link link) {
		getObject().add(link);
		return this;
	}
}