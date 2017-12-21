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

package com.epam.ta.reportportal.core.acl.chain;

import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharing chain element for processing dashboards.
 *
 * @author Aliaksei_Makayed
 */
@Service("DashboardChainElement")
public class DashboardChainElement extends ChainElement {

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	public DashboardChainElement(@Qualifier("WidgetChainElement") IChainElement nextChainElement) {
		super(nextChainElement);
	}

	@Override
	public boolean isCanHandle(List<? extends Shareable> elementsToProcess) {
		return Dashboard.class.equals(elementsToProcess.get(0).getClass());
	}

	@Override
	public List<? extends Shareable> getNextElements(List<? extends Shareable> elementsToProcess, String owner) {
		Set<String> ids = elementsToProcess.stream()
				.map(e -> (Dashboard) e)
				.flatMap(d -> d.getWidgets().stream())
				.map(Dashboard.WidgetObject::getWidgetId)
				.collect(Collectors.toSet());
		if (!ids.isEmpty()) {
			return widgetRepository.findOnlyOwnedEntities(ids, owner);
		}
		return Collections.emptyList();
	}

	@Override
	public void saveElements(List<? extends Shareable> elementsToProcess) {
		dashboardRepository.save(elementsToProcess.stream().map(input -> (Dashboard) input).collect(Collectors.toList()));
	}
}