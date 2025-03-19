package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsMilestoneRepository;
import com.epam.ta.reportportal.core.tms.mapper.TmsMilestoneMapper;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsMilestoneServiceImplTest {

    @Mock
    private TmsMilestoneMapper tmsMilestoneMapper;

    @Mock
    private TmsMilestoneRepository tmsMilestoneRepository;

    @InjectMocks
    private TmsMilestoneServiceImpl sut;

    @Test
    void shouldUpsertTestPlanToMilestonesWithEmptyMilestoneIds() {
        var tmsTestPlan = new TmsTestPlan();
        List<Long> milestoneIds = Collections.emptyList();

        assertDoesNotThrow(() -> sut.upsertTestPlanToMilestones(tmsTestPlan, milestoneIds));

        // Assert
        verifyNoInteractions(tmsMilestoneRepository, tmsMilestoneMapper);
    }

    @Test
    void shouldUpsertTestPlanToMilestones() {
        var testPlanId = 1L;
        var milestoneIds = List.of(10L, 20L);
        var tmsTestPlan = new TmsTestPlan();
        tmsTestPlan.setId(testPlanId);

        var firstMilestone = new TmsMilestone();
        firstMilestone.setId(10L);
        var secondMilestone = new TmsMilestone();
        secondMilestone.setId(20L);

        var milestones = Set.of(firstMilestone, secondMilestone);

        when(tmsMilestoneMapper.convertToTmsMilestones(milestoneIds)).thenReturn(milestones);

        assertDoesNotThrow(() -> sut.upsertTestPlanToMilestones(tmsTestPlan, milestoneIds));

        // Assert
        verify(tmsMilestoneRepository).detachTestPlanFromMilestones(testPlanId);

        var milestoneIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(tmsMilestoneRepository, times(2)).attachTestPlanToMilestone(
            eq(tmsTestPlan),
            milestoneIdCaptor.capture()
        );

        var capturedMilestoneIds = milestoneIdCaptor.getAllValues();
        assertThat(capturedMilestoneIds).containsExactlyInAnyOrderElementsOf(milestoneIds);

        // Ensure the milestones are updated correctly
        assertEquals(milestones.size(), tmsTestPlan.getMilestones().size());
        assertEquals(Set.of(firstMilestone, secondMilestone), tmsTestPlan.getMilestones());
    }

    @Test
    void shouldDetachTestPlanFromMilestones() {
        var testPlanId = 1L;

        assertDoesNotThrow(() -> sut.detachTestPlanFromMilestones(testPlanId));

        // Assert
        verify(tmsMilestoneRepository).detachTestPlanFromMilestones(testPlanId);
    }
}
