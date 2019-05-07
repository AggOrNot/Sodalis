package com.martin.sodalis;

/**
 * Created by Martin on 3/8/18.
 */

public class UserReplies {

    public String userReplyA;
    public String userReplyB;
    public String userReplyC;
    public String userReplyD;

    public UserReplies() {
    }

    public UserReplies(String userReplyA, String userReplyB, String userReplyC, String userReplyD) {

        this.userReplyA = userReplyA;
        this.userReplyB = userReplyB;
        this.userReplyC = userReplyC;
        this.userReplyD = userReplyD;
    }

    public String getUserReplyA() {
        return userReplyA;
    }

    public String getUserReplyB() {
        return userReplyB;
    }

    public String getUserReplyC() {
        return userReplyC;
    }

    public String getUserReplyD() {
        return userReplyD;
    }
}