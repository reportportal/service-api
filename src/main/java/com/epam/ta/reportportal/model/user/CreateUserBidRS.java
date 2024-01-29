/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class CreateUserBidRS {

	@JsonProperty(value = "message")
	private String message;

	@JsonProperty(value = "bid")
	private String bid;

	@JsonProperty(value = "backLink")
	private String backLink;

	public void setMessage(String value) {
		this.message = value;
	}

	public String getMessage() {
		return message;
	}

	public void setBid(String uuid) {
		this.bid = uuid;
	}

	public String getBid() {
		return bid;
	}

	public void setBackLink(String link) {
		this.backLink = link;
	}

	public String getBackLink() {
		return backLink;
	}
}