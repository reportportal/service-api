package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SuggestInfo implements Serializable {

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SuggestInfo that = (SuggestInfo) o;
		return isMergedLog == that.isMergedLog && Float.compare(that.matchScore, matchScore) == 0 && resultPosition == that.resultPosition
				&& Float.compare(that.esScore, esScore) == 0 && esPosition == that.esPosition && usedLogLines == that.usedLogLines
				&& minShouldMatch == that.minShouldMatch && Float.compare(that.processedTime, processedTime) == 0
				&& userChoice == that.userChoice && Objects.equals(project, that.project) && Objects.equals(testItem, that.testItem)
				&& Objects.equals(testItemLogId, that.testItemLogId) && Objects.equals(launchId, that.launchId)
				&& Objects.equals(launchName, that.launchName) && Objects.equals(issueType, that.issueType) && Objects.equals(relevantItem,
				that.relevantItem
		) && Objects.equals(relevantLogId, that.relevantLogId) && Objects.equals(modelFeatureNames, that.modelFeatureNames)
				&& Objects.equals(
				modelFeatureValues,
				that.modelFeatureValues
		) && Objects.equals(modelInfo, that.modelInfo) && Objects.equals(methodName, that.methodName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project,
				testItem,
				testItemLogId,
				launchId,
				launchName,
				issueType,
				relevantItem,
				relevantLogId,
				isMergedLog,
				matchScore,
				resultPosition,
				esScore,
				esPosition,
				modelFeatureNames,
				modelFeatureValues,
				modelInfo,
				usedLogLines,
				minShouldMatch,
				processedTime,
				userChoice,
				methodName
		);
	}

	@Override
	public String toString() {
		return "SuggestInfo{" + "project=" + project + ", testItem=" + testItem + ", testItemLogId=" + testItemLogId + ", launchId="
				+ launchId + ", launchName='" + launchName + '\'' + ", issueType='" + issueType + '\'' + ", relevantItem=" + relevantItem
				+ ", relevantLogId=" + relevantLogId + ", isMergedLog=" + isMergedLog + ", matchScore=" + matchScore + ", resultPosition="
				+ resultPosition + ", esScore=" + esScore + ", esPosition=" + esPosition + ", modelFeatureNames='" + modelFeatureNames
				+ '\'' + ", modelFeatureValues='" + modelFeatureValues + '\'' + ", modelInfo='" + modelInfo + '\'' + ", usedLogLines="
				+ usedLogLines + ", minShouldMatch=" + minShouldMatch + ", processedTime=" + processedTime + ", userChoice=" + userChoice
				+ ", methodName='" + methodName + '\'' + '}';
	}
}
