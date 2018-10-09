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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.user.RestorePasswordBid;
import com.epam.ta.reportportal.ws.model.user.RestorePasswordRQ;
import com.google.common.base.Preconditions;

import java.util.UUID;
import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class RestorePasswordBidConverter {

	private RestorePasswordBidConverter() {
		//static only
	}

	public static final Function<RestorePasswordRQ, RestorePasswordBid> TO_BID = request -> {
		Preconditions.checkNotNull(request);
		RestorePasswordBid bid = new RestorePasswordBid();
		bid.setEmail(request.getEmail());
		bid.setUuid(UUID.randomUUID().toString());
		return bid;
	};
}
