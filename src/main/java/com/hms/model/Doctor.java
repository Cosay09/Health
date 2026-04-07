package com.hms.model;

import javafx.beans.property.*;

public class Doctor {

    private final IntegerProperty doctorId       = new SimpleIntegerProperty();
    private final StringProperty  name           = new SimpleStringProperty();
    private final StringProperty  specialization = new SimpleStringProperty();
    private final StringProperty  phone          = new SimpleStringProperty();
    private final BooleanProperty available      = new SimpleBooleanProperty();

    public Doctor(int doctorId, String name, String specialization,
                  String phone, boolean available) {
        this.doctorId.set(doctorId);
        this.name.set(name);
        this.specialization.set(specialization);
        this.phone.set(phone);
        this.available.set(available);
    }

    // Property getters — needed by TableView
    public IntegerProperty doctorIdProperty()       { return doctorId; }
    public StringProperty  nameProperty()           { return name; }
    public StringProperty  specializationProperty() { return specialization; }
    public StringProperty  phoneProperty()          { return phone; }
    public BooleanProperty availableProperty()      { return available; }

    // Regular getters
    public int     getDoctorId()       { return doctorId.get(); }
    public String  getName()           { return name.get(); }
    public String  getSpecialization() { return specialization.get(); }
    public String  getPhone()          { return phone.get(); }
    public boolean isAvailable()       { return available.get(); }

    // Setters
    public void setName(String v)           { name.set(v); }
    public void setSpecialization(String v) { specialization.set(v); }
    public void setPhone(String v)          { phone.set(v); }
    public void setAvailable(boolean v)     { available.set(v); }
}