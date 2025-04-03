package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tms_test_plan_attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlanAttribute implements Serializable {
    
    @EmbeddedId
    private TmsTestPlanAttributeId id;
    
    @ManyToOne
    @MapsId(value = "testPlanId")
    @JoinColumn(name = "test_plan_id")
    private TmsTestPlan testPlan;
    
    @ManyToOne
    @MapsId(value = "attributeId")
    @JoinColumn(name = "attribute_id")
    private TmsAttribute attribute;
    
    @Column(name = "value")
    private String value;
}
