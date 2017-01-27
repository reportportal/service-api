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

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.ws.controller.impl.LogController;
import com.epam.ta.reportportal.ws.converter.builders.LogResourceBuilder;
import com.epam.ta.reportportal.ws.model.log.LogResource;

/**
 * Assembler for LogResources
 * 
 * @author Andrei Varabyeu
 * 
 */
@Service
public class LogResourceAssembler extends ProjectRelatedResourceAssembler<Log, LogResource> {

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	@Autowired
	public LogResourceAssembler(TestItemRepository testItemRepository, LaunchRepository launchRepository) {
		super(LogController.class, LogResource.class);
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
	}

	@Override
	public LogResource toResource(Log log) {
		return this.toResource(log, null);
	}

	@Override
	public LogResource toResource(Log log, String project) {
		Link link = ControllerLinkBuilder.linkTo(LogController.class, project == null
				? launchRepository.findOne(testItemRepository.findOne(log.getTestItemRef()).getLaunchRef()).getProjectRef() : project)
				.slash(log).withSelfRel();
		// Removed lazy reference for performance increasing
		return new LogResourceBuilder().addLog(log).addLink(link).build();
	}
}
