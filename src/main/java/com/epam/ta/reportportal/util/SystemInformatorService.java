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
		return (float) (oper.getSystemCpuLoad() * 100);
	}

	/**
	 * Get formatted string of server MEM usage in percent
	 */
	private float getMemLoad() {
		float leftPerc = ((float) oper.getFreePhysicalMemorySize() / oper.getTotalPhysicalMemorySize()) * 100;
		return 100 - leftPerc;
	}
}