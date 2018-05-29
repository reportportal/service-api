package com.epam.ta.reportportal.store.commons;

public class BinaryDataMetaInfo {

	private final String fileId;

	private final String thumbnailFileId;

	/**
	 * Object to hold information about saved file.
	 *
	 * @param fileId
	 * @param thumbnailFileId
	 */
	public BinaryDataMetaInfo(String fileId, String thumbnailFileId) {
		this.fileId = fileId;
		this.thumbnailFileId = thumbnailFileId;
	}

	public String getFileId() {
		return fileId;
	}

	public String getThumbnailFileId() {
		return thumbnailFileId;
	}

	public static final class BinaryDataMetaInfoBuilder {
		private String fileId;
		private String thumbnailFileId;

		private BinaryDataMetaInfoBuilder() {
		}

		public static BinaryDataMetaInfoBuilder aBinaryDataMetaInfo() {
			return new BinaryDataMetaInfoBuilder();
		}

		public BinaryDataMetaInfoBuilder withFileId(String fileId) {
			this.fileId = fileId;
			return this;
		}

		public BinaryDataMetaInfoBuilder withThumbnailFileId(String thumbnailFileId) {
			this.thumbnailFileId = thumbnailFileId;
			return this;
		}

		public BinaryDataMetaInfo build() {
			return new BinaryDataMetaInfo(fileId, thumbnailFileId);
		}
	}
}
