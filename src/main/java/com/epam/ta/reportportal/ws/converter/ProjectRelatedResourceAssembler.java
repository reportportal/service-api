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

import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

/**
 * Extension of {@link PagedResourcesAssembler}. Added for improving performance of resource building operations.<br>
 * Added possibility to build paged resource with specified project.
 *
 * @param <T>
 * @param <R>
 * @author Aliaksei_Makayed
 */
public abstract class ProjectRelatedResourceAssembler<T, R> extends PagedResourcesAssembler<T, R> {


    public abstract R toResource(T element, String project);


    /**
     * Creates {@link com.epam.ta.reportportal.ws.model.Page} from {@link Page} DB query result
     *
     * @param content
     * @return
     */
    public com.epam.ta.reportportal.ws.model.Page<R> toPagedResources(Page<T> content, final String project) {
        Assert.notNull(content);

        return new com.epam.ta.reportportal.ws.model.Page<R>(content.getContent().stream()
                    .map(c -> toResource(c, project)).collect(Collectors.toList()),
                new com.epam.ta.reportportal.ws.model.Page.PageMetadata(content.getSize(),
                        content.getNumber() + 1, content.getTotalElements(), content.getTotalPages()));
    }

}
