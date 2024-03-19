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

package com.epam.ta.reportportal.model.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Issue sub-type resource representation
 * 
 * @author Andrei_Ramanchuk
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueSubTypeResource {

	@JsonProperty(value = "id")
	private Long id;

	@JsonProperty(value = "locator")
	private String locator;

	@JsonProperty(value = "typeRef")
	private String typeRef;

	@JsonProperty(value = "longName")
	private String longName;

	@JsonProperty(value = "shortName")
	private String shortName;

	@JsonProperty(value = "color")
	private String color;

	public IssueSubTypeResource() {

	}

	public IssueSubTypeResource(Long id, String locator, String typeRef, String longName, String shortName, String color) {
		this.id = id;
		this.locator = locator;
		this.typeRef = typeRef;
		this.longName = longName;
		this.shortName = shortName;
		this.color = color;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	public String getTypeRef() {
		return typeRef;
	}

	public void setTypeRef(String typeRef) {
		this.typeRef = typeRef;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
