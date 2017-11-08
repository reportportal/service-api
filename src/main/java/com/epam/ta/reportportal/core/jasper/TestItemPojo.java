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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.jasper;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;

/**
 * Jasper Reports collection {@link TestItem} POJO
 *
 * @author Andrei_Ramanchuk
 */
public class TestItemPojo {
	private String type;
	private String name;
	private String status;
	private Integer total;
	private Integer passed;
	private Integer failed;
	private Integer skipped;
	private Integer automationBug;
	private Integer productBug;
	private Integer systemIssue;
	private Integer noDefect;
	private Integer toInvestigate;

	public TestItemPojo(TestItem input) {
		this.type = input.getType().name();
		String issueDescription = "";
		if (input.getIssue() != null) {
			final TestItemIssue issue = input.getIssue();
			if (issue.getIssueDescription() != null) {
				issueDescription = "\r\n" + " DEFECT COMMENT: " + issue.getIssueDescription();
			}
		}
		String description = "";
		if (input.getItemDescription() != null) {
			description = "\r\n" + " ITEM DESCRIPTION: " + input.getItemDescription();
		}
		this.name = input.getName() + description + issueDescription;
		this.status = input.getStatus().name();

		ExecutionCounter exec = input.getStatistics().getExecutionCounter();
		this.total = exec.getTotal();
		this.passed = exec.getPassed();
		this.failed = exec.getFailed();
		this.skipped = exec.getSkipped();

		IssueCounter issue = input.getStatistics().getIssueCounter();
		this.automationBug = issue.getAutomationBugTotal();
		this.productBug = issue.getProductBugTotal();
		this.systemIssue = issue.getSystemIssueTotal();
		this.noDefect = issue.getNoDefectTotal();
		this.toInvestigate = issue.getToInvestigateTotal();
	}

	// @formatter:off
	public void setType(String value) {this.type = value;}
	public String getType() {return type;}
	public void setName(String value) {this.name = value;}
	public String getName() {return name;}
	public void setStatus(String value) {this.status = value;}
	public String getStatus() {return status;}
	public void setTotal(Integer value) {this.total = value;}
	public Integer getTotal() {return total;}
	public void setPased(Integer value) {this.passed = value;}
	public Integer getPassed() {return passed;}
	public void setFailed(Integer value) {this.failed = value;}
	public Integer getFailed() {return failed;}
	public void setSkipped(Integer value) {this.skipped = value;}
	public Integer getSkipped() {return skipped;}
	public void setAutomationBug(Integer value) {this.automationBug = value;}
	public Integer getAutomationBug() {return automationBug;}
	public void setProductBug(Integer value) {this.productBug = value;}
	public Integer getProductBug() {return productBug;}
	public void setSystemIssue(Integer value) {this.systemIssue = value;}
	public Integer getSystemIssue() {return systemIssue;}
	public void setNoDefect(Integer value) {this.noDefect = value;}
	public Integer getNoDefect() {return noDefect;}
	public void setToInvestigate(Integer value) {this.toInvestigate = value;}
	public Integer getToInvestigate() {return toInvestigate;}
	//@formatter:on

	@Override
	public String toString() {
		return "TestItemPojo [type=" + type + ", name=" + name + ", status=" + status + ", total=" + total + ", passed=" + passed
				+ ", failed=" + failed + ", skipped=" + skipped + ", automationBug=" + automationBug + ", productBug=" + productBug
				+ ", systemIssue=" + systemIssue + ", noDefect=" + noDefect + ", toInvestigate=" + toInvestigate + "]";
	}
}