package com.movie.micro.user_service.controller;

import com.movie.micro.user_service.dto.InternalUserProfileRequest;
import com.movie.micro.user_service.dto.UserProfileRequest;
import com.movie.micro.user_service.dto.UserProfileResponse;
import com.movie.micro.user_service.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;
    private final String internalApiKey;

    public UserController(UserProfileService userProfileService,
                          @Value("${app.internal-api-key}") String internalApiKey) {
        this.userProfileService = userProfileService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse create(@Valid @RequestBody UserProfileRequest request) {
        return userProfileService.create(request);
    }

    @PostMapping("/internal/profiles")
    public UserProfileResponse upsertInternal(
            @RequestHeader("X-Internal-Api-Key") String requestApiKey,
            @Valid @RequestBody InternalUserProfileRequest request
    ) {
        validateInternalApiKey(requestApiKey);
        return userProfileService.upsertInternal(request);
    }

    @GetMapping("/{id}")
    public UserProfileResponse getById(@PathVariable Long id) {
        return userProfileService.getById(id);
    }

    @GetMapping
    public ResponseEntity<?> getUsers(@RequestParam(required = false) Long accountId) {
        if (accountId != null) {
            return ResponseEntity.ok(userProfileService.getByAccountId(accountId));
        }
        List<UserProfileResponse> users = userProfileService.getAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public UserProfileResponse update(@PathVariable Long id, @Valid @RequestBody UserProfileRequest request) {
        return userProfileService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userProfileService.delete(id);
    }

    private void validateInternalApiKey(String requestApiKey) {
        if (!StringUtils.hasText(requestApiKey) || !requestApiKey.equals(internalApiKey)) {
            throw new IllegalArgumentException("Invalid internal api key");
        }
    }
}
