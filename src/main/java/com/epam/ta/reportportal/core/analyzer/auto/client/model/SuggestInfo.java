/*
 * Copyright 2021 EPAM Systems
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
package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import java.util.Objects;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SuggestInfo {

	private Long project;

	private Long testItem;

	private Long testItemLogId;

	private Long launchId;

	private String launchName;

	private String issueType;

	private Long relevantItem;

	private Long relevantLogId;

	private boolean isMergedLog;

	private float matchScore;

	private int resultPosition;

	private float esScore;

	private int esPosition;

	private String modelFeatureNames;

	private String modelFeatureValues;

	private String modelInfo;

	private int usedLogLines;

	private int minShouldMatch;

	private float processedTime;

	private int userChoice;

	private String methodName;

	public Long getProject() {
		return project;
	}

	public void setProject(Long project) {
		this.project = project;
	}

	public Long getTestItem() {
		return testItem;
	}

	public void setTestItem(Long testItem) {
		this.testItem = testItem;
	}

	public Long getTestItemLogId() {
		return testItemLogId;
	}

	public void setTestItemLogId(Long testItemLogId) {
		this.testItemLogId = testItemLogId;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public String getLaunchName() {
		return launchName;
	}

	public void setLaunchName(String launchName) {
		this.launchName = launchName;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	public Long getRelevantItem() {
		return relevantItem;
	}

	public void setRelevantItem(Long relevantItem) {
		this.relevantItem = relevantItem;
	}

	public Long getRelevantLogId() {
		return relevantLogId;
	}

	public void setRelevantLogId(Long relevantLogId) {
		this.relevantLogId = relevantLogId;
	}

	public boolean getIsMergedLog() {
		return isMergedLog;
	}

	public void setIsMergedLog(boolean isMergedLog) {
		this.isMergedLog = isMergedLog;
	}

	public float getMatchScore() {
		return matchScore;
	}

	public void setMatchScore(float matchScore) {
		this.matchScore = matchScore;
	}

	public int getResultPosition() {
		return resultPosition;
	}

	public void setResultPosition(int resultPosition) {
		this.resultPosition = resultPosition;
	}

	public float getEsScore() {
		return esScore;
	}

	public void setEsScore(float esScore) {
		this.esScore = esScore;
	}

	public int getEsPosition() {
		return esPosition;
	}

	public void setEsPosition(int esPosition) {
		this.esPosition = esPosition;
	}

	public String getModelFeatureNames() {
		return modelFeatureNames;
	}

	public void setModelFeatureNames(String modelFeatureNames) {
		this.modelFeatureNames = modelFeatureNames;
	}

	public String getModelFeatureValues() {
		return modelFeatureValues;
	}

	public void setModelFeatureValues(String modelFeatureValues) {
		this.modelFeatureValues = modelFeatureValues;
	}

	public String getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(String modelInfo) {
		this.modelInfo = modelInfo;
	}

	public int getUsedLogLines() {
		return usedLogLines;
	}

	public void setUsedLogLines(int usedLogLines) {
		this.usedLogLines = usedLogLines;
	}

	public int getMinShouldMatch() {
		return minShouldMatch;
	}

	public void setMinShouldMatch(int minShouldMatch) {
		this.minShouldMatch = minShouldMatch;
	}

	public float getProcessedTime() {
		return processedTime;
	}

	public void setProcessedTime(float processedTime) {
		this.processedTime = processedTime;
	}

	public int getUserChoice() {
		return userChoice;
	}

	public void setUserChoice(int userChoice) {
		this.userChoice = userChoice;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
}
