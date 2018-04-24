package com.epam.ta.reportportal.store;

import com.epam.ta.reportportal.store.database.dao.ServerSettingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@ImportDataset("classpath:db/dataset.xml")
public class ServerSettingsRepositoryTest extends BaseDBTest {

	@Autowired
	private ServerSettingRepository serverSettingRepository;

	@Test
	public void shouldDeleteUser() throws Exception {
		assertThat(serverSettingRepository).isNotNull();
		assertThat(serverSettingRepository.count()).isEqualTo(3);
		//		serverSettingRepository.delete(userRepository.findOne(2L));
	}
}
