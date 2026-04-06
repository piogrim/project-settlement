package com.creator.settlement.creator.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "creators")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Creator {

    @Id
    @Column(name = "creator_id", nullable = false, updatable = false, length = 50)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Builder
    private Creator(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
