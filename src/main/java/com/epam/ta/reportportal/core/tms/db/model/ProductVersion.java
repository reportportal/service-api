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

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "tms_product_version", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersion implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "documentation")
    private String documentation;
    
    @OneToMany(mappedBy = "productVersion")
    private Set<TestPlan> testPlans;
    
    @OneToMany(mappedBy = "productVersion")
    private Set<Milestone> milestones;
    //TODO test changes
}
