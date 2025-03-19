package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tms_product_version", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsProductVersion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "version")
    private String version;

    @Column(name = "documentation")
    private String documentation;

    //TODO to become foreign key
    @Column(name = "project_id")
    private Long projectId;

    @OneToMany(mappedBy = "productVersion")
    @ToString.Exclude
    private Set<TmsTestPlan> testPlans;

    @OneToMany(mappedBy = "productVersion")
    @ToString.Exclude
    private Set<TmsMilestone> milestones;
    //TODO test changes
}
