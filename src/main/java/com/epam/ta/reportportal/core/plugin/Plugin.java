/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.plugin;

import com.epam.reportportal.extension.common.ExtensionPoint;

import java.io.Serializable;
import java.util.Objects;

/**
 * ReportPortal plugin details
 *
 * @author Andrei Varabyeu
 */
public class Plugin implements Serializable {

	private String id;
	private ExtensionPoint type;

	public Plugin() {

	}

	public Plugin(String id, ExtensionPoint type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ExtensionPoint getType() {
		return type;
	}

	public void setType(ExtensionPoint type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Plugin plugin = (Plugin) o;
		return Objects.equals(type, plugin.type);
	}

	@Override
	public int hashCode() {

		return Objects.hash(type);
	}

	@Override
	public String toString() {
		return "Plugin{" + "type='" + type + '\'' + '}';
	}
}
