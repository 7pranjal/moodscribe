package com.example.JournalWebApp.service;

import com.example.JournalWebApp.Entity.User;
import com.example.JournalWebApp.Entity.JournalEntries;
import com.example.JournalWebApp.Repository.UserRepository;
import com.example.JournalWebApp.Repository.JournalEntriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.types.ObjectId;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JournalEntriesRepository journalEntriesRepository;

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            // Delete all journal entries for this user
            for (JournalEntries entry : user.getJournalEntries()) {
                if (entry.getId() != null) {
                    journalEntriesRepository.deleteById(new ObjectId(entry.getId()));
                }
            }
            
            // Delete the user
            userRepository.delete(user);
            logger.info("Successfully deleted user {} and all their journal entries", username);
        } else {
            logger.warn("Attempted to delete non-existent user: {}", username);
        }
    }
} 