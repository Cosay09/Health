package com.hms.model;

import java.time.LocalDate;
import java.time.Period;

public class Patient {

    private int patientId;
    private String name;
    private LocalDate dateOfBirth;  // not age — remember why?
    private String gender;
    private String phone;
    private String address;

    // Constructor
    public Patient(int patientId, String name, LocalDate dateOfBirth,
                   String gender, String phone, String address) {
        this.patientId   = patientId;
        this.name        = name;
        this.dateOfBirth = dateOfBirth;
        this.gender      = gender;
        this.phone       = phone;
        this.address     = address;
    }

    // Calculated age — never stored, always fresh
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Getters and setters
    public int getPatientId()          { return patientId; }
    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }
    public LocalDate getDateOfBirth()  { return dateOfBirth; }
    public String getGender()          { return gender; }
    public String getPhone()           { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress()         { return address; }
}