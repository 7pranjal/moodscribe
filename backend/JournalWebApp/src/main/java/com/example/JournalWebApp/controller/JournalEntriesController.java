package com.example.JournalWebApp.controller;

import com.example.JournalWebApp.Entity.JournalEntries;
import com.example.JournalWebApp.Entity.User;
import com.example.JournalWebApp.Repository.JournalEntriesRepository;
import com.example.JournalWebApp.Repository.UserRepository;
import com.example.JournalWebApp.service.EmotionAnalysisService;
import com.example.JournalWebApp.Repository.JournalEntriesRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journal")
@CrossOrigin(origins = "*", 
    allowedHeaders = {"Authorization", "Content-Type", "X-Requested-With"},
    exposedHeaders = "Authorization",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class JournalEntriesController {
    private static final Logger logger = LoggerFactory.getLogger(JournalEntriesController.class);

    @Autowired
    private JournalEntriesRepository journalEntriesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @Autowired
    private JournalEntriesRepositoryImpl journalEntriesRepositoryImpl;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Current authentication: {}", auth);
        logger.debug("Authentication principal: {}", auth.getPrincipal());
        logger.debug("Authentication authorities: {}", auth.getAuthorities());
        
        User user = userRepository.findByUsername(auth.getName());
        logger.debug("Retrieved user: {}", user != null ? user.getUsername() : "null");
        return user;
    }

    @PostMapping
    public ResponseEntity<JournalEntries> createJournalEntry(@RequestBody JournalEntries journalEntry) {
        User currentUser = getCurrentUser();
        journalEntry.setDate(LocalDate.now());
        journalEntry.setUser(currentUser);

        try {
            // Call ML model for sentiment analysis
            Map<String, Object> analysisResult = emotionAnalysisService.analyzeEmotion(journalEntry.getContent());
            
            // Extract dominant emotion
            @SuppressWarnings("unchecked")
            Map<String, Object> dominantEmotion = analysisResult.get("dominant_emotion") instanceof Map ? 
                (Map<String, Object>) analysisResult.get("dominant_emotion") : new HashMap<String, Object>();
            String emotion = (String) dominantEmotion.getOrDefault("label", "unknown");
            Double score = (Double) dominantEmotion.getOrDefault("score", 0.0);
            journalEntry.setEmotion(emotion);
            journalEntry.setScore(score);

            JournalEntries savedEntry = journalEntriesRepository.save(journalEntry);
            
            // Update user's journal entries list
            currentUser.getJournalEntries().add(savedEntry);
            userRepository.save(currentUser);
            
            return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            // If emotion analysis fails, save with default values
            journalEntry.setEmotion("unknown");
            journalEntry.setScore(0.0);
            JournalEntries savedEntry = journalEntriesRepository.save(journalEntry);
            
            // Update user's journal entries list
            currentUser.getJournalEntries().add(savedEntry);
            userRepository.save(currentUser);
            
            return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);
        }
    }

    @GetMapping
    public ResponseEntity<List<JournalEntries>> getAllJournalEntries() {
        logger.debug("Getting all journal entries");
        User currentUser = getCurrentUser();
        logger.debug("Current user: {}", currentUser != null ? currentUser.getUsername() : "null");
        
        if (currentUser == null) {
            logger.error("No authenticated user found");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        List<JournalEntries> entries = journalEntriesRepository.findByUserId(currentUser.getId());
        logger.debug("Found {} entries for user {}", entries.size(), currentUser.getUsername());
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable String id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.FORBIDDEN);
        }
        try {
            ObjectId objectId = new ObjectId(id);
            return journalEntriesRepository.findById(objectId)
                    .map(entry -> {
                        if (!entry.getUser().getId().equals(currentUser.getId())) {
                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                        }
                        return new ResponseEntity<>(entry, HttpStatus.OK);
                    })
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid ID format", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJournalEntryById(@PathVariable String id, @RequestBody JournalEntries journalEntry) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.FORBIDDEN);
        }
        try {
            ObjectId objectId = new ObjectId(id);
            return journalEntriesRepository.findById(objectId)
                    .map(existingEntry -> {
                        if (!existingEntry.getUser().getId().equals(currentUser.getId())) {
                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                        }
                        existingEntry.setTitle(journalEntry.getTitle());
                        existingEntry.setContent(journalEntry.getContent());
                        existingEntry.setTags(journalEntry.getTags());
                        try {
                            Map<String, Object> analysisResult = emotionAnalysisService.analyzeEmotion(journalEntry.getContent());
                            @SuppressWarnings("unchecked")
                            Map<String, Object> dominantEmotion = analysisResult.get("dominant_emotion") instanceof Map ? 
                                (Map<String, Object>) analysisResult.get("dominant_emotion") : new HashMap<String, Object>();
                            String emotion = (String) dominantEmotion.getOrDefault("label", "unknown");
                            Double score = (Double) dominantEmotion.getOrDefault("score", 0.0);
                            existingEntry.setEmotion(emotion);
                            existingEntry.setScore(score);
                        } catch (Exception e) {
                            if (existingEntry.getEmotion() == null) {
                                existingEntry.setEmotion("unknown");
                                existingEntry.setScore(0.0);
                            }
                        }
                        JournalEntries updatedEntry = journalEntriesRepository.save(existingEntry);
                        int index = currentUser.getJournalEntries().indexOf(existingEntry);
                        if (index != -1) {
                            currentUser.getJournalEntries().set(index, updatedEntry);
                            userRepository.save(currentUser);
                        }
                        return new ResponseEntity<>(updatedEntry, HttpStatus.OK);
                    })
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid ID format", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable String id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.FORBIDDEN);
        }
        try {
            ObjectId objectId = new ObjectId(id);
            return journalEntriesRepository.findById(objectId)
                    .map(entry -> {
                        if (!entry.getUser().getId().equals(currentUser.getId())) {
                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                        }
                        currentUser.getJournalEntries().removeIf(e -> e.getId().equals(entry.getId().toString()));
                        userRepository.save(currentUser);
                        journalEntriesRepository.delete(entry);
                        logger.debug("Successfully deleted journal entry {} for user {}", id, currentUser.getUsername());
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    })
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting journal entry: {}", e.getMessage());
            return new ResponseEntity<>("Invalid ID format", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/analytics/calendar")
    public ResponseEntity<Map<String, String>> getCalendarEmotions() {
        User currentUser = getCurrentUser();
        List<JournalEntries> entries = journalEntriesRepository.findByUserId(currentUser.getId());
        
        Map<String, String> dateEmotions = entries.stream()
            .collect(Collectors.toMap(
                entry -> entry.getDate().toString(),
                JournalEntries::getEmotion
            ));
        
        return new ResponseEntity<>(dateEmotions, HttpStatus.OK);
    }

    @GetMapping("/analytics/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Integer>> getMonthlyEmotionStats(
            @PathVariable int year,
            @PathVariable int month) {
        User currentUser = getCurrentUser();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        
        List<JournalEntries> entries = journalEntriesRepository.findByUserIdAndDateBetween(
            currentUser.getId(), startDate, endDate);
        
        Map<String, Integer> emotionCounts = entries.stream()
            .collect(Collectors.groupingBy(
                JournalEntries::getEmotion,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        
        return new ResponseEntity<>(emotionCounts, HttpStatus.OK);
    }

    //check this endpoint!

    @GetMapping("/search")
    public ResponseEntity<List<JournalEntries>> searchEntries(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String emotion) {
    
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    
        if (query == null && tags == null && emotion == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    
        ObjectId userId = currentUser.getId();
        List<JournalEntries> results;
    
        if (query != null && tags != null && emotion != null) {
            results = journalEntriesRepository.advancedSearch(userId, query, tags, emotion);
        } else if (query != null) {
            results = journalEntriesRepositoryImpl.searchEntriesByUserAndText(userId, query);
        } else if (tags != null && !tags.isEmpty()) {
            results = journalEntriesRepository.findByUserIdAndTagsContaining(userId, tags.get(0));
        } else {
            results = journalEntriesRepository.findByUserIdAndEmotion(userId, emotion);
        }
    
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    @PutMapping("/{id}/tags")
    public ResponseEntity<?> updateTagsById(@PathVariable String id, @RequestBody Set<String> tags) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.FORBIDDEN);
        }
        try {
            ObjectId objectId = new ObjectId(id);
            return journalEntriesRepository.findById(objectId)
                    .map(entry -> {
                        if (!entry.getUser().getId().equals(currentUser.getId())) {
                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                        }
                        entry.setTags(tags);
                        return new ResponseEntity<>(journalEntriesRepository.save(entry), HttpStatus.OK);
                    })
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid ID format", HttpStatus.BAD_REQUEST);
        }
    }
} 