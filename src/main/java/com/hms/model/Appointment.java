package com.hms.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private final IntegerProperty appointmentId   = new SimpleIntegerProperty();
    private final IntegerProperty patientId       = new SimpleIntegerProperty();
    private final IntegerProperty doctorId        = new SimpleIntegerProperty();
    private final StringProperty  patientName     = new SimpleStringProperty();
    private final StringProperty  doctorName      = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date  = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> time  = new SimpleObjectProperty<>();
    private final StringProperty  status          = new SimpleStringProperty();
    private final StringProperty conditionNote    = new SimpleStringProperty();

    public Appointment(int appointmentId, int patientId, int doctorId,
                       String patientName, String doctorName,
                       LocalDate date, LocalTime time, String conditionNote, String status) {
        this.appointmentId.set(appointmentId);
        this.patientId.set(patientId);
        this.doctorId.set(doctorId);
        this.patientName.set(patientName);
        this.doctorName.set(doctorName);
        this.date.set(date);
        this.time.set(time);
        this.conditionNote.set(conditionNote);
        this.status.set(status);
    }

    // Property getters
    public IntegerProperty appointmentIdProperty() { return appointmentId; }
    public StringProperty  patientNameProperty()   { return patientName; }
    public StringProperty  doctorNameProperty()    { return doctorName; }
    public ObjectProperty<LocalDate> dateProperty(){ return date; }
    public ObjectProperty<LocalTime> timeProperty(){ return time; }
    public StringProperty  statusProperty()        { return status; }
    public StringProperty conditionNoteProperty()  { return conditionNote; }

    // Regular getters
    public int       getAppointmentId() { return appointmentId.get(); }
    public int       getPatientId()     { return patientId.get(); }
    public int       getDoctorId()      { return doctorId.get(); }
    public String    getPatientName()   { return patientName.get(); }
    public String    getDoctorName()    { return doctorName.get(); }
    public LocalDate getDate()          { return date.get(); }
    public LocalTime getTime()          { return time.get(); }
    public String    getStatus()        { return status.get(); }
    public String getConditionNote() { return conditionNote.get(); }

    // Setters
    public void setStatus(String v)        { status.set(v); }
    public void setDate(LocalDate v)       { date.set(v); }
    public void setTime(LocalTime v)       { time.set(v); }
    public void setPatientId(int v)        { patientId.set(v); }
    public void setDoctorId(int v)         { doctorId.set(v); }
    public void setPatientName(String v)   { patientName.set(v); }
    public void setDoctorName(String v)    { doctorName.set(v); }
    public void setConditionNote(String v) { conditionNote.set(v); }
}