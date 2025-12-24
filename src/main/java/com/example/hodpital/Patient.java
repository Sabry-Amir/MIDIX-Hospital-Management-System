package com.example.hodpital;

public class Patient {
    // التعديل هنا: ID بقى String
    private String id;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String bloodType;
    private String status;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String allergies;
    private String dob;

    public Patient(String id, String name, int age, String gender, String phone, String bloodType, String status, String address, String emergencyContact, String emergencyPhone, String allergies, String dob) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.bloodType = bloodType;
        this.status = status;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.allergies = allergies;
        this.dob = dob;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getBloodType() { return bloodType; }
    public String getStatus() { return status; }
    public String getAddress() { return address; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public String getAllergies() { return allergies; }
    public String getDob() { return dob; }

    public void displayInfo() {
        System.out.println("Name " + name);
        System.out.println("Age " + age);
        System.out.println("Phone " + phone);
        System.out.println("status: " +status );
        System.out.println("blood type is: " +bloodType );
        System.out.println("allergies: " +allergies );
    }


    public void admit() {
        status = "admitted";
    }

    public void discharge() {
        status = "discharged";
    }
}