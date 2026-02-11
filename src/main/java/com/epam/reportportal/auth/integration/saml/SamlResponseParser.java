/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.auth.integration.saml;


import com.epam.reportportal.auth.model.saml.NameId;
import com.epam.reportportal.auth.model.saml.SamlResponse;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Parse saml xml response
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class SamlResponseParser {

  public static SamlResponse parseSamlResponse(String samlResponse) throws Exception {
    org.opensaml.core.config.InitializationService.initialize();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new ByteArrayInputStream(samlResponse.getBytes()));

    Element element = document.getDocumentElement();
    UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
    XMLObject xmlObject = unmarshaller.unmarshall(element);

    if (xmlObject instanceof Response) {
      Response response = (Response) xmlObject;
      return extractSamlResponse(response);
    }

    throw new RuntimeException("Invalid SAML response");
  }

  private static SamlResponse extractSamlResponse(Response response) {
    SamlResponse samlResponse = new SamlResponse();

    samlResponse.setIssuer(response.getIssuer().getValue());

    for (Assertion assertion : response.getAssertions()) {
      Subject subject = assertion.getSubject();
      if (subject != null && subject.getNameID() != null) {
        NameId nameID = new NameId(subject.getNameID().getFormat(), subject.getNameID().getValue());
        samlResponse.setNameId(nameID);
        break;
      }
    }

    Map<String, String> attributes = getAttributes(response);
    samlResponse.setAttributes(attributes);

    return samlResponse;
  }

  private static Map<String, String> getAttributes(Response response) {
    Map<String, String> attributes = new HashMap<>();
    for (Assertion assertion : response.getAssertions()) {
      for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
        for (org.opensaml.saml.saml2.core.Attribute attribute : attributeStatement.getAttributes()) {
          String name = attribute.getName();
          String value = attribute.getAttributeValues().isEmpty() ? null
              : attribute.getAttributeValues().get(0).getDOM().getTextContent();
          attributes.put(name, value);
        }
      }
    }
    return attributes;
  }
}
