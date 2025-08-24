package com.example.JournalWebApp.Repository;

import com.example.JournalWebApp.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByUsername(String userName);
    void deleteByUsername(String username);
    User findByResetToken(String resetToken);
}
