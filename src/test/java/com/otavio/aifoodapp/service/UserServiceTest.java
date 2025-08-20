package com.otavio.aifoodapp.service;

import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;
import com.otavio.aifoodapp.enums.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRoles.USER);
        testUser.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void findUserByEmail_ShouldReturnUser_WhenEmailExists() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findUserByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findUserByEmail_ShouldReturnNull_WhenEmailDoesNotExist() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        User result = userService.findUserByEmail(email);

        // Then
        assertNull(result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findUserByEmail_ShouldReturnNull_WhenEmailIsNull() {
        // When
        User result = userService.findUserByEmail(null);

        // Then
        assertNull(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void findUserByEmail_ShouldReturnNull_WhenRepositoryThrowsException() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

        // When
        User result = userService.findUserByEmail(email);

        // Then
        assertNull(result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findUserById_ShouldReturnUser_WhenIdExists() {
        // Given
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findUserById(id);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findById(id);
    }

    @Test
    void findUserById_ShouldReturnNull_WhenIdDoesNotExist() {
        // Given
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // When
        User result = userService.findUserById(id);

        // Then
        assertNull(result);
        verify(userRepository).findById(id);
    }

    @Test
    void findUserById_ShouldReturnNull_WhenIdIsNull() {
        // When
        User result = userService.findUserById(null);

        // Then
        assertNull(result);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void findUserById_ShouldReturnNull_WhenRepositoryThrowsException() {
        // Given
        Long id = 1L;
        when(userRepository.findById(id)).thenThrow(new RuntimeException("Database error"));

        // When
        User result = userService.findUserById(id);

        // Then
        assertNull(result);
        verify(userRepository).findById(id);
    }
}