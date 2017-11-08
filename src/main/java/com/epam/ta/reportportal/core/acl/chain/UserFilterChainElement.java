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
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharing chain element for processing user filters.
 *
 * @author Aliaksei_Makayed
 */
@Service("UserFilterChainElement")
public class UserFilterChainElement extends ChainElement {

	@Autowired
	private UserFilterRepository userFilterRepository;

	public UserFilterChainElement() {
		super(null);
	}

	public UserFilterChainElement(IChainElement nextChainElement) {
		super(nextChainElement);
	}

	@Override
	public boolean isCanHandle(List<? extends Shareable> elementsToProcess) {
		return UserFilter.class.equals(elementsToProcess.get(0).getClass());
	}

	@Override
	public List<? extends Shareable> getNextElements(List<? extends Shareable> elementsToProcess, String owner) {
		//In current implementation user filter it's last element on chain so return null
		return null;
	}

	@Override
	public void saveElements(List<? extends Shareable> elementsToProcess) {
		userFilterRepository.save(elementsToProcess.stream().map(i -> (UserFilter) i).collect(Collectors.toList()));
	}

}