package com.example.JournalWebApp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "journals_entries")
@Data
@NoArgsConstructor
public class JournalEntries {
    @Id
    private ObjectId id;
    
    @TextIndexed
    private String title;
    
    @TextIndexed
    private String content;
    
    private LocalDate date;
    private String emotion;
    private double score;
    private Set<String> tags = new HashSet<>();

    @JsonIgnore
    @DBRef
    private User user;

    // Custom getter to return id as string
    public String getId() {
        return id != null ? id.toString() : null;
    }
}
