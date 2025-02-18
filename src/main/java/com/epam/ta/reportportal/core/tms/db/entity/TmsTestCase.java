package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.CascadeType;
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

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "tms_test_case", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCase implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(mappedBy = "testCase")
    private Set<TmsTestCaseAttribute> tags;
    
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.PERSIST)
    private Set<TmsTestCaseVersion> versions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_folder_id", nullable = false)
    private TmsTestFolder testFolder;
    
    //    TODO:many-to-one
    //    private DataSet dataSet;
    //
    //    TODO:many-to-many
    //    private Set<TestPlan> testPlans;
    
    public void addTestCaseVersion(final TmsTestCaseVersion testCaseVersion) {
        versions.add(testCaseVersion);
        testCaseVersion.setTestCase(this);
    }
}
