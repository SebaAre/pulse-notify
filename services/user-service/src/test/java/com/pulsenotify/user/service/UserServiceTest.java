package com.pulsenotify.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.user.dto.CreateUserRequest;
import com.pulsenotify.user.dto.UserResponse;
import com.pulsenotify.user.exception.UserAlreadyExistsException;
import com.pulsenotify.user.exception.UserNotFoundException;
import com.pulsenotify.user.model.UserAccount;
import com.pulsenotify.user.model.UserPreference;
import com.pulsenotify.user.repository.UserAccountRepository;
import com.pulsenotify.user.repository.UserPreferenceRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_savesUserAndOnePreferencePerChannel() {
        // ARRANGE
        CreateUserRequest request = new CreateUserRequest(
                "user@example.com",
                "+54911111111",
                "push-token-123",
                "Sebastian Arellano",
                "America/Argentina/Buenos_Aires"
        );

        UUID generatedId = UUID.randomUUID();
        UserAccount savedUser = UserAccount.builder()
                .id(generatedId)
                .email(request.email())
                .phone(request.phone())
                .pushToken(request.pushToken())
                .displayName(request.displayName())
                .timezone(request.timezone())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userAccountRepository.existsByEmail(request.email())).thenReturn(false);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);

        // ACT
        UserResponse response = userService.createUser(request);

        // ASSERT
        assertThat(response.id()).isEqualTo(generatedId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.active()).isTrue();
        verify(userPreferenceRepository, times(3)).save(any(UserPreference.class));
    }

    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        // ARRANGE
        CreateUserRequest request = new CreateUserRequest(
                "user@example.com",
                "+54911111111",
                "push-token-123",
                "Sebastian Arellano",
                "America/Argentina/Buenos_Aires"
        );
        when(userAccountRepository.existsByEmail(request.email())).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(request.email());

        verify(userAccountRepository, never()).save(any(UserAccount.class));
        verify(userPreferenceRepository, never()).save(any(UserPreference.class));
    }

    @Test
    void getUserById_throwsWhenNotFound() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        when(userAccountRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}