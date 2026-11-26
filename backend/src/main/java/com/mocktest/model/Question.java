package com.mocktest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false, length = 300) private String optionA;
    @Column(nullable = false, length = 300) private String optionB;
    @Column(nullable = false, length = 300) private String optionC;
    @Column(nullable = false, length = 300) private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Option correctOption;   // never sent to students while the exam is running
}
