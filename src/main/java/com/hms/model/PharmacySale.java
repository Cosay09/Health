package com.hms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PharmacySale {

    private final IntegerProperty saleId         = new SimpleIntegerProperty();
    private final IntegerProperty patientId      = new SimpleIntegerProperty();
    private final IntegerProperty appointmentId  = new SimpleIntegerProperty();
    private final IntegerProperty prescriptionId = new SimpleIntegerProperty();
    private final IntegerProperty soldBy         = new SimpleIntegerProperty();
    private final DoubleProperty  totalAmount    = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> saleDate = new SimpleObjectProperty<>();

    // Display fields — populated via JOIN
    private final StringProperty patientName = new SimpleStringProperty();

    private List<PharmacySaleItem> items = new ArrayList<>();

    public PharmacySale(int saleId, int patientId, int appointmentId,
                        int prescriptionId, int soldBy,
                        double totalAmount, LocalDateTime saleDate,
                        String patientName) {
        this.saleId.set(saleId);
        this.patientId.set(patientId);
        this.appointmentId.set(appointmentId);
        this.prescriptionId.set(prescriptionId);
        this.soldBy.set(soldBy);
        this.totalAmount.set(totalAmount);
        this.saleDate.set(saleDate);
        this.patientName.set(patientName);
    }

    public IntegerProperty saleIdProperty()         { return saleId; }
    public StringProperty  patientNameProperty()    { return patientName; }
    public DoubleProperty  totalAmountProperty()    { return totalAmount; }

    public int           getSaleId()         { return saleId.get(); }
    public int           getPatientId()      { return patientId.get(); }
    public int           getAppointmentId()  { return appointmentId.get(); }
    public int           getPrescriptionId() { return prescriptionId.get(); }
    public int           getSoldBy()         { return soldBy.get(); }
    public double        getTotalAmount()    { return totalAmount.get(); }
    public LocalDateTime getSaleDate()       { return saleDate.get(); }
    public String        getPatientName()    { return patientName.get(); }

    public List<PharmacySaleItem> getItems()                   { return items; }
    public void setItems(List<PharmacySaleItem> items)         { this.items = items; }
    public void setTotalAmount(double v)                       { totalAmount.set(v); }
}