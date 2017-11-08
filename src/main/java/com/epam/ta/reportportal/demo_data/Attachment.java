package com.epam.ta.reportportal.demo_data;

import org.springframework.core.io.ClassPathResource;

import static org.springframework.http.MediaType.*;

/**
 * @author Pavel_Bortnik
 */
public enum Attachment {

	CMD("demo/attachments/Test.cmd", TEXT_PLAIN_VALUE),
	CSS("demo/attachments/css.css", "text/css"),
	CSV("demo/attachments/Test.csv", "text/csv"),
	HTML("demo/attachments/html.html", TEXT_HTML_VALUE),
	JS("demo/attachments/javascript.js", "application/javascript"),
	PDF("demo/attachments/test.pdf", APPLICATION_PDF_VALUE),
	PHP("demo/attachments/php.php", "text/x-php"),
	TXT("demo/attachments/plain.txt", TEXT_PLAIN_VALUE),
	ZIP("demo/attachments/demo.zip", "application/zip"),
	JSON("demo/demo_widgets.json", APPLICATION_JSON_VALUE),
	PNG("demo/attachments/img.png", IMAGE_PNG_VALUE);

	Attachment(String resource, String contentType) {
		this.resource = resource;
		this.contentType = contentType;
	}

	private String resource;

	private String contentType;

	public ClassPathResource getResource() {
		return new ClassPathResource(resource);
	}

	public String getContentType() {
		return contentType;
	}
}
