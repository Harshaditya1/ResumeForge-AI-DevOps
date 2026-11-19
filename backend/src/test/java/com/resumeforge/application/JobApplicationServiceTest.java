package com.resumeforge.application;

import com.resumeforge.application.dto.JobApplicationRequestDto;
import com.resumeforge.application.dto.JobApplicationResponseDto;
import com.resumeforge.application.dto.JobApplicationStatisticsDto;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private CurrentUserService currentUserService;

    private JobApplicationService service;
    private User testUser;

    @BeforeEach
    void setUp() {
        service = new JobApplicationService(repository, currentUserService);
        testUser = User.builder().id(1L).fullName("Test User").email("user@example.com").build();
    }

    @Test
    void testCreateApplicationSuccessfully() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(repository.existsByUserIdAndCompanyNameIgnoreCaseAndJobTitleIgnoreCase(1L, "Infosys", "Java Developer"))
                .thenReturn(false);

        JobApplicationRequestDto request = new JobApplicationRequestDto();
        request.setCompanyName("Infosys");
        request.setJobTitle("Java Developer");
        request.setLocation("Bengaluru");
        request.setStatus(JobApplicationStatus.SAVED);
        request.setDeadline(LocalDate.now().plusDays(10));

        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication app = invocation.getArgument(0);
            app.setId(10L);
            return app;
        });

        JobApplicationResponseDto response = service.create(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Infosys", response.getCompanyName());
        assertEquals("Java Developer", response.getJobTitle());
        assertEquals("Bengaluru", response.getLocation());
        assertEquals(JobApplicationStatus.SAVED, response.getStatus());
        verify(repository, times(1)).save(any(JobApplication.class));
    }

    @Test
    void testPreventDuplicateApplication() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(repository.existsByUserIdAndCompanyNameIgnoreCaseAndJobTitleIgnoreCase(1L, "Infosys", "Java Developer"))
                .thenReturn(true);

        JobApplicationRequestDto request = new JobApplicationRequestDto();
        request.setCompanyName("Infosys");
        request.setJobTitle("Java Developer");

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verify(repository, never()).save(any(JobApplication.class));
    }

    @Test
    void testGetStatistics() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(repository.countByUserId(1L)).thenReturn(5L);
        lenient().when(repository.countByUserIdAndStatus(eq(1L), any(JobApplicationStatus.class))).thenReturn(0L);
        lenient().when(repository.countByUserIdAndStatus(1L, JobApplicationStatus.SAVED)).thenReturn(2L);
        lenient().when(repository.countByUserIdAndStatus(1L, JobApplicationStatus.APPLIED)).thenReturn(2L);
        lenient().when(repository.countByUserIdAndStatus(1L, JobApplicationStatus.INTERVIEW)).thenReturn(1L);

        JobApplicationStatisticsDto stats = service.getStatistics();

        assertEquals(5L, stats.getTotal());
        assertEquals(2L, stats.getByStatus().get(JobApplicationStatus.SAVED));
        assertEquals(2L, stats.getByStatus().get(JobApplicationStatus.APPLIED));
        assertEquals(1L, stats.getByStatus().get(JobApplicationStatus.INTERVIEW));
    }
}
