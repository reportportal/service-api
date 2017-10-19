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

import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharing chain element for processing widgets.
 *
 * @author Aliaksei_Makayed
 */
@Service("WidgetChainElement")
public class WidgetChainElement extends ChainElement {

	@Autowired
	private UserFilterRepository userFilterRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	public WidgetChainElement(@Qualifier("UserFilterChainElement") IChainElement nextChainElement) {
		super(nextChainElement);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isCanHandle(List<? extends Shareable> elementsToProcess) {
		return Widget.class.equals(elementsToProcess.get(0).getClass());
	}

	@Override
	public List<? extends Shareable> getNextElements(List<? extends Shareable> elementsToProcess, String ownerId) {
		Set<String> ids = elementsToProcess.stream().map(it -> (Widget) it).map(Widget::getApplyingFilterId).collect(Collectors.toSet());
		if (!ids.isEmpty()) {
			return userFilterRepository.findOnlyOwnedEntities(ids, ownerId);
		}
		return Collections.emptyList();
	}

	@Override
	public void saveElements(List<? extends Shareable> elementsToProcess) {
		widgetRepository.save(elementsToProcess.stream().map(input -> (Widget) input).collect(Collectors.toList()));
	}

}