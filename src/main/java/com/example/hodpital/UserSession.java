package com.example.hodpital;

public class UserSession {
    private static int userId;
    private static String userName;
    private static String userRole;

    public static int getUserId() { return userId; }
    public static void setUserId(int userId) { UserSession.userId = userId; }

    public static String getUserName() { return userName; }
    public static void setUserName(String userName) { UserSession.userName = userName; }

    public static String getUserRole() { return userRole; }
    public static void setUserRole(String userRole) { UserSession.userRole = userRole; }
}