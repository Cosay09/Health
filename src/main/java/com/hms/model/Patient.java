package com.hms.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.Period;

public class Patient {

    private final IntegerProperty patientId   = new SimpleIntegerProperty();
    private final StringProperty  name        = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty  gender      = new SimpleStringProperty();
    private final StringProperty  phone       = new SimpleStringProperty();
    private final StringProperty  address     = new SimpleStringProperty();

    public Patient(int patientId, String name, LocalDate dateOfBirth,
                   String gender, String phone, String address) {
        this.patientId.set(patientId);
        this.name.set(name);
        this.dateOfBirth.set(dateOfBirth);
        this.gender.set(gender);
        this.phone.set(phone);
        this.address.set(address);
    }

    // Age is always calculated fresh — never stored
    public int getAge() {
        return Period.between(dateOfBirth.get(), LocalDate.now()).getYears();
    }

    // Property getters — TableView needs these
    public IntegerProperty patientIdProperty()             { return patientId; }
    public StringProperty  nameProperty()                  { return name; }
    public ObjectProperty<LocalDate> dateOfBirthProperty() { return dateOfBirth; }
    public StringProperty  genderProperty()                { return gender; }
    public StringProperty  phoneProperty()                 { return phone; }
    public StringProperty  addressProperty()               { return address; }

    // Regular getters — DAO and other code needs these
    public int       getPatientId()   { return patientId.get(); }
    public String    getName()        { return name.get(); }
    public LocalDate getDateOfBirth() { return dateOfBirth.get(); }
    public String    getGender()      { return gender.get(); }
    public String    getPhone()       { return phone.get(); }
    public String    getAddress()     { return address.get(); }

    // Setters
    public void setName(String v)        { name.set(v); }
    public void setGender(String v)      { gender.set(v); }
    public void setPhone(String v)       { phone.set(v); }
    public void setAddress(String v)     { address.set(v); }
    public void setDateOfBirth(LocalDate v) { dateOfBirth.set(v); }
}