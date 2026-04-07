package com.hms.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Prescription {

    private final IntegerProperty prescriptionId = new SimpleIntegerProperty();
    private final IntegerProperty appointmentId  = new SimpleIntegerProperty();
    private final IntegerProperty doctorId       = new SimpleIntegerProperty();
    private final StringProperty  doctorName     = new SimpleStringProperty();
    private final StringProperty  patientName    = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty  note           = new SimpleStringProperty();

    // The line items — not stored in this table, loaded via JOIN
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription(int prescriptionId, int appointmentId, int doctorId,
                        String doctorName, String patientName,
                        LocalDate date, String note) {
        this.prescriptionId.set(prescriptionId);
        this.appointmentId.set(appointmentId);
        this.doctorId.set(doctorId);
        this.doctorName.set(doctorName);
        this.patientName.set(patientName);
        this.date.set(date);
        this.note.set(note);
    }

    public IntegerProperty prescriptionIdProperty() { return prescriptionId; }
    public IntegerProperty appointmentIdProperty()  { return appointmentId; }
    public StringProperty  doctorNameProperty()     { return doctorName; }
    public StringProperty  patientNameProperty()    { return patientName; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty  noteProperty()           { return note; }

    public int       getPrescriptionId() { return prescriptionId.get(); }
    public int       getAppointmentId()  { return appointmentId.get(); }
    public int       getDoctorId()       { return doctorId.get(); }
    public String    getDoctorName()     { return doctorName.get(); }
    public String    getPatientName()    { return patientName.get(); }
    public LocalDate getDate()           { return date.get(); }
    public String    getNote()           { return note.get(); }

    public List<PrescriptionItem> getItems()                      { return items; }
    public void setItems(List<PrescriptionItem> items)            { this.items = items; }
    public void setNote(String v)                                 { note.set(v); }
}