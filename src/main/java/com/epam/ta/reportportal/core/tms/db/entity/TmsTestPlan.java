package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "tms_test_plan", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    private TmsEnvironment environment;
    
    @ManyToOne
    @JoinColumn(name = "product_version_id", nullable = false)
    private TmsProductVersion productVersion;
    
    @OneToMany(mappedBy = "testPlan")
    private Set<TmsTestPlanAttribute> attributes;
    
    @OneToMany(mappedBy = "testPlan", fetch = FetchType.LAZY)
    private Set<TmsMilestone> milestones;
    
}
