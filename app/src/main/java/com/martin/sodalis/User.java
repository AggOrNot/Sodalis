package com.martin.sodalis;

/**
 * class that holds basic user data and is used when first creating the user's node in db
 */

public class User {

    public String userId;
    public String email;
    public String userName;
    public String bday;
    public String companionName;
    public int relationshipRating;

    public User() {
    }

    public User(String userId, String email, String userName, String bday, String companionName,
                int relationshipRating) {

        this.userId = userId;
        this.email = email;
        this.userName = userName;
        this.bday = bday;
        this.companionName = companionName;
        this.relationshipRating = relationshipRating;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getBday() {
        return bday;
    }

    public String getCompanionName() {
        return companionName;
    }

    public int getRelationshipRating() {
        return relationshipRating;
    }
}
