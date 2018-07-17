package com.epam.ta.reportportal.core.plugin;

import java.io.Serializable;
import java.util.Objects;

/**
 * ReportPortal plugin details
 *
 * @author Andrei Varabyeu
 */
public class Plugin implements Serializable {

	private String id;
	private String type;

	public Plugin() {

	}

	public Plugin(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
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
