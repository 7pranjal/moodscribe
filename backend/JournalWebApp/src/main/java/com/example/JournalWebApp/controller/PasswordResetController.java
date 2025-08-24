package com.example.JournalWebApp.controller;

import com.example.JournalWebApp.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "http://localhost:5173")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/reset-request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        String token = passwordResetService.initiatePasswordReset(username);
        
        Map<String, String> response = new HashMap<>();
        if (token != null) {
            response.put("message", "Reset token generated successfully");
            response.put("token", token);
            response.put("resetUrl", "http://localhost:5173/reset-password?token=" + token);
        } else {
            response.put("message", "Failed to generate reset token. Username not found.");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token and new password are required"));
        }
        
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-direct")
    public ResponseEntity<?> resetPasswordDirect(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");
        
        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Username and new password are required"
            ));
        }

        boolean success = passwordResetService.resetPasswordDirectly(username, newPassword);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to reset password. Username not found."
            ));
        }
    }
} 