package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "tms_test_case_attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseAttribute implements Serializable {
    
    @EmbeddedId
    private TmsTestCaseAttributeId id;
    
    @ManyToOne
    @MapsId(value = "testCaseId")
    @JoinColumn(name = "test_case_id")
    private TmsTestCase testCase;
    
    @ManyToOne
    @MapsId(value = "attributeId")
    @JoinColumn(name = "attribute_id")
    private TmsAttribute attribute;
    
    // TODO: Do we need this field for test case?
    @Column(name = "value")
    private String value;
    
    //TODO: override equals and hashCode methods
}
