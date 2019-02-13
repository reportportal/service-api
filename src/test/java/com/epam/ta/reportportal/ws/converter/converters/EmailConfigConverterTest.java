package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class EmailConfigConverterTest {

	@Test(expected = NullPointerException.class)
	public void toCaseModelNullTest() {
		EmailConfigConverter.TO_CASE_MODEL.apply(null);
	}

	@Test
	public void toResource() {
		final Set<SenderCase> senderCases = getSenderCases();
		final ProjectNotificationConfigDTO dto = EmailConfigConverter.TO_RESOURCE.apply(senderCases);

		assertEquals(dto.getSenderCases().size(), senderCases.size());
	}

	@Test
	public void toCaseResource() {
		final SenderCase senderCase = getCase();
		final SenderCaseDTO senderCaseDTO = EmailConfigConverter.TO_CASE_RESOURCE.apply(senderCase);

		assertThat(senderCaseDTO.getRecipients()).containsExactlyInAnyOrderElementsOf(senderCase.getRecipients());
		assertThat(senderCaseDTO.getAttributes()).containsExactlyInAnyOrderElementsOf(senderCase.getLaunchAttributes());
		assertThat(senderCaseDTO.getLaunchNames()).containsExactlyInAnyOrderElementsOf(senderCase.getLaunchNames());
		assertEquals(senderCaseDTO.getSendCase(), senderCase.getSendCase().getCaseString());
	}

	@Test
	public void toCaseModel() {
		final SenderCaseDTO caseDTO = getCaseDTO();
		final SenderCase senderCase = EmailConfigConverter.TO_CASE_MODEL.apply(caseDTO);

		assertThat(senderCase.getRecipients()).containsExactlyInAnyOrderElementsOf(caseDTO.getRecipients());
		assertThat(senderCase.getLaunchNames()).containsExactlyInAnyOrderElementsOf(caseDTO.getLaunchNames());
		assertThat(senderCase.getLaunchAttributes()).containsExactlyInAnyOrderElementsOf(caseDTO.getAttributes());
		assertEquals(senderCase.getSendCase().getCaseString(), caseDTO.getSendCase());
	}

	private static Set<SenderCase> getSenderCases() {
		Set<SenderCase> senderCases = new HashSet<>();
		senderCases.add(getCase());
		senderCases.add(new SenderCase(
				Sets.newHashSet("recipent3", "recipient8"),
				Sets.newHashSet("launch1", "launch5", "launch10"),
				Sets.newHashSet("attribue11", "attribute24"),
				SendCase.ALWAYS
		));
		return senderCases;
	}

	private static SenderCase getCase() {
		return new SenderCase(
				Sets.newHashSet("recipent1", "recipient2"),
				Sets.newHashSet("launch1", "launch2", "launch3"),
				Sets.newHashSet("attribue1", "attribute2"),
				SendCase.MORE_10
		);
	}

	private static SenderCaseDTO getCaseDTO() {
		SenderCaseDTO senderCaseDTO = new SenderCaseDTO();
		senderCaseDTO.setRecipients(Arrays.asList("recipient1", "recipient2"));
		senderCaseDTO.setLaunchNames(Arrays.asList("launch1", "launch2"));
		senderCaseDTO.setAttributes(Arrays.asList("attr1", "attr2"));
		senderCaseDTO.setSendCase("always");
		return senderCaseDTO;
	}
}