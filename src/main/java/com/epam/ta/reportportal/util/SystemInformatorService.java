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
package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.ws.model.SystemInfoRS;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;

/**
 * System load information
 *
 * @author Andrei_Ramanchuk
 */
@Service("systemInfo")
public class SystemInformatorService {

	private String OS_FORMATTER = "%s ver.%s arch.%s";
	private OperatingSystemMXBean oper;

	public SystemInformatorService() {
		this.oper = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	}

	public SystemInfoRS getSystemInformation() {
		SystemInfoRS response = new SystemInfoRS();
		response.setOsVersion(this.getFormattedOs());
		response.setCpuUsage(this.getFormattedCPU());
		response.setMemUsage(this.getMemLoad());
		return response;
	}

	/**
	 * Get formatted string of server OS
	 */
	private String getFormattedOs() {
		return String.format(OS_FORMATTER, oper.getName(), oper.getVersion(), oper.getArch());
	}

	/**
	 * Get formatted string of server CPU usage in percent
	 */
	private float getFormattedCPU() {
		return Double.valueOf(oper.getSystemCpuLoad() * 100).floatValue();
	}

	/**
	 * Get formatted string of server MEM usage in percent
	 */
	private float getMemLoad() {
		float leftPerc = ((float) oper.getFreePhysicalMemorySize() / oper.getTotalPhysicalMemorySize()) * 100;
		return 100 - leftPerc;
	}
}