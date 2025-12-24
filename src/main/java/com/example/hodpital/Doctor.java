package com.example.hodpital;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    private String department;

    public Doctor(int id, String name, String specialization, String phone, String email, String department) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.department = department;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }

}