package com.epam.ta.reportportal.store;

import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@ImportDataset("classpath:db/dataset.xml")
public class ServerSettingsRepositoryTest extends BaseDBTest {

	@Autowired
	private ServerSettingsRepository serverSettingRepository;

	@Test
	public void shouldDeleteUser() throws Exception {
		assertThat(serverSettingRepository).isNotNull();
		assertThat(serverSettingRepository.count()).isEqualTo(3);
		//		serverSettingRepository.delete(userRepository.findOne(2L));
	}
}
