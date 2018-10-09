/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.bts;

import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Contains functionality for providing external system strategy by system name.
 *
 * @author Aliaksei_Makayed
 */

@Service
public class StrategyProvider {

	/**
	 * Validate external system name and provide strategy for interacting with
	 * external system.
	 *
	 * @param externalSystemName Name of external system
	 * @return
	 */
	public ExternalSystemStrategy getStrategy(String externalSystemName) {
		return new ExternalSystemStrategy() {
			@Override
			public boolean connectionTest(BugTrackingSystem system) {
				return false;
			}

			@Override
			public Optional<Ticket> getTicket(String id, BugTrackingSystem system) {
				return Optional.empty();
			}

			@Override
			public Ticket submitTicket(PostTicketRQ ticketRQ, BugTrackingSystem system) {
				return null;
			}

			@Override
			public List<PostFormField> getTicketFields(String issueType, BugTrackingSystem system) {
				return null;
			}

			@Override
			public List<String> getIssueTypes(BugTrackingSystem system) {
				return null;
			}
		};
		//		externalSystemStrategy.checkAvailable(externalSystemName);
		//		return externalSystemStrategy;
	}
}