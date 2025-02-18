package com.epam.ta.reportportal.core.tms.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "tms_environment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Environment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "test_data")
    private String testData;
    
    @OneToMany(mappedBy = "environment")
    private Set<Attachment> attachments;
    
    @OneToMany(mappedBy = "environment")
    private Set<TestPlan> testPlans;
    
    //TODO: add link to dataset
}
