package com.example.JournalWebApp.service;

import com.example.JournalWebApp.Entity.User;
import com.example.JournalWebApp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String initiatePasswordReset(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours
            userRepository.save(user);
            
            logger.info("Password reset token generated for user: {}", username);
            return token;
        }
        logger.warn("No user found with username: {}", username);
        return null;
    }

    public boolean validateResetToken(String token) {
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token);
        if (user != null && user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            logger.info("Password successfully reset for user: {}", user.getUsername());
        } else {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
    }

    public boolean resetPasswordDirectly(String username, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Password directly reset for user: {}", username);
            return true;
        }
        logger.warn("No user found with username: {}", username);
        return false;
    }
} 