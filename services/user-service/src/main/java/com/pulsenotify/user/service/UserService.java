package com.pulsenotify.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.user.dto.CreateUserRequest;
import com.pulsenotify.user.dto.UpdateUserRequest;
import com.pulsenotify.user.dto.UserResponse;
import com.pulsenotify.user.exception.UserAlreadyExistsException;
import com.pulsenotify.user.exception.UserNotFoundException;
import com.pulsenotify.user.model.UserAccount;
import com.pulsenotify.user.model.UserPreference;
import com.pulsenotify.user.repository.UserAccountRepository;
import com.pulsenotify.user.repository.UserPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserAccountRepository userAccountRepository;

    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request){

        if(userAccountRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException(request.email());
        }

        UserAccount userAccount = UserAccount.builder()
            .email(request.email())
            .phone(request.phone())
            .pushToken(request.pushToken())
            .displayName(request.displayName())
            .timezone(request.timezone())
            .build();
        
        UserAccount savedUser = userAccountRepository.save(userAccount);

        for (NotificationChannel channel : NotificationChannel.values()) {
            UserPreference preference = UserPreference.builder()
                .userId(savedUser.getId())
                .channel(channel)
                .build();
            userPreferenceRepository.save(preference);
        }
        
        return toResponse(savedUser);

    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id){

        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        return toResponse(userAccount);
        
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email){

        UserAccount userAccount = userAccountRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        return toResponse(userAccount);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request){

        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        userAccount.setDisplayName(request.displayName());
        userAccount.setPhone(request.phone());
        userAccount.setPushToken(request.pushToken());
        userAccount.setTimezone(request.timezone());

        return toResponse(userAccount);
    }

    @Transactional
    public UserResponse deactivateUser(UUID id){

        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        userAccount.setActive(false);

        return toResponse(userAccount);
    }

    private UserResponse toResponse(UserAccount userAccount) {
        return new UserResponse(
            userAccount.getId(),
            userAccount.getEmail(),
            userAccount.getPhone(),
            userAccount.getDisplayName(),
            userAccount.getTimezone(),
            userAccount.getActive(),
            userAccount.getCreatedAt(),
            userAccount.getUpdatedAt()
        );
    }
}