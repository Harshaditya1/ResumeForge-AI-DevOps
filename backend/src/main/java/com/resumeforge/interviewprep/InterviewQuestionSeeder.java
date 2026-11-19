package com.resumeforge.interviewprep;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterviewQuestionSeeder implements CommandLineRunner {

    private final InterviewQuestionBankRepository repository;

    public InterviewQuestionSeeder(InterviewQuestionBankRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        long count = repository.count();
        boolean needsRefresh = count == 0 || repository.findAll().stream().anyMatch(q -> q.getSourceUrl() == null);
        if (!needsRefresh) {
            return;
        }

        // If there are existing questions without URLs, update or clean them up safely
        repository.deleteAll();

        List<InterviewQuestionBankItem> questions = new ArrayList<>();

        // 1. JAVA
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.JAVA)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Why is String immutable in Java?")
                .topic("Core Java & Memory")
                .question("Explain why the String class is made immutable in Java and what benefits it provides regarding security, caching, and multithreading.")
                .answer("1. String Pool (Interning): Enables JVM to share identical string literals, saving substantial heap memory.\n" +
                        "2. Thread Safety: Immutable objects are inherently thread-safe without synchronization overhead.\n" +
                        "3. Security: Sensitive parameters (database URLs, passwords, network sockets) cannot be altered after validation.\n" +
                        "4. HashCode Caching: Hash code is computed once and cached, providing fast lookups in HashMaps.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/string-immutability-in-java/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.JAVA)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("HashMap vs ConcurrentHashMap in Java")
                .topic("Collections & Concurrency")
                .question("Compare HashMap, SynchronizedMap, and ConcurrentHashMap. How does ConcurrentHashMap achieve high throughput in multi-threaded environments?")
                .answer("HashMap is non-synchronized and not thread-safe. SynchronizedMap locks the entire map on every operation, creating contention bottlenecks. ConcurrentHashMap (Java 8+) uses CAS (Compare-And-Swap) operations and synchronized blocks on bucket node heads rather than locking the entire map, allowing concurrent reads and segmented writes.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/difference-between-hashmap-and-concurrenthashmap-in-java/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.JAVA)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("JVM Memory Architecture: Heap, Stack, and Metaspace")
                .topic("JVM Internals")
                .question("Describe the different runtime memory areas of the Java Virtual Machine (Stack, Heap, Metaspace, and Program Counter).")
                .answer("Heap: Shared area holding all instantiated objects and instance variables; managed by Garbage Collection (Young Gen, Old Gen).\n" +
                        "Stack: Thread-private memory holding method call frames, local primitives, and object references; allocated and freed in LIFO order.\n" +
                        "Metaspace: Native memory holding class metadata, bytecode definitions, and static variables (replaced PermGen in Java 8).\n" +
                        "PC Register: Holds the address of the current JVM instruction being executed.")
                .sourceName("Baeldung")
                .sourceUrl("https://www.baeldung.com/java-stack-heap")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.JAVA)
                .difficulty(InterviewQuestionDifficulty.HARD)
                .title("Java Garbage Collection Algorithms & Generational Hypothesis")
                .topic("Garbage Collection")
                .question("Explain the Weak Generational Hypothesis and compare the G1 GC and ZGC collectors in modern Java.")
                .answer("The Weak Generational Hypothesis observes that most objects die shortly after creation (Eden space). G1 GC divides heap into equal regions, prioritizing regions with highest garbage ratio ('Garbage-First') with bounded pause targets. ZGC (Zero-pause GC) performs all expensive GC phases (marking, relocation) concurrently using colored pointers and load barriers with sub-millisecond pauses.")
                .sourceName("Oracle Java Docs")
                .sourceUrl("https://docs.oracle.com/en/java/javase/21/gctuning/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.JAVA)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Checked vs Unchecked Exceptions in Java")
                .topic("Exception Handling")
                .question("What is the technical and architectural difference between Checked and Unchecked (Runtime) exceptions in Java?")
                .answer("Checked Exceptions inherit from Exception (excluding RuntimeException) and are enforced at compile time; callers must either catch them or declare them in throws (e.g., IOException, SQLException). Unchecked Exceptions inherit from RuntimeException or Error and indicate programming errors (NullPointerException, IllegalArgumentException) that usually cannot be recovered from.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/checked-vs-unchecked-exceptions-in-java/")
                .build());

        // 2. DSA
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DSA)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Two Sum Problem")
                .topic("Arrays & Hashing")
                .question("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. Can you do it in O(n) time?")
                .answer("Use a Hash Map storing value -> index. Iterate through the array once: for each element x, check if (target - x) exists in the map. If yes, return current index and stored index. If no, insert x with its index. Time Complexity: O(n), Space Complexity: O(n).")
                .sourceName("LeetCode")
                .sourceUrl("https://leetcode.com/problems/two-sum/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DSA)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Longest Substring Without Repeating Characters")
                .topic("Sliding Window")
                .question("Find the length of the longest substring without repeating characters in a given string s.")
                .answer("Use the Sliding Window technique with two pointers (left, right) and a character frequency/index map. Expand the right pointer; if a duplicate character is seen within the current window, advance the left pointer past the previous index of that character. Track max window size (right - left + 1). Time Complexity: O(n), Space: O(min(m, n)).")
                .sourceName("LeetCode")
                .sourceUrl("https://leetcode.com/problems/longest-substring-without-repeating-characters/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DSA)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("LRU Cache Design and Implementation")
                .topic("Data Structure Design")
                .question("Design a data structure that follows the constraints of a Least Recently Used (LRU) cache supporting get(key) and put(key, value) in O(1) time complexity.")
                .answer("Combine a Doubly Linked List with a Hash Map. The Hash Map stores key -> Node reference for O(1) lookup. The Doubly Linked List maintains access order with dummy Head and Tail nodes. On get() or put(), move accessed node to Head. When capacity is exceeded, evict the node immediately before Tail in O(1).")
                .sourceName("LeetCode")
                .sourceUrl("https://leetcode.com/problems/lru-cache/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DSA)
                .difficulty(InterviewQuestionDifficulty.HARD)
                .title("Trapping Rain Water")
                .topic("Two Pointers & Dynamic Programming")
                .question("Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.")
                .answer("Optimal O(n) time and O(1) space using Two Pointers: Initialize left = 0, right = n - 1, leftMax = 0, rightMax = 0. While left < right: if height[left] < height[right], update leftMax and add max(0, leftMax - height[left]) to water, then left++. Otherwise, update rightMax and add max(0, rightMax - height[right]) to water, then right--.")
                .sourceName("LeetCode")
                .sourceUrl("https://leetcode.com/problems/trapping-rain-water/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DSA)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Detect Cycle in a Directed Graph")
                .topic("Graphs & DFS")
                .question("Explain how to detect a cycle in a directed graph using Depth First Search (DFS) with recursion stack coloring.")
                .answer("Maintain three states for each node: 0 (Unvisited), 1 (Visiting / In current recursion stack), 2 (Completely visited). When running DFS from a node, mark it 1. For each neighbor: if neighbor is in state 1, a back-edge exists, indicating a directed cycle. If neighbor is 0, recurse. When done processing all neighbors, mark node as 2. Time: O(V + E).")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/detect-cycle-in-a-graph/")
                .build());

        // 3. DBMS
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DBMS)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Explain ACID Properties in Relational Databases")
                .topic("Transactions & Concurrency")
                .question("Explain the four ACID properties that ensure database transactions are processed reliably, using a banking fund transfer scenario.")
                .answer("Atomicity: All operations in a transaction succeed, or all are rolled back (all-or-nothing).\n" +
                        "Consistency: Database transitions from one valid state satisfying all schema constraints to another.\n" +
                        "Isolation: Concurrent transactions execute without interfering with one another.\n" +
                        "Durability: Once committed, state changes survive server crashes or power failures (via write-ahead log / WAL).")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/acid-properties-in-dbms/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DBMS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Clustered vs Non-Clustered Indexes in SQL")
                .topic("Database Indexing")
                .question("What is the difference between a clustered and a non-clustered index, and how do B+ Tree leaf nodes differ between them?")
                .answer("Clustered Index determines the physical order of data rows on disk. A table can have only one clustered index (usually the Primary Key). The leaf nodes of the B+ Tree contain the actual data rows.\n" +
                        "Non-Clustered Index creates a separate structure sorted by index columns where leaf nodes store pointers (row identifiers or primary keys) back to the clustered index rows. A table can have multiple non-clustered indexes.")
                .sourceName("InterviewBit")
                .sourceUrl("https://www.interviewbit.com/dbms-interview-questions/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DBMS)
                .difficulty(InterviewQuestionDifficulty.HARD)
                .title("Transaction Isolation Levels and Concurrency Anomalies")
                .topic("Transactions & Locking")
                .question("Detail the four standard SQL transaction isolation levels and the anomalies they prevent (Dirty Read, Non-Repeatable Read, Phantom Read).")
                .answer("1. Read Uncommitted: Permits dirty reads (reading uncommitted changes of another tx).\n" +
                        "2. Read Committed: Prevents dirty reads; allows non-repeatable reads (re-reading same row returns modified values).\n" +
                        "3. Repeatable Read: Prevents dirty and non-repeatable reads; snapshot isolation ensures rows read remain consistent.\n" +
                        "4. Serializable: Strictest level; guarantees execution equivalent to serial order, preventing phantom reads (new rows matching filter).")
                .sourceName("PostgreSQL Documentation")
                .sourceUrl("https://www.postgresql.org/docs/current/transaction-iso.html")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.DBMS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("How to Diagnose and Resolve the N+1 Query Problem")
                .topic("ORM & Performance Tuning")
                .question("What causes the N+1 query problem when querying relational data through ORMs like Hibernate, and how is it resolved?")
                .answer("Cause: Fetching 1 parent record and then executing N individual queries to fetch lazy child collections for each row.\n" +
                        "Solutions:\n" +
                        "1. JOIN FETCH in JPQL/HQL to retrieve parents and children in a single SQL JOIN query.\n" +
                        "2. @EntityGraph to configure eager fetching dynamically for specific queries.\n" +
                        "3. Batch fetching (@BatchSize(size = 25)) to retrieve children using SQL IN clauses.")
                .sourceName("Baeldung")
                .sourceUrl("https://www.baeldung.com/hibernate-n-plus-1")
                .build());

        // 4. OPERATING_SYSTEMS
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.OPERATING_SYSTEMS)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Process vs Thread and Context Switching")
                .topic("Processes & Threads")
                .question("What are the fundamental differences between a process and a thread, and what structures are swapped during a context switch?")
                .answer("A Process is an executing instance of a program with its own independent virtual address space, memory (code, data, heap), file descriptors, and security context. A Thread is the smallest unit of CPU scheduling executing within a process, sharing the process's heap and address space while retaining its own stack, registers, and program counter. Process context switching invalidates the CPU TLB cache and updates page table registers; thread context switching only saves registers and stack pointer.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/difference-between-process-and-thread/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.OPERATING_SYSTEMS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Four Conditions for Deadlock and Banker's Algorithm")
                .topic("Deadlock & Synchronization")
                .question("List the four Coffman conditions necessary for a deadlock to occur, and describe how deadlocks can be avoided.")
                .answer("1. Mutual Exclusion: At least one resource must be held in non-shareable mode.\n" +
                        "2. Hold and Wait: A process holds at least one resource and waits to acquire additional ones held by other processes.\n" +
                        "3. No Preemption: Resources cannot be forcibly taken; only released voluntarily.\n" +
                        "4. Circular Wait: A closed chain of processes where each process holds resources requested by the next.\n" +
                        "Deadlock prevention breaks at least one condition (e.g. strict resource ordering). Deadlock avoidance uses Banker's Algorithm to verify system stays in a safe state before granting requests.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/deadlock-in-operating-system/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.OPERATING_SYSTEMS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Virtual Memory, Paging, and Page Faults")
                .topic("Memory Management")
                .question("Explain how Virtual Memory provides address isolation, and walk through the step-by-step handling of a Page Fault by the OS.")
                .answer("Virtual memory translates process logical addresses to physical RAM addresses via the Memory Management Unit (MMU) and Page Tables. Step-by-step page fault flow:\n" +
                        "1. CPU tries to access a virtual address whose page table entry has valid bit = 0.\n" +
                        "2. MMU triggers a page fault trap to OS kernel.\n" +
                        "3. Kernel verifies address legality in process virtual memory map.\n" +
                        "4. Kernel finds a free physical frame (or evicts a page using LRU/Clock).\n" +
                        "5. Disk I/O reads requested page into the frame.\n" +
                        "6. Page table is updated with frame number and valid bit = 1.\n" +
                        "7. Trapped instruction restarts seamlessly.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/virtual-memory-in-operating-system/")
                .build());

        // 5. COMPUTER_NETWORKS
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.COMPUTER_NETWORKS)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("TCP vs UDP: Reliability vs Low Latency")
                .topic("Transport Layer")
                .question("Compare TCP and UDP in terms of connection state, sequencing, flow control, error checking, and ideal use cases.")
                .answer("TCP is connection-oriented, byte-stream based, reliable, and provides guaranteed in-order packet delivery via sequence numbers, ACKs, retransmissions, and sliding window flow control. Best for HTTP/HTTPS, SSH, Database connections.\n" +
                        "UDP is connectionless, datagram-based, unreliable with minimal header overhead (8 bytes vs 20+ bytes) and no retransmissions. Ideal for real-time applications where timeliness beats packet retransmission: DNS, VoIP, Video Streaming, Online Gaming.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/differences-between-tcp-and-udp/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.COMPUTER_NETWORKS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("The TCP Three-Way Handshake and Four-Way Teardown")
                .topic("TCP Protocol")
                .question("Explain how a client and server establish a TCP connection using SYN, SYN-ACK, ACK, and how connection termination works.")
                .answer("Establishment (3-Way Handshake):\n" +
                        "1. Client sends SYN (seq = x) to server (Client -> SYN_SENT).\n" +
                        "2. Server responds with SYN-ACK (seq = y, ack = x + 1) (Server -> SYN_RECEIVED).\n" +
                        "3. Client sends ACK (ack = y + 1) (Both reach ESTABLISHED).\n" +
                        "Teardown (4-Way Termination):\n" +
                        "1. Initiator sends FIN (seq = u).\n" +
                        "2. Receiver sends ACK (ack = u + 1) and can continue sending pending data.\n" +
                        "3. Receiver sends its own FIN (seq = v).\n" +
                        "4. Initiator sends ACK (ack = v + 1) and enters TIME_WAIT (2*MSL) to ensure server receives final ACK.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/tcp-3-way-handshake-process/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.COMPUTER_NETWORKS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("What happens when you type a URL in the browser?")
                .topic("Web Architecture & DNS")
                .question("Walk through the entire journey from typing 'https://example.com' into a browser to the page rendering.")
                .answer("1. URL parsing and HSTS check.\n" +
                        "2. DNS Lookup: Browser cache -> OS cache -> Router -> ISP Recursive Resolver -> Root/TLD/Authoritative Nameservers to resolve IP.\n" +
                        "3. TCP 3-Way Handshake with server on port 443.\n" +
                        "4. TLS 1.3 Handshake: Certificate validation, Diffie-Hellman key exchange, symmetric session key establishment.\n" +
                        "5. HTTP GET request sent over encrypted TLS tunnel.\n" +
                        "6. Server processes request through reverse proxy/load balancer and returns HTTP 200 OK with HTML payload.\n" +
                        "7. Browser engine parses DOM and CSSOM, builds Render Tree, runs Layout and Paint cycles.")
                .sourceName("Cloudflare Learning")
                .sourceUrl("https://www.cloudflare.com/learning/dns/what-is-dns/")
                .build());

        // 6. OOP
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.OOP)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("The Four Pillars of Object-Oriented Programming")
                .topic("OOP Fundamentals")
                .question("Define Encapsulation, Abstraction, Inheritance, and Polymorphism with concrete software engineering examples.")
                .answer("Encapsulation: Bundling data and methods operating on that data within a single class and restricting direct access to object internals via private fields and public accessors.\n" +
                        "Abstraction: Exposing only high-level conceptual interfaces while hiding implementation complexity (e.g., java.util.List interface hiding ArrayList/LinkedList algorithms).\n" +
                        "Inheritance: Mechanism where a child class acquires attributes and behavior from a parent class for code reuse.\n" +
                        "Polymorphism: Ability of an entity to take multiple forms—Compile-time (Method Overloading) and Runtime (Method Overriding via dynamic method dispatch).")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/object-oriented-programming-oops-concept-in-java/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.OOP)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("SOLID Principles with Java Design Patterns")
                .topic("Software Architecture")
                .question("State each principle in SOLID and explain how Dependency Inversion Principle (DIP) enables loose coupling in frameworks.")
                .answer("S - Single Responsibility: A class should have one, and only one, reason to change.\n" +
                        "O - Open/Closed: Software entities should be open for extension, but closed for modification.\n" +
                        "L - Liskov Substitution: Subtypes must be substitutable for their base types without altering program correctness.\n" +
                        "I - Interface Segregation: Clients should not be forced to depend on interfaces they do not use.\n" +
                        "D - Dependency Inversion: High-level modules should not depend on low-level modules; both should depend on abstractions. In Spring, controllers depend on service interfaces, injected via constructor injection.")
                .sourceName("Baeldung")
                .sourceUrl("https://www.baeldung.com/solid-principles")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.OOP)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Why Favor Composition Over Inheritance?")
                .topic("Design Patterns")
                .question("Explain the principle 'Favor Composition Over Inheritance' and what problems rigid inheritance hierarchies introduce.")
                .answer("Inheritance creates tight compile-time coupling ('white-box reuse') where subclass behavior can break if parent implementation changes (Fragile Base Class problem). Composition ('has-a') embeds references to interface types, allowing runtime swapping of behavior (Strategy Pattern), independent unit testing with mocks, and avoiding monolithic class hierarchies.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/favoring-composition-over-inheritance-in-java-with-examples/")
                .build());

        // 7. SPRING_BOOT
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.SPRING_BOOT)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Inversion of Control (IoC) and Dependency Injection in Spring")
                .topic("Spring Core")
                .question("What is IoC in Spring Boot and why is Constructor Injection strongly favored over Field Injection (@Autowired)?")
                .answer("IoC transfers control of object instantiation, configuration, and lifecycle from application code to the Spring ApplicationContext container. Constructor Injection is preferred over field injection because:\n" +
                        "1. Immutability: Injected dependencies can be declared final.\n" +
                        "2. Ease of Unit Testing: Dependencies can be supplied directly in unit test constructors without Spring Context or reflection.\n" +
                        "3. Fail-fast on startup: Circular dependencies and missing beans fail at initialization time.")
                .sourceName("Spring Official Guides")
                .sourceUrl("https://spring.io/guides")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.SPRING_BOOT)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("How Spring Boot Auto-Configuration Works")
                .topic("Auto-Configuration")
                .question("Explain the magic behind @SpringBootApplication and how @EnableAutoConfiguration conditionally loads beans.")
                .answer("@SpringBootApplication combines @Configuration, @EnableAutoConfiguration, and @ComponentScan. Under the hood, Spring Boot reads auto-configuration classes declared in META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports. It evaluates condition annotations (@ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty) to only register beans if required classes are present on the classpath and user hasn't defined custom beans.")
                .sourceName("Baeldung")
                .sourceUrl("https://www.baeldung.com/spring-boot-auto-configuration")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.SPRING_BOOT)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Spring Boot Transaction Management (@Transactional)")
                .topic("Transactions & AOP")
                .question("How does Spring manage transactions using Spring AOP dynamic proxies, and why does calling a @Transactional method from within the same class bypass transaction management?")
                .answer("Spring wraps beans with a dynamic proxy (CGLIB or JDK dynamic proxy). When a client calls a @Transactional method on the bean, the proxy intercepts the call, opens a JDBC transaction via PlatformTransactionManager, executes target method, commits if no unhandled RuntimeException occurs, or issues rollback. Self-invocation (calling this.method() internally) bypasses the proxy wrapper, executing plain code without transaction interceptor.")
                .sourceName("Baeldung")
                .sourceUrl("https://www.baeldung.com/transaction-configuration-with-jpa-and-spring")
                .build());

        // 8. HR & BEHAVIORAL
        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.HR)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Tell Me About Yourself (Elevator Pitch for Tech Roles)")
                .topic("Personal Introduction")
                .question("How should you structure the 'Tell me about yourself' question for a software developer role?")
                .answer("Use the Present-Past-Future framework in under 90 seconds:\n" +
                        "1. Present: State your current status, specialization (e.g., Full Stack / Backend Java Engineer), and current key technologies.\n" +
                        "2. Past: Highlight 1-2 impactful projects, internships, or degrees where you delivered measurable value.\n" +
                        "3. Future: Articulate why this specific company and position aligns with your technical growth and goals.")
                .sourceName("Harvard Career Services")
                .sourceUrl("https://careerservices.fas.harvard.edu/")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.HR)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Why Do You Want to Work at Our Company?")
                .topic("Company Alignment")
                .question("How do you effectively answer 'Why do you want to join our company?' without giving generic platitudes?")
                .answer("Research three company pillars beforehand: Product/Engineering challenge (e.g. high-throughput processing), Company culture/mission, and recent tech blog or open source contributions. Connect your personal technical interests directly to their roadmap.")
                .sourceName("Indeed Career Guide")
                .sourceUrl("https://www.indeed.com/career-advice/interviewing/why-do-you-want-to-work-here")
                .build());

        questions.add(InterviewQuestionBankItem.builder()
                .category(InterviewQuestionCategory.BEHAVIORAL)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Answering Technical Challenges with the STAR Method")
                .topic("STAR Framework")
                .question("Explain the STAR method (Situation, Task, Action, Result) for behavioral questions about conflict, failure, or production incidents.")
                .answer("S - Situation: Set the scene (context, project, team size) concisely in 2 sentences.\n" +
                        "T - Task: Define your specific responsibility or the critical obstacle faced.\n" +
                        "A - Action: Describe the steps YOU specifically executed (diagnosing logs, running profiling, collaborating with stakeholders).\n" +
                        "R - Result: Conclude with quantifiable outcomes (e.g., reduced latency by 35%, zero data loss, system restored in 15 mins).")
                .sourceName("Indeed Career Guide")
                .sourceUrl("https://www.indeed.com/career-advice/interviewing/how-to-use-the-star-interview-response-technique")
                .build());

        repository.saveAll(questions);
    }
}
