package com.movie.micro.user_service.service;

import com.movie.micro.user_service.dto.InternalUserProfileRequest;
import com.movie.micro.user_service.dto.UserProfileRequest;
import com.movie.micro.user_service.dto.UserProfileResponse;
import com.movie.micro.user_service.entity.UserProfile;
import com.movie.micro.user_service.repository.UserProfileRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileResponse create(UserProfileRequest request) {
        if (userProfileRepository.findByAccountId(request.accountId()).isPresent()) {
            throw new IllegalArgumentException("accountId already exists");
        }
        if (userProfileRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("email already exists");
        }

        UserProfile profile = new UserProfile();
        profile.setAccountId(request.accountId());
        profile.setEmail(normalizeEmail(request.email()));
        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        profile.setDateOfBirth(request.dateOfBirth());

        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public UserProfileResponse upsertInternal(InternalUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByAccountId(request.accountId()).orElseGet(UserProfile::new);
        if (profile.getId() != null && userProfileRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), profile.getId())) {
            throw new IllegalArgumentException("email already exists");
        }

        profile.setAccountId(request.accountId());
        profile.setEmail(normalizeEmail(request.email()));
        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        profile.setDateOfBirth(request.dateOfBirth());

        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getById(Long id) {
        return toResponse(userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getByAccountId(Long accountId) {
        return toResponse(userProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAll() {
        return userProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserProfileResponse update(Long id, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!profile.getAccountId().equals(request.accountId())
                && userProfileRepository.findByAccountId(request.accountId()).isPresent()) {
            throw new IllegalArgumentException("accountId already exists");
        }

        if (userProfileRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new IllegalArgumentException("email already exists");
        }

        profile.setAccountId(request.accountId());
        profile.setEmail(normalizeEmail(request.email()));
        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        profile.setDateOfBirth(request.dateOfBirth());

        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public void delete(Long id) {
        if (!userProfileRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userProfileRepository.deleteById(id);
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAccountId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getDateOfBirth(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
