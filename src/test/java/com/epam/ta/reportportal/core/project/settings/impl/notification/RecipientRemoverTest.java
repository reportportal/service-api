package com.epam.ta.reportportal.core.project.settings.impl.notification;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.project.settings.notification.RecipientRemover;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RecipientRemoverTest {

  @InjectMocks
  private RecipientRemover recipientRemover;

  @Mock
  private SenderCaseRepository senderCaseRepository;

  private static final Long PROJECT_ID = 1L;

  @Test
  public void removeRecipients() {
    Project project = mock(Project.class);
    when(project.getId()).thenReturn(PROJECT_ID);

    String firstEmail = "firstUser@gmail.com";
    String secondEmail = "secondUser@gmail.com";
    String firstLogin = "firstUser";
    String secondLogin = "secondUser";
    List<User> userList = new ArrayList<>();
    User firstUser = new User();
    firstUser.setEmail(firstEmail);
    firstUser.setLogin("firstUser");
    User secondUser = new User();
    secondUser.setEmail(secondEmail);
    secondUser.setLogin("secondUser");
    userList.add(firstUser);
    userList.add(secondUser);

    Set<String> emailsAndLogins = new HashSet<>();
    emailsAndLogins.add(firstEmail.toLowerCase());
    emailsAndLogins.add(secondEmail.toLowerCase());
    emailsAndLogins.add(firstLogin.toLowerCase());
    emailsAndLogins.add(secondLogin.toLowerCase());

    List<SenderCase> senderCases = new ArrayList<>();
    SenderCase firstSenderCase = mock(SenderCase.class);
    SenderCase secondSenderCase = mock(SenderCase.class);

    when(firstSenderCase.getId()).thenReturn(1L);
    when(secondSenderCase.getId()).thenReturn(2L);
    senderCases.add(firstSenderCase);
    senderCases.add(secondSenderCase);

    when(senderCaseRepository.findAllByProjectId(PROJECT_ID)).thenReturn(senderCases);

    recipientRemover.handle(userList, project);

    verify(senderCaseRepository).deleteRecipients(firstSenderCase.getId(), emailsAndLogins);
    verify(senderCaseRepository).deleteRecipients(secondSenderCase.getId(), emailsAndLogins);
  }
}
