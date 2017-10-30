package com.epam.ta.reportportal.util;

import com.google.common.annotations.VisibleForTesting;

import java.util.UUID;

public class RetryId {

	@VisibleForTesting
	static final String PREFIX = "retry:";

	private String rootID;
	private String itemHash;

	private RetryId(String rootID, String itemHash) {
		this.rootID = rootID;
		this.itemHash = itemHash;
	}

	public static boolean isRetry(String id) {
		return id.startsWith(PREFIX);
	}

	public static RetryId parse(String id) {
		String[] parts = id.split(":");
		if (3 != parts.length) {
			throw new IllegalArgumentException(id + " is not retry ID");
		}
		return new RetryId(parts[1], parts[2]);
	}

	public static RetryId newID(String rootID) {
		return new RetryId(rootID, UUID.randomUUID().toString());
	}

	public String getRootID() {
		return rootID;
	}

	public String getItemHash() {
		return itemHash;
	}

	@Override
	public String toString() {
		return PREFIX + rootID + ":" + itemHash;
	}
}
