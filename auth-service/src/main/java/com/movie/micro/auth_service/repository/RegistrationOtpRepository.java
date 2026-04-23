package com.movie.micro.auth_service.repository;

import com.movie.micro.auth_service.entity.RegistrationOtp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, Long> {

    Optional<RegistrationOtp> findByEmailIgnoreCase(String email);

    void deleteByEmailIgnoreCase(String email);
}
