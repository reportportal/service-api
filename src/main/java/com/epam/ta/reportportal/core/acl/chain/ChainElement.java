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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;

import java.util.List;
import java.util.Objects;

/**
 * Abstract chain element, contains common behavior(logic) for all chain
 * elements.
 *
 * @author Aliaksei_Makayed
 */
abstract class ChainElement implements IChainElement {

	private IChainElement nextChainElement;

	public ChainElement(IChainElement nextChainElement) {
		this.nextChainElement = nextChainElement;
	}

	@Override
	public boolean process(ChainMessage message) {
		if (!Preconditions.NOT_EMPTY_COLLECTION.test(message.getElements())) {
			return false;
		}
		if (!isCanHandle(message.getElements())) {
			if (nextChainElement == null) {
				return false;
			}
			return nextChainElement.process(message);
		}
		message.getElements()
				.stream()
				.filter(Objects::nonNull)
				.forEach(shareable -> AclUtils.modifyACL(shareable.getAcl(), message.getProjectName(), message.getUserName(),
						message.isShare()
				));
		if (message.isSave()) {
			saveElements(message.getElements());
		}
		List<? extends Shareable> nextIterationElements = getNextElements(message.getElements(), message.getUserName());
		if (nextChainElement == null) {
			return true;
		}
		/*
		 * If chain more than just filter and going to be share=false, then keep
		 * user filter as share=true
		 */
		else if ((nextChainElement instanceof UserFilterChainElement) && (!message.isShare())) {
			return true;
		} else {
			message.setElements(nextIterationElements);
			message.setSave(true);
			return nextChainElement.process(message);
		}
	}

	public abstract boolean isCanHandle(List<? extends Shareable> elementsToProcess);

	public abstract List<? extends Shareable> getNextElements(List<? extends Shareable> elementsToProcess, String owner);

	/**
	 * Save elements related to current chain element
	 *
	 * @param elementsToProcess
	 */
	public abstract void saveElements(List<? extends Shareable> elementsToProcess);

}