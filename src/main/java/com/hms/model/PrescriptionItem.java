// PrescriptionItem.java — one row in the junction table
package com.hms.model;

public class PrescriptionItem {

    private int    medicineId;
    private String medicineName;
    private String dosage;
    private int    quantity;

    public PrescriptionItem(int medicineId, String medicineName,
                            String dosage, int quantity) {
        this.medicineId   = medicineId;
        this.medicineName = medicineName;
        this.dosage       = dosage;
        this.quantity     = quantity;
    }

    public int    getMedicineId()   { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public String getDosage()       { return dosage; }
    public int    getQuantity()     { return quantity; }

    public void setDosage(String v)   { dosage = v; }
    public void setQuantity(int v)    { quantity = v; }
    public void setMedicineName(String v) { medicineName = v; }
}