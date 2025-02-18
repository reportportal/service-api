package com.epam.ta.reportportal.core.tms.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "tms_manual_scenario", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualScenario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "execution_estimation_time")
    private Integer executionEstimationTime;
    
    @Column(name = "link_to_requirements")
    private String linkToRequirements;
    
    @Column(name = "preconditions")
    private String preconditions;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_version_id")
    private TestCaseVersion testCaseVersion;
    
    @OneToMany(mappedBy = "manualScenario")
    private Set<ManualScenarioAttribute> attributes;
    
    @OneToMany(mappedBy = "manualScenario")
    private Set<Step> steps;
}
