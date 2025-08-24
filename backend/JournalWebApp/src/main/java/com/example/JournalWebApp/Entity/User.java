package com.example.JournalWebApp.Entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @DBRef
    private List<JournalEntries> journalEntries = new ArrayList<>();

    private List<String> roles = new ArrayList<>();

    // Constructor with required fields
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
