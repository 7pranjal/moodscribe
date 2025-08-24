package com.example.JournalWebApp.controller;

import com.example.JournalWebApp.Entity.User;
import com.example.JournalWebApp.Repository.UserRepository;
import com.example.JournalWebApp.security.JwtUtil;
import com.example.JournalWebApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); //?

    @Autowired
    private UserRepository userRepository; //for user data

    @Autowired
    private JwtUtil jwtUtil; //for token generation

    @Autowired
    private PasswordEncoder passwordEncoder; //for password encoding

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        logger.info("Signup request received for username: {}", user.getUsername());
        
        if (userRepository.findByUsername(user.getUsername()) != null) {
            logger.warn("Username already taken: {}", user.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("message", "This username is already taken. Please choose a different username.");
            response.put("status", "USERNAME_TAKEN");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Set default role as USER
        List<String> defaultRoles = new ArrayList<>();
        defaultRoles.add("USER");
        user.setRoles(defaultRoles);
        //Set password after encoding 
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // No need to set email
        User savedUser = userRepository.save(user);
        logger.info("User successfully created with role USER: {}", savedUser.getUsername());

        String token = jwtUtil.generateToken(savedUser.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", savedUser.getUsername());
        response.put("roles", savedUser.getRoles());
        response.put("status", "SUCCESS");
        response.put("message", "Registration successful! Welcome " + savedUser.getUsername() + "!");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        logger.info("Login attempt for username: {}", user.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            
            if (authentication.isAuthenticated()) {
                User existingUser = userRepository.findByUsername(user.getUsername());
                String token = jwtUtil.generateToken(existingUser.getUsername());
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("username", existingUser.getUsername());
                response.put("status", "SUCCESS");
                response.put("message", "Login successful!");
                
                logger.info("Login successful for username: {}", user.getUsername());
                return ResponseEntity.ok(response);
            }
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for username: {} - Bad credentials", user.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password", "status", "INVALID_CREDENTIALS"));
        } catch (Exception e) {
            logger.error("Login error for username: {} - {}", user.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An error occurred during login", "status", "ERROR"));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Authentication failed", "status", "AUTH_FAILED"));
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication(); //information for the current thread
            String currentUsername = auth.getName();

            // Only allow users to delete their own account or require admin role
            if (!currentUsername.equals(username) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return new ResponseEntity<>(
                    Map.of("message", "You are not authorized to delete this user", 
                          "status", "FORBIDDEN"), 
                    HttpStatus.FORBIDDEN);
            }

            userService.deleteUser(username);
            return new ResponseEntity<>(
                Map.of("message", "User and all associated journal entries deleted successfully",
                      "status", "SUCCESS"),
                HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", username, e.getMessage());
            return new ResponseEntity<>(
                Map.of("message", "Error deleting user", 
                      "status", "ERROR"),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 