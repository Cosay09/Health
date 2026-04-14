package com.hms.model;

public class PharmacySaleItem {

    private int    itemId;
    private int    saleId;
    private int    medicineId;
    private String medicineName;
    private int    quantity;
    private double unitPrice;

    public PharmacySaleItem(int itemId, int saleId, int medicineId,
                            String medicineName, int quantity, double unitPrice)
    {
        this.itemId       = itemId;
        this.saleId       = saleId;
        this.medicineId   = medicineId;
        this.medicineName = medicineName;
        this.quantity     = quantity;
        this.unitPrice    = unitPrice;
    }

    public int    getItemId()       { return itemId; }
    public int    getSaleId()       { return saleId; }
    public int    getMedicineId()   { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public int    getQuantity()     { return quantity; }
    public double getUnitPrice()    { return unitPrice; }
    public double getSubtotal()     { return quantity * unitPrice; }

    public void setQuantity(int v)      { quantity = v; }
    public void setMedicineName(String v) { medicineName = v; }
}