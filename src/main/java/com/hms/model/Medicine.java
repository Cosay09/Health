package com.hms.model;

import javafx.beans.property.*;

public class Medicine {

    private final IntegerProperty medicineId = new SimpleIntegerProperty();
    private final StringProperty  name       = new SimpleStringProperty();
    private final IntegerProperty stock      = new SimpleIntegerProperty();
    private final StringProperty  unit       = new SimpleStringProperty();
    private final DoubleProperty  price      = new SimpleDoubleProperty();

    public Medicine(int medicineId, String name, int stock,
                    String unit, double price) {
        this.medicineId.set(medicineId);
        this.name.set(name);
        this.stock.set(stock);
        this.unit.set(unit);
        this.price.set(price);
    }

    public IntegerProperty medicineIdProperty() { return medicineId; }
    public StringProperty  nameProperty()       { return name; }
    public IntegerProperty stockProperty()      { return stock; }
    public StringProperty  unitProperty()       { return unit; }
    public DoubleProperty  priceProperty()      { return price; }

    public int    getMedicineId() { return medicineId.get(); }
    public String getName()       { return name.get(); }
    public int    getStock()      { return stock.get(); }
    public String getUnit()       { return unit.get(); }
    public double getPrice()      { return price.get(); }

    public void setName(String v)   { name.set(v); }
    public void setStock(int v)     { stock.set(v); }
    public void setUnit(String v)   { unit.set(v); }
    public void setPrice(double v)  { price.set(v); }

    // Used by ComboBox in prescription form
    @Override
    public String toString() {
        return name.get() + " (" + unit.get() + ")";
    }
}