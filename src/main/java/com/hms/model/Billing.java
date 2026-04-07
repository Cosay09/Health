package com.hms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Billing {

    private final IntegerProperty billId        = new SimpleIntegerProperty();
    private final IntegerProperty patientId     = new SimpleIntegerProperty();
    private final IntegerProperty appointmentId = new SimpleIntegerProperty();
    private final StringProperty  patientName   = new SimpleStringProperty();
    private final DoubleProperty  totalAmount   = new SimpleDoubleProperty();
    private final StringProperty  status        = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public Billing(int billId, int patientId, int appointmentId,
                   String patientName, double totalAmount,
                   String status, LocalDateTime createdAt) {
        this.billId.set(billId);
        this.patientId.set(patientId);
        this.appointmentId.set(appointmentId);
        this.patientName.set(patientName);
        this.totalAmount.set(totalAmount);
        this.status.set(status);
        this.createdAt.set(createdAt);
    }

    public IntegerProperty billIdProperty()        { return billId; }
    public StringProperty  patientNameProperty()   { return patientName; }
    public IntegerProperty appointmentIdProperty() { return appointmentId; }
    public DoubleProperty  totalAmountProperty()   { return totalAmount; }
    public StringProperty  statusProperty()        { return status; }

    public int           getBillId()        { return billId.get(); }
    public int           getPatientId()     { return patientId.get(); }
    public int           getAppointmentId() { return appointmentId.get(); }
    public String        getPatientName()   { return patientName.get(); }
    public double        getTotalAmount()   { return totalAmount.get(); }
    public String        getStatus()        { return status.get(); }
    public LocalDateTime getCreatedAt()     { return createdAt.get(); }

    public void setStatus(String v)      { status.set(v); }
    public void setTotalAmount(double v) { totalAmount.set(v); }
}