package com.epam.ta.reportportal.core.project.settings.notification;

import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

@Service
public class ProjectRecipientHandlerImpl implements ProjectRecipientHandler {

	private final SenderCaseRepository senderCaseRepository;

	@Autowired
	public ProjectRecipientHandlerImpl(SenderCaseRepository senderCaseRepository) {
		this.senderCaseRepository = senderCaseRepository;
	}

	@Override
	public void excludeProjectRecipients(Iterable<User> users, Project project) {
		final Set<String> toExclude = stream(users.spliterator(), false).map(user -> asList(user.getEmail().toLowerCase(),
				user.getLogin().toLowerCase()
		)).flatMap(List::stream).collect(toSet());
		/* Current recipients of specified project */
		senderCaseRepository.findAllByProjectId(project.getId()).forEach(senderCase -> {
			// saved - list of saved user emails before changes
			senderCaseRepository.deleteRecipients(senderCase.getId(), toExclude);
		});
	}
}
