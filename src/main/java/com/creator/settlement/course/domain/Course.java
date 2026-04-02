package com.creator.settlement.course.domain;

import com.creator.settlement.creator.domain.Creator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_course_creator_id", columnList = "creator_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @Column(name = "course_id", nullable = false, updatable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Builder
    private Course(String id, Creator creator, String title) {
        this.id = id;
        this.creator = creator;
        this.title = title;
    }
}
