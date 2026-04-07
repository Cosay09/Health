// LabTest.java
package com.hms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class LabTest {

    private final IntegerProperty labId         = new SimpleIntegerProperty();
    private final IntegerProperty appointmentId = new SimpleIntegerProperty();
    private final IntegerProperty patientId     = new SimpleIntegerProperty();
    private final StringProperty  patientName   = new SimpleStringProperty();
    private final StringProperty  testName      = new SimpleStringProperty();
    private final StringProperty  result        = new SimpleStringProperty();
    private final StringProperty  status        = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> orderedAt = new SimpleObjectProperty<>();

    public LabTest(int labId, int appointmentId, int patientId,
                   String patientName, String testName,
                   String result, String status, LocalDateTime orderedAt) {
        this.labId.set(labId);
        this.appointmentId.set(appointmentId);
        this.patientId.set(patientId);
        this.patientName.set(patientName);
        this.testName.set(testName);
        this.result.set(result);
        this.status.set(status);
        this.orderedAt.set(orderedAt);
    }

    public IntegerProperty labIdProperty()         { return labId; }
    public IntegerProperty appointmentIdProperty() { return appointmentId; }
    public StringProperty  patientNameProperty()   { return patientName; }
    public StringProperty  testNameProperty()      { return testName; }
    public StringProperty  resultProperty()        { return result; }
    public StringProperty  statusProperty()        { return status; }

    public int           getLabId()         { return labId.get(); }
    public int           getAppointmentId() { return appointmentId.get(); }
    public int           getPatientId()     { return patientId.get(); }
    public String        getPatientName()   { return patientName.get(); }
    public String        getTestName()      { return testName.get(); }
    public String        getResult()        { return result.get(); }
    public String        getStatus()        { return status.get(); }
    public LocalDateTime getOrderedAt()     { return orderedAt.get(); }

    public void setResult(String v) { result.set(v); }
    public void setStatus(String v) { status.set(v); }
    public void setTestName(String v) { testName.set(v); }
}