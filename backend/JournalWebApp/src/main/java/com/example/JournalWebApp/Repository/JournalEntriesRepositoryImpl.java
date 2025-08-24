package com.example.JournalWebApp.Repository;

import com.example.JournalWebApp.Entity.JournalEntries;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JournalEntriesRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<JournalEntries> searchEntriesByUserAndText(ObjectId userId, String searchText) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchText);
        Query query = TextQuery.queryText(criteria)
                .addCriteria(Criteria.where("user.$id").is(userId));
        return mongoTemplate.find(query, JournalEntries.class);
    }
}
 