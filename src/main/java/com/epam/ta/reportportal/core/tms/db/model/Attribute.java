package com.epam.ta.reportportal.core.tms.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "tms_attribute", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attribute implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // TODO: Do we actually need to mark this field unique=true and nullable=false ?
    @Column(name = "key", nullable = false, unique = true)
    private String key;
    
    //TODO: override equals and hashCode methods
}
