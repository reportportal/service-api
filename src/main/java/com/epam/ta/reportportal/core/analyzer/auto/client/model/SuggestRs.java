package com.epam.ta.reportportal.core.analyzer.auto.client.model;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SuggestRs {

	private Long testItemId;

	private Long testItemLogId;

	private String issueType;

	private Long relevantItem;

	private Long relevantLogId;

	private float matchScore;

	private int resultPosition;

	private float esScore;

	private int esPosition;

	private String modelFeatureNames;

	private String modelFeatureValues;

	private String modelInfo;

	private int usedLogLines;

	private int minShouldMatch;

	private int userChoice;

	private String methodName;

	public Long getTestItemId() {
		return testItemId;
	}

	public void setTestItemId(Long testItemId) {
		this.testItemId = testItemId;
	}

	public Long getTestItemLogId() {
		return testItemLogId;
	}

	public void setTestItemLogId(Long testItemLogId) {
		this.testItemLogId = testItemLogId;
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
