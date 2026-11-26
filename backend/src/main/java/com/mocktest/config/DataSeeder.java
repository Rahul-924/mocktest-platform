package com.mocktest.config;

import com.mocktest.model.Exam;
import com.mocktest.model.Option;
import com.mocktest.model.Question;
import com.mocktest.model.Role;
import com.mocktest.model.User;
import com.mocktest.repo.ExamRepository;
import com.mocktest.repo.QuestionRepository;
import com.mocktest.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** On first start: creates the admin from environment variables and a small sample exam. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final ExamRepository exams;
    private final QuestionRepository questions;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}") private String adminEmail;
    @Value("${app.admin.password:}") private String adminPassword;
    @Value("${app.seed-sample:true}") private boolean seedSample;

    @Override
    public void run(String... args) {
        if (!adminEmail.isBlank() && adminPassword.length() >= 8 && !users.existsByEmail(adminEmail.toLowerCase())) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail.trim().toLowerCase());
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
        if (seedSample && exams.count() == 0) {
            Exam exam = new Exam();
            exam.setTitle("Java & OOP Basics");
            exam.setDescription("Five quick questions to try the platform.");
            exam.setDurationMinutes(5);
            exam = exams.save(exam);
            add(exam, "Which keyword prevents a class from being inherited?", "static", "final", "private", "volatile", Option.B);
            add(exam, "What is the default value of an int instance variable?", "0", "null", "undefined", "1", Option.A);
            add(exam, "Which collection does not allow duplicate elements?", "ArrayList", "LinkedList", "HashSet", "Vector", Option.C);
            add(exam, "Which of these is NOT a pillar of OOP?", "Encapsulation", "Polymorphism", "Compilation", "Inheritance", Option.C);
            add(exam, "What does JVM stand for?", "Java Variable Machine", "Java Virtual Machine", "Joint Virtual Method", "Java Verified Module", Option.B);
        }
    }

    private void add(Exam exam, String text, String a, String b, String c, String d, Option correct) {
        Question q = new Question();
        q.setExam(exam);
        q.setText(text);
        q.setOptionA(a); q.setOptionB(b); q.setOptionC(c); q.setOptionD(d);
        q.setCorrectOption(correct);
        questions.save(q);
    }
}
