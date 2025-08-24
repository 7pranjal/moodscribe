package com.example.JournalWebApp.Repository;

import com.example.JournalWebApp.Entity.JournalEntries;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface JournalEntriesRepository extends MongoRepository<JournalEntries, ObjectId> {

    List<JournalEntries> findByUserId(ObjectId userId);

    List<JournalEntries> findByUserIdAndDateBetween(ObjectId userId, LocalDate startDate, LocalDate endDate);

    @Query("{ '$and': [ { 'user.$id': ?0 }, { '$text': { '$search': ?1 } } ] }")
    List<JournalEntries> searchEntries(ObjectId userId, String searchText);

    List<JournalEntries> findByUserIdAndTagsContaining(ObjectId userId, String tag);

    List<JournalEntries> findByUserIdAndEmotion(ObjectId userId, String emotion);

    @Query("{ '$and': [ " +
           "{ 'user.$id': ?0 }, " +
           "{ '$text': { '$search': ?1 } }, " +
           "{ 'tags': { '$in': ?2 } }, " +
           "{ 'emotion': ?3 } " +
           "] }")
    List<JournalEntries> advancedSearch(ObjectId userId, String searchText, List<String> tags, String emotion);
}
