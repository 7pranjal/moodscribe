# Search Functionality Debugging Guide

## Issues Fixed

1. **MongoDB Text Index**: Added text index creation in `MongoConfig.java` for `title` and `content` fields
2. **Search Logic**: Improved the search method with better error handling and logging
3. **Fallback Search**: Added regex-based fallback search when text search fails
4. **Debug Endpoint**: Added `/api/journal/search/debug` endpoint for troubleshooting

## Testing Steps

### 1. Restart the Application
After the changes, restart your Spring Boot application to ensure the text index is created.

### 2. Test the Debug Endpoint
First, test the debug endpoint to see what data exists:

```bash
# Get debug info for current user
GET /api/journal/search/debug

# Test with a search query
GET /api/journal/search/debug?query=your_search_term

# Test with tags
GET /api/journal/search/debug?tags=work,personal

# Test with emotion
GET /api/journal/search/debug?emotion=happy
```

### 3. Test the Main Search Endpoint
```bash
# Text search
GET /api/journal/search?query=your_search_term

# Tag search
GET /api/journal/search?tags=work,personal

# Emotion search
GET /api/journal/search?emotion=happy

# Combined search
GET /api/journal/search?query=meeting&tags=work&emotion=happy
```

## Common Issues and Solutions

### Issue 1: Text Index Not Created
**Symptoms**: Text search returns no results even when data exists
**Solution**: Check application logs for text index creation messages

### Issue 2: Case Sensitivity
**Symptoms**: Search only works with exact case
**Solution**: The fallback regex search handles case-insensitive matching

### Issue 3: User Authentication
**Symptoms**: Search returns 401 Unauthorized
**Solution**: Ensure you're sending the JWT token in the Authorization header

### Issue 4: Empty Search Parameters
**Symptoms**: Search returns 400 Bad Request
**Solution**: Ensure at least one search parameter is provided

## Debugging Checklist

- [ ] Application restarted after changes
- [ ] JWT token is valid and included in requests
- [ ] User has journal entries in the database
- [ ] Search terms match actual content in entries
- [ ] Check application logs for error messages
- [ ] Test debug endpoint first to verify data exists

## Log Messages to Look For

Look for these log messages in your application console:

```
Text index creation note: ...
Searching for text '...' for user '...'
Text search found X results for user '...'
Using fallback regex search for text '...'
Fallback search found X results
```

## Manual Database Check

If issues persist, you can manually check your MongoDB:

```javascript
// Connect to your MongoDB and run:
use your_database_name

// Check if text index exists
db.journals_entries.getIndexes()

// Check user's entries
db.journals_entries.find({"user": ObjectId("user_id_here")})

// Test text search manually
db.journals_entries.find({$text: {$search: "your_search_term"}})
``` 