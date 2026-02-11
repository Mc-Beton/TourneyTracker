package com.tourney.service.user;

import com.common.domain.User;
import com.tourney.dto.user.UserLookupDTO;
import com.tourney.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLookupServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserLookupService userLookupService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john@example.com");

        user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");

        user3 = new User();
        user3.setId(3L);
        user3.setName("Johnny Walker");
        user3.setEmail("johnny@example.com");
    }

    // ===== VALID SEARCH TESTS =====

    @Test
    void testSearchByName_ValidQuery_ReturnsResults() {
        // Given
        String query = "John";
        Long currentUserId = null;
        Integer limit = 10;
        List<User> users = Arrays.asList(user1, user3);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, currentUserId, limit);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("John Doe", results.get(0).getName());
        assertEquals("Johnny Walker", results.get(1).getName());
    }

    @Test
    void testSearchByName_MinQueryLength_ReturnsResults() {
        // Given - exactly 4 characters (minimum)
        String query = "John";
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertEquals(1, results.size());
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any());
    }

    // ===== SHORT QUERY TESTS =====

    @Test
    void testSearchByName_QueryTooShort_ReturnsEmptyList() {
        // Given - query with less than 4 characters
        String query = "Joe";

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(userRepository, never()).findByNameContainingIgnoreCaseOrderByNameAsc(any(), any());
    }

    @Test
    void testSearchByName_EmptyQuery_ReturnsEmptyList() {
        // Given
        String query = "";

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertTrue(results.isEmpty());
        verify(userRepository, never()).findByNameContainingIgnoreCaseOrderByNameAsc(any(), any());
    }

    @Test
    void testSearchByName_NullQuery_ReturnsEmptyList() {
        // Given
        String query = null;

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertTrue(results.isEmpty());
        verify(userRepository, never()).findByNameContainingIgnoreCaseOrderByNameAsc(any(), any());
    }

    // ===== CURRENT USER FILTERING TESTS =====

    @Test
    void testSearchByName_FiltersSelf() {
        // Given
        String query = "John";
        Long currentUserId = 1L; // user1's ID
        List<User> users = Arrays.asList(user1, user3);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, currentUserId, 10);

        // Then
        assertEquals(1, results.size());
        assertEquals(3L, results.get(0).getId()); // Only user3, user1 filtered out
        assertEquals("Johnny Walker", results.get(0).getName());
    }

    @Test
    void testSearchByName_NoCurrentUser_ReturnsAll() {
        // Given
        String query = "John";
        Long currentUserId = null;
        List<User> users = Arrays.asList(user1, user3);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, currentUserId, 10);

        // Then
        assertEquals(2, results.size());
    }

    // ===== LIMIT TESTS =====

    @Test
    void testSearchByName_DefaultLimit() {
        // Given
        String query = "test";
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        userLookupService.searchByName(query, null, null);

        // Then
        verify(userRepository).findByNameContainingIgnoreCaseOrderByNameAsc(eq(query),
                argThat(page -> page.getPageSize() == 10));
    }

    @Test
    void testSearchByName_CustomLimit() {
        // Given
        String query = "test";
        Integer customLimit = 5;
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        userLookupService.searchByName(query, null, customLimit);

        // Then
        verify(userRepository).findByNameContainingIgnoreCaseOrderByNameAsc(eq(query),
                argThat(page -> page.getPageSize() == 5));
    }

    @Test
    void testSearchByName_LimitTooHigh_UsesDefault() {
        // Given
        String query = "test";
        Integer tooHighLimit = 100;
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        userLookupService.searchByName(query, null, tooHighLimit);

        // Then
        verify(userRepository).findByNameContainingIgnoreCaseOrderByNameAsc(eq(query),
                argThat(page -> page.getPageSize() == 10)); // Uses default
    }

    @Test
    void testSearchByName_NegativeLimit_UsesDefault() {
        // Given
        String query = "test";
        Integer negativeLimit = -5;
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        userLookupService.searchByName(query, null, negativeLimit);

        // Then
        verify(userRepository).findByNameContainingIgnoreCaseOrderByNameAsc(eq(query),
                argThat(page -> page.getPageSize() == 10));
    }

    // ===== DTO MAPPING TESTS =====

    @Test
    void testSearchByName_CorrectDTOMapping() {
        // Given
        String query = "Jane";
        List<User> users = Collections.singletonList(user2);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertEquals(1, results.size());
        UserLookupDTO dto = results.get(0);
        assertEquals(user2.getId(), dto.getId());
        assertEquals(user2.getName(), dto.getName());
    }

    // ===== CASE INSENSITIVITY TESTS =====

    @Test
    void testSearchByName_CaseInsensitive() {
        // Given
        String query = "JOHN";
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertEquals(1, results.size());
        verify(userRepository).findByNameContainingIgnoreCaseOrderByNameAsc(eq(query), any());
    }

    @Test
    void testSearchByName_TrimsWhitespace() {
        // Given
        String query = "  John  ";
        List<User> users = Collections.singletonList(user1);

        when(userRepository.findByNameContainingIgnoreCaseOrderByNameAsc(eq("John"), any(PageRequest.class)))
                .thenReturn(users);

        // When
        List<UserLookupDTO> results = userLookupService.searchByName(query, null, 10);

        // Then
        assertEquals(1, results.size());
        verify(userRepository).findByNameContainingIgnoreCaseOrderByNameAsc(eq("John"), any());
    }
}
