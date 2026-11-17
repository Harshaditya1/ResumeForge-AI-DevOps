package com.resumeforge.interviewprep;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterviewQuestionSeeder implements CommandLineRunner {

    private final InterviewQuestionBankRepository repository;

    public InterviewQuestionSeeder(InterviewQuestionBankRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        List<InterviewQuestionBankItem> questions = new ArrayList<>();
        add(questions, InterviewQuestionCategory.JAVA, InterviewQuestionDifficulty.EASY,
                "Java is statically typed, object-oriented, and runs on the JVM.",
                "classes and objects", "JVM memory areas", "JDK versus JRE", "primitive versus reference types",
                "String immutability", "equals and hashCode", "method overloading", "method overriding",
                "access modifiers", "checked versus unchecked exceptions");
        add(questions, InterviewQuestionCategory.DSA, InterviewQuestionDifficulty.MEDIUM,
                "Choose the data structure or algorithm that meets the time and space constraints, then explain its invariant.",
                "array versus linked list", "hash table collisions", "binary search", "merge sort",
                "sliding window", "two pointers", "BFS versus DFS", "heap operations", "dynamic programming",
                "Big O analysis");
        add(questions, InterviewQuestionCategory.DBMS, InterviewQuestionDifficulty.MEDIUM,
                "A relational database stores structured data in tables and uses transactions and constraints for consistency.",
                "primary and foreign keys", "normalization", "ACID transactions", "isolation levels",
                "database indexes", "clustered versus non-clustered indexes", "inner versus outer joins",
                "GROUP BY and HAVING", "optimistic locking", "the N+1 query problem");
        add(questions, InterviewQuestionCategory.OPERATING_SYSTEMS, InterviewQuestionDifficulty.MEDIUM,
                "An operating system manages processes, memory, files, devices, and scheduling.",
                "process versus thread", "context switching", "virtual memory", "paging",
                "deadlock conditions", "deadlock prevention", "CPU scheduling", "mutex versus semaphore",
                "user mode versus kernel mode", "file descriptors");
        add(questions, InterviewQuestionCategory.COMPUTER_NETWORKS, InterviewQuestionDifficulty.MEDIUM,
                "Network protocols define how data is addressed, transmitted, routed, and secured.",
                "TCP versus UDP", "the TCP three-way handshake", "DNS resolution", "HTTP methods",
                "HTTP versus HTTPS", "TLS certificates", "REST idempotency", "load balancing",
                "subnets", "WebSockets");
        add(questions, InterviewQuestionCategory.OOP, InterviewQuestionDifficulty.EASY,
                "Good object-oriented design keeps responsibilities focused and dependencies replaceable.",
                "encapsulation", "abstraction", "inheritance", "polymorphism",
                "composition over inheritance", "the single responsibility principle", "the open-closed principle",
                "dependency inversion", "interfaces versus abstract classes", "immutable objects");
        add(questions, InterviewQuestionCategory.SPRING_BOOT, InterviewQuestionDifficulty.MEDIUM,
                "Spring Boot provides convention-based configuration, dependency injection, and production-ready web tooling.",
                "dependency injection", "component scanning", "Spring beans", "bean scopes",
                "Spring profiles", "REST controllers", "validation", "transaction boundaries",
                "Spring Security filters", "Spring Boot Actuator");
        add(questions, InterviewQuestionCategory.HR, InterviewQuestionDifficulty.EASY,
                "Keep answers concise, specific, and connected to the role; use measurable outcomes where possible.",
                "tell me about yourself", "why do you want this role", "your greatest strength",
                "a skill you are improving", "why should we hire you", "your preferred work environment",
                "handling feedback", "salary expectations", "availability", "questions for the interviewer");
        add(questions, InterviewQuestionCategory.BEHAVIORAL, InterviewQuestionDifficulty.MEDIUM,
                "Use the STAR structure: describe the situation, task, action, and measurable result.",
                "a difficult deadline", "a conflict with a teammate", "a production incident",
                "a project that failed", "an ambiguous requirement", "leading without authority",
                "a time you made a mistake", "prioritizing competing tasks", "receiving critical feedback",
                "an achievement you are proud of");

        repository.saveAll(questions);
    }

    private void add(
            List<InterviewQuestionBankItem> questions,
            InterviewQuestionCategory category,
            InterviewQuestionDifficulty difficulty,
            String answer,
            String... topics
    ) {
        int remaining = Math.max(
                0,
                10 - (int) repository.countByCategory(category)
        );
        for (int index = 0; index < remaining && index < topics.length; index++) {
            String topic = topics[index];
            questions.add(InterviewQuestionBankItem.builder()
                    .category(category)
                    .difficulty(difficulty)
                    .question("Explain " + topic + " and when it is useful.")
                    .answer(answer)
                    .build());
        }
    }
}
