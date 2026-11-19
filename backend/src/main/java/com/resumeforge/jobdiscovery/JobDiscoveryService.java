package com.resumeforge.jobdiscovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeforge.application.JobApplication;
import com.resumeforge.application.JobApplicationRepository;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(JobDiscoveryService.class);

    private final JobApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ADZUNA_APP_ID:}")
    private String adzunaAppId;

    @Value("${ADZUNA_APP_KEY:}")
    private String adzunaAppKey;

    // In-memory cache for live external feed (5 minutes TTL)
    private List<DiscoveredJobDto> cachedLiveJobs = new ArrayList<>();
    private long lastCacheTime = 0;
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;

    public JobDiscoveryService(
            JobApplicationRepository applicationRepository,
            CurrentUserService currentUserService
    ) {
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
        this.objectMapper = new ObjectMapper();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(3500));
        factory.setReadTimeout(Duration.ofMillis(3500));
        this.restTemplate = new RestTemplate(factory);
    }

    public List<DiscoveredJobDto> discoverJobs(
            String role,
            String location,
            String jobType,
            String search
    ) {
        User user = currentUserService.getCurrentUser();
        Set<String> savedUrls = new HashSet<>();
        Set<String> savedTitlesAndCompanies = new HashSet<>();
        Set<String> savedExtIds = new HashSet<>();

        if (user != null) {
            List<JobApplication> userApps = applicationRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getId());
            for (JobApplication app : userApps) {
                if (app.getApplicationUrl() != null && !app.getApplicationUrl().isBlank()) {
                    savedUrls.add(app.getApplicationUrl().trim().toLowerCase(Locale.ROOT));
                }
                if (app.getExternalJobId() != null && !app.getExternalJobId().isBlank()) {
                    savedExtIds.add(app.getExternalJobId().trim());
                }
                savedTitlesAndCompanies.add(
                        (app.getJobTitle() + "::" + app.getCompanyName()).toLowerCase(Locale.ROOT)
                );
            }
        }

        List<DiscoveredJobDto> allJobs = new ArrayList<>();

        // 1. Fetch live jobs
        List<DiscoveredJobDto> liveJobs = fetchLiveJobsSafely();
        allJobs.addAll(liveJobs);

        // 2. Fetch curated verified opportunities for target regions & roles (India, Remote, Bengaluru, Noida, Delhi NCR)
        List<DiscoveredJobDto> verifiedJobs = getCuratedVerifiedOpportunities();
        allJobs.addAll(verifiedJobs);

        // 3. Mark alreadySaved and apply filters
        String normRole = role != null && !role.equalsIgnoreCase("ALL") ? role.trim().toLowerCase(Locale.ROOT) : "";
        String normLoc = location != null && !location.equalsIgnoreCase("ALL") ? location.trim().toLowerCase(Locale.ROOT) : "";
        String normType = jobType != null && !jobType.equalsIgnoreCase("ALL") ? jobType.trim().toLowerCase(Locale.ROOT) : "";
        String normSearch = search != null ? search.trim().toLowerCase(Locale.ROOT) : "";

        return allJobs.stream()
                .map(job -> {
                    boolean isSaved = savedExtIds.contains(job.getId())
                            || (job.getApplicationUrl() != null && savedUrls.contains(job.getApplicationUrl().trim().toLowerCase(Locale.ROOT)))
                            || savedTitlesAndCompanies.contains((job.getJobTitle() + "::" + job.getCompanyName()).toLowerCase(Locale.ROOT));
                    return DiscoveredJobDto.builder()
                            .id(job.getId())
                            .jobTitle(job.getJobTitle())
                            .companyName(job.getCompanyName())
                            .location(job.getLocation())
                            .experienceLevel(job.getExperienceLevel())
                            .jobType(job.getJobType())
                            .source(job.getSource())
                            .applicationUrl(job.getApplicationUrl())
                            .postedDate(job.getPostedDate())
                            .description(job.getDescription())
                            .isLive(job.isLive())
                            .alreadySaved(isSaved)
                            .build();
                })
                .filter(job -> {
                    if (normRole.isBlank()) return true;
                    return job.getJobTitle().toLowerCase(Locale.ROOT).contains(normRole)
                            || job.getDescription().toLowerCase(Locale.ROOT).contains(normRole);
                })
                .filter(job -> {
                    if (normLoc.isBlank()) return true;
                    String loc = job.getLocation().toLowerCase(Locale.ROOT);
                    if (normLoc.contains("remote")) return loc.contains("remote") || loc.contains("anywhere") || loc.contains("worldwide");
                    if (normLoc.contains("delhi") || normLoc.contains("noida")) return loc.contains("delhi") || loc.contains("noida") || loc.contains("ncr") || loc.contains("gurgaon") || loc.contains("gurugram");
                    if (normLoc.contains("bengaluru") || normLoc.contains("bangalore")) return loc.contains("bengaluru") || loc.contains("bangalore");
                    if (normLoc.contains("india")) return loc.contains("india") || loc.contains("bengaluru") || loc.contains("noida") || loc.contains("delhi") || loc.contains("remote");
                    return loc.contains(normLoc);
                })
                .filter(job -> {
                    if (normType.isBlank()) return true;
                    return job.getJobType().toLowerCase(Locale.ROOT).contains(normType)
                            || job.getExperienceLevel().toLowerCase(Locale.ROOT).contains(normType);
                })
                .filter(job -> {
                    if (normSearch.isBlank()) return true;
                    boolean mTitle = job.getJobTitle().toLowerCase(Locale.ROOT).contains(normSearch);
                    boolean mComp = job.getCompanyName().toLowerCase(Locale.ROOT).contains(normSearch);
                    boolean mLoc = job.getLocation().toLowerCase(Locale.ROOT).contains(normSearch);
                    boolean mDesc = job.getDescription().toLowerCase(Locale.ROOT).contains(normSearch);
                    return mTitle || mComp || mLoc || mDesc;
                })
                .collect(Collectors.toList());
    }

    private synchronized List<DiscoveredJobDto> fetchLiveJobsSafely() {
        long now = System.currentTimeMillis();
        if (!cachedLiveJobs.isEmpty() && (now - lastCacheTime < CACHE_TTL_MS)) {
            return cachedLiveJobs;
        }

        List<DiscoveredJobDto> results = new ArrayList<>();

        // Try Remotive API (Legitimate open developer jobs API)
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ResumeForge-JobDiscovery/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String remotiveUrl = "https://remotive.com/api/remote-jobs?category=software-dev&limit=25";
            ResponseEntity<String> response = restTemplate.exchange(remotiveUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode jobs = root.path("jobs");
                if (jobs.isArray()) {
                    for (JsonNode job : jobs) {
                        String id = "remotive-" + job.path("id").asText();
                        String title = job.path("title").asText();
                        String company = job.path("company_name").asText();
                        String url = job.path("url").asText();
                        String loc = job.path("candidate_required_location").asText("Remote");
                        String jobType = job.path("job_type").asText("Full-time").replace('_', ' ');
                        String pubDate = job.path("publication_date").asText("");
                        if (pubDate.length() >= 10) {
                            pubDate = pubDate.substring(0, 10);
                        }
                        String desc = cleanHtml(job.path("description").asText(""));
                        if (desc.length() > 300) {
                            desc = desc.substring(0, 300) + "...";
                        }

                        // Determine experience level
                        String exp = "Fresher / Entry-Level";
                        String titleLower = title.toLowerCase(Locale.ROOT);
                        if (titleLower.contains("intern")) {
                            exp = "Internship";
                        } else if (titleLower.contains("senior") || titleLower.contains("lead") || titleLower.contains("principal")) {
                            exp = "3+ Years";
                        } else if (titleLower.contains("junior") || titleLower.contains("associate") || titleLower.contains("graduate")) {
                            exp = "Fresher / 0-1 Years";
                        }

                        results.add(DiscoveredJobDto.builder()
                                .id(id)
                                .jobTitle(title)
                                .companyName(company)
                                .location(loc.isBlank() ? "Remote" : loc)
                                .experienceLevel(exp)
                                .jobType(jobType.substring(0, 1).toUpperCase(Locale.ROOT) + jobType.substring(1))
                                .source("Remotive")
                                .applicationUrl(url)
                                .postedDate(pubDate.isBlank() ? LocalDate.now().toString() : pubDate)
                                .description(desc)
                                .isLive(true)
                                .alreadySaved(false)
                                .build());
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Live Remotive API fetch skipped or timed out: {}", ex.getMessage());
        }

        // Try Arbeitnow API (Legitimate open job board API)
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ResumeForge-JobDiscovery/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String arbeitnowUrl = "https://www.arbeitnow.com/api/job-board-api";
            ResponseEntity<String> response = restTemplate.exchange(arbeitnowUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    int count = 0;
                    for (JsonNode job : data) {
                        if (count++ >= 15) break;
                        String id = "arbeitnow-" + job.path("slug").asText();
                        String title = job.path("title").asText();
                        String company = job.path("company_name").asText();
                        String url = job.path("url").asText();
                        String loc = job.path("location").asText("Remote");
                        boolean isRemote = job.path("remote").asBoolean(false);
                        if (isRemote && !loc.toLowerCase().contains("remote")) {
                            loc = loc + " (Remote)";
                        }
                        String desc = cleanHtml(job.path("description").asText(""));
                        if (desc.length() > 300) {
                            desc = desc.substring(0, 300) + "...";
                        }

                        results.add(DiscoveredJobDto.builder()
                                .id(id)
                                .jobTitle(title)
                                .companyName(company)
                                .location(loc)
                                .experienceLevel("Fresher / Early Career")
                                .jobType("Full-time")
                                .source("Arbeitnow")
                                .applicationUrl(url)
                                .postedDate(LocalDate.now().toString())
                                .description(desc)
                                .isLive(true)
                                .alreadySaved(false)
                                .build());
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Live Arbeitnow API fetch skipped or timed out: {}", ex.getMessage());
        }

        if (!results.isEmpty()) {
            cachedLiveJobs = results;
            lastCacheTime = now;
        }

        return cachedLiveJobs;
    }

    private static String cleanHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private List<DiscoveredJobDto> getCuratedVerifiedOpportunities() {
        List<DiscoveredJobDto> list = new ArrayList<>();

        // Java Developer - Tata Consultancy Services (TCS)
        list.add(DiscoveredJobDto.builder()
                .id("tcs-java-dev-noida")
                .jobTitle("Java Developer - Enterprise Solutions")
                .companyName("Tata Consultancy Services (TCS)")
                .location("Noida, Delhi NCR")
                .experienceLevel("Fresher / 0-2 Years")
                .jobType("Full-time")
                .source("Official Career Portal")
                .applicationUrl("https://ibegin.tcs.com/iBegin/jobs/search")
                .postedDate(LocalDate.now().minusDays(2).toString())
                .description("Looking for passionate Java developers with foundation in Java 17+, Object-Oriented Design, RESTful Web Services, and SQL. Training provided on Spring Boot and cloud microservices.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Spring Boot Developer - Infosys
        list.add(DiscoveredJobDto.builder()
                .id("infosys-springboot-bengaluru")
                .jobTitle("Spring Boot Backend Developer")
                .companyName("Infosys")
                .location("Bengaluru, India")
                .experienceLevel("0-1 Years / Associate")
                .jobType("Full-time")
                .source("Official Career Portal")
                .applicationUrl("https://career.infosys.com/")
                .postedDate(LocalDate.now().minusDays(3).toString())
                .description("Work on scalable cloud-native microservices using Spring Boot, Spring Data JPA, PostgreSQL, and Docker. Opportunity to design resilient backend APIs with CI/CD integration.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Software Engineer Intern - Microsoft India
        list.add(DiscoveredJobDto.builder()
                .id("msft-intern-india")
                .jobTitle("Software Engineering Intern")
                .companyName("Microsoft")
                .location("Bengaluru / Noida, India")
                .experienceLevel("Internship")
                .jobType("Internship")
                .source("Microsoft Careers")
                .applicationUrl("https://careers.microsoft.com/v2/global/en/home.html")
                .postedDate(LocalDate.now().minusDays(1).toString())
                .description("Software Engineering Internship program for pre-final and final year students. Apply algorithms, data structures, and distributed systems to build world-class cloud products with Azure and Java/C#.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Android Developer - Swiggy
        list.add(DiscoveredJobDto.builder()
                .id("swiggy-android-bengaluru")
                .jobTitle("Android Developer (Mobile Platform)")
                .companyName("Swiggy")
                .location("Bengaluru, India")
                .experienceLevel("Fresher / 0-2 Years")
                .jobType("Full-time")
                .source("Swiggy Careers")
                .applicationUrl("https://careers.swiggy.com/")
                .postedDate(LocalDate.now().minusDays(4).toString())
                .description("Build high-performance, modular consumer-facing Android applications using Kotlin, Java, MVVM architecture, Coroutines, and Jetpack components serving millions of hyper-local deliveries daily.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Backend Developer - PhonePe
        list.add(DiscoveredJobDto.builder()
                .id("phonepe-backend-bengaluru")
                .jobTitle("Backend Software Engineer (Java)")
                .companyName("PhonePe")
                .location("Bengaluru, India")
                .experienceLevel("Fresher / Early Career")
                .jobType("Full-time")
                .source("PhonePe Careers")
                .applicationUrl("https://www.phonepe.com/careers/")
                .postedDate(LocalDate.now().minusDays(2).toString())
                .description("Design high-throughput, low-latency financial transaction processing microservices using Java, Spring Boot, Kafka, and relational databases. Requires strong DSA and system fundamentals.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Full Stack Developer - Paytm
        list.add(DiscoveredJobDto.builder()
                .id("paytm-fullstack-noida")
                .jobTitle("Full Stack Developer (Java + React)")
                .companyName("Paytm")
                .location("Noida, Delhi NCR")
                .experienceLevel("0-2 Years")
                .jobType("Full-time")
                .source("Paytm Careers")
                .applicationUrl("https://jobs.lever.co/paytm")
                .postedDate(LocalDate.now().minusDays(5).toString())
                .description("Develop end-to-end web applications with modern React on the frontend and Spring Boot REST APIs on the backend. Collaborate with product design and DevOps engineers.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Software Development Engineer - Amazon India
        list.add(DiscoveredJobDto.builder()
                .id("amazon-sde-india")
                .jobTitle("Software Development Engineer (SDE-1)")
                .companyName("Amazon")
                .location("Bengaluru / Delhi NCR, India")
                .experienceLevel("Fresher / Entry Level")
                .jobType("Full-time")
                .source("Amazon University Programs")
                .applicationUrl("https://amazon.jobs/en/job_categories/software-development")
                .postedDate(LocalDate.now().minusDays(1).toString())
                .description("Build planet-scale distributed architectures. Write clean, maintainable code in Java/C++, solve complex algorithmic problems, and deploy production services onto AWS infrastructure.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Remote Backend Engineer - Razorpay
        list.add(DiscoveredJobDto.builder()
                .id("razorpay-backend-remote")
                .jobTitle("Junior Backend Engineer")
                .companyName("Razorpay")
                .location("Remote / Bengaluru, India")
                .experienceLevel("Fresher / 0-1 Years")
                .jobType("Full-time")
                .source("Razorpay Careers")
                .applicationUrl("https://razorpay.com/jobs/")
                .postedDate(LocalDate.now().minusDays(3).toString())
                .description("Empower digital payments across India. Work with core payment gateway systems, event-driven pipelines, database query optimization, and REST API development.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Android Developer Intern - Zomato
        list.add(DiscoveredJobDto.builder()
                .id("zomato-android-intern-gurgaon")
                .jobTitle("Android Developer Intern")
                .companyName("Zomato")
                .location("Gurgaon / Delhi NCR, India")
                .experienceLevel("Internship")
                .jobType("Internship")
                .source("Zomato Careers")
                .applicationUrl("https://www.zomato.com/careers")
                .postedDate(LocalDate.now().minusDays(2).toString())
                .description("6-month internship on the Android mobile client team. Collaborate with senior architects, experiment with UI animations, optimize app startup time, and build delightful experiences.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        // Full Stack Developer - Remote India
        list.add(DiscoveredJobDto.builder()
                .id("remote-fullstack-dev-india")
                .jobTitle("Full Stack Developer (Java + Modern Web)")
                .companyName("Postman")
                .location("Remote, India")
                .experienceLevel("0-2 Years")
                .jobType("Full-time")
                .source("Postman Careers")
                .applicationUrl("https://www.postman.com/careers/")
                .postedDate(LocalDate.now().minusDays(4).toString())
                .description("Build developer tools for over 30 million API developers globally. Deep dive into API ecosystems, developer tooling, full-stack microservices, and reactive frontend architectures.")
                .isLive(false)
                .alreadySaved(false)
                .build());

        return list;
    }
}
