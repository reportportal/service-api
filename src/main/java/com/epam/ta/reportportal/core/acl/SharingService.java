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

package com.epam.ta.reportportal.core.acl;

import com.epam.ta.reportportal.core.acl.chain.ChainMessage;
import com.epam.ta.reportportal.core.acl.chain.IChainElement;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.events.SharingModifiedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SharingService {

	private final IChainElement strartOfChain;

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public SharingService(@Qualifier("DashboardChainElement") IChainElement strartOfChain, ApplicationEventPublisher eventPublisher) {
		this.strartOfChain = strartOfChain;
		this.eventPublisher = eventPublisher;
	}

	public void modifySharing(List<? extends Shareable> elements, String userName, String projectName, boolean isShare) {
		ChainMessage chainMessage = new ChainMessage();
		chainMessage.setElements(elements);
		chainMessage.setProjectName(projectName);
		chainMessage.setUserName(userName);
		chainMessage.setShare(isShare);
		strartOfChain.process(chainMessage);
		eventPublisher.publishEvent(new SharingModifiedEvent(elements, userName, projectName, isShare));
	}

}