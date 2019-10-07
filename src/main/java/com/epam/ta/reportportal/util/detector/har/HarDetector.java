package com.epam.ta.reportportal.util.detector.har;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class HarDetector implements Detector {

	public static final String HAR_MIME_TYPE = "har+json";

	@Override
	public MediaType detect(InputStream input, Metadata metadata) throws IOException {
		return MediaType.application(HAR_MIME_TYPE);
	}
}
