package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class NotificationConfigConverterTest {

	@Test
	void toCaseModelNullTest() {
		assertThrows(NullPointerException.class, () -> NotificationConfigConverter.TO_CASE_MODEL.apply(null));
	}

	@Test
	void toResource() {
		final Set<SenderCase> senderCases = getSenderCases();
		List<SenderCaseDTO> resources = NotificationConfigConverter.TO_RESOURCE.apply(senderCases);

		assertEquals(resources.size(), senderCases.size());
	}

	@Test
	void toCaseResource() {
		final SenderCase senderCase = getCase();
		final SenderCaseDTO senderCaseDTO = NotificationConfigConverter.TO_CASE_RESOURCE.apply(senderCase);

		assertThat(senderCaseDTO.getRecipients()).containsExactlyInAnyOrderElementsOf(senderCase.getRecipients());
		//		assertThat(senderCaseDTO.getAttributes()).containsExactlyInAnyOrderElementsOf(senderCase.getLaunchAttributeRules());
		assertThat(senderCaseDTO.getLaunchNames()).containsExactlyInAnyOrderElementsOf(senderCase.getLaunchNames());
		assertEquals(senderCaseDTO.getSendCase(), senderCase.getSendCase().getCaseString());
	}

	@Test
	void toCaseModel() {
		final SenderCaseDTO caseDTO = getCaseDTO();
		final SenderCase senderCase = NotificationConfigConverter.TO_CASE_MODEL.apply(caseDTO);

		assertThat(senderCase.getRecipients()).containsExactlyInAnyOrderElementsOf(caseDTO.getRecipients());
		assertThat(senderCase.getLaunchNames()).containsExactlyInAnyOrderElementsOf(caseDTO.getLaunchNames());
		//		assertThat(senderCase.getLaunchAttributes()).containsExactlyInAnyOrderElementsOf(caseDTO.getAttributes());
		assertEquals(senderCase.getSendCase().getCaseString(), caseDTO.getSendCase());
	}

	private static Set<SenderCase> getSenderCases() {
		Set<SenderCase> senderCases = new HashSet<>();
		senderCases.add(getCase());
		final LaunchAttributeRule launchAttributeRule = new LaunchAttributeRule();
		launchAttributeRule.setId(1L);
		launchAttributeRule.setKey("key");
		launchAttributeRule.setValue("value");
		senderCases.add(new SenderCase(
				Sets.newHashSet("recipent3", "recipient8"),
				Sets.newHashSet("launch1", "launch5", "launch10"),
				Sets.newHashSet(launchAttributeRule),
				SendCase.ALWAYS
		));
		return senderCases;
	}

	private static SenderCase getCase() {
		final LaunchAttributeRule launchAttributeRule = new LaunchAttributeRule();
		launchAttributeRule.setId(2L);
		launchAttributeRule.setKey("key1");
		launchAttributeRule.setValue("value1");
		return new SenderCase(
				Sets.newHashSet("recipent1", "recipient2"),
				Sets.newHashSet("launch1", "launch2", "launch3"),
				Sets.newHashSet(launchAttributeRule),
				SendCase.MORE_10
		);
	}

	private static SenderCaseDTO getCaseDTO() {
		SenderCaseDTO senderCaseDTO = new SenderCaseDTO();
		senderCaseDTO.setRecipients(Arrays.asList("recipient1", "recipient2"));
		senderCaseDTO.setLaunchNames(Arrays.asList("launch1", "launch2"));
		final ItemAttributeResource launchAttribute = new ItemAttributeResource();
		launchAttribute.setKey("key");
		launchAttribute.setValue("val");
		senderCaseDTO.setAttributes(Sets.newHashSet(launchAttribute));
		senderCaseDTO.setSendCase("always");
		return senderCaseDTO;
	}

}