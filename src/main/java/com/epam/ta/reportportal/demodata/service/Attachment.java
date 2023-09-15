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

package com.epam.ta.reportportal.demodata.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Pavel_Bortnik
 */
public enum Attachment {

  CMD("Test.cmd", "demo/attachments/Test.cmd", TEXT_PLAIN_VALUE),
  CSS("css.css", "demo/attachments/css.css", "text/css"),
  CSV("Test.csv", "demo/attachments/Test.csv", "text/csv"),
  HTML("html.html", "demo/attachments/html.html", TEXT_HTML_VALUE),
  JS("javascript.js", "demo/attachments/javascript.js", "application/javascript"),
  PDF("test.pdf", "demo/attachments/test.pdf", APPLICATION_PDF_VALUE),
  PHP("php.php", "demo/attachments/php.php", "text/x-php"),
  TXT("plain.txt", "demo/attachments/plain.txt", TEXT_PLAIN_VALUE),
  ZIP("demo.zip", "demo/attachments/demo.zip", "application/zip"),
  JSON("demo_widgets.json", "demo/demo_widgets.json", APPLICATION_JSON_VALUE),
  PNG("img.png", "demo/attachments/img.png", IMAGE_PNG_VALUE),
  XML("xml.xml", "demo/attachments/xml.xml", APPLICATION_XML_VALUE),
  HAR("har.har", "demo/attachments/har.har", "application/har+json"),
  GZ("gz.gz", "demo/attachments/gz.gz", "application/gzip"),
  RAR("rar.rar", "demo/attachments/rar.rar", "application/x-rar-compressed"),
  TAR("tar.tar", "demo/attachments/tar.tar", "application/tar");

  Attachment(String name, String resource, String contentType) {
    this.name = name;
    this.resource = resource;
    this.contentType = contentType;
  }

  private final String name;

  private final String resource;

  private final String contentType;

  public String getName() {
    return name;
  }

  public ClassPathResource getResource() {
    return new ClassPathResource(resource);
  }

  public String getContentType() {
    return contentType;
  }
}
