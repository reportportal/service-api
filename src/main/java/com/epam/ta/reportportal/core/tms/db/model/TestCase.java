package com.epam.ta.reportportal.core.tms.db.model;

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
public class TestCase implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(mappedBy = "testCase")
    private Set<TestCaseAttribute> tags;
    
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.PERSIST)
    private Set<TestCaseVersion> versions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_folder_id", nullable = false)
    private TestFolder testFolder;
    
    //    TODO:many-to-one
    //    private DataSet dataSet;
    //
    //    TODO:many-to-many
    //    private Set<TestPlan> testPlans;
    
    public void addTestCaseVersion(final TestCaseVersion testCaseVersion) {
        versions.add(testCaseVersion);
        testCaseVersion.setTestCase(this);
    }
}
