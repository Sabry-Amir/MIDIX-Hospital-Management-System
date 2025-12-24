package com.example.hodpital;

public class User {
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String department;
    private String password; // 1. ضفنا الباسورد هنا

    // 2. تحديث الـ Constructor لاستقبال الباسورد
    public User(String username, String fullName, String role, String email, String department, String password) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.department = department;
        this.password = password;
    }

    // Getters
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }

    // 3. دالة استدعاء الباسورد (اللي كانت عاملة Error عندك)
    public String getPassword() { return password; }

    public void getInfo()
    {
        System.out.println("first name is "+username);
        System.out.println("user's full name is "+fullName);
        System.out.println("user's role is "+role);
        System.out.println("user's email is "+email);
        System.out.println("user's department is "+department);

    }


    public void addUser() {}
    public void updateUser() {}
    public void deleteUser() {}


    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}