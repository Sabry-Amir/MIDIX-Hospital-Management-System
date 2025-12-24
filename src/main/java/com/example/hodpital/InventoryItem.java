package com.example.hodpital;

public class InventoryItem {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private int minQuantity;
    private String unit;
    private double price;
    private String expiryDate;
    private String supplier;

    public InventoryItem(String id, String name, String category, int quantity, int minQuantity,
                         String unit, double price, String expiryDate, String supplier) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.unit = unit;
        this.price = price;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public int getMinQuantity() { return minQuantity; }
    public String getUnit() { return unit; }
    public double getPrice() { return price; }
    public String getExpiryDate() { return expiryDate; }
    public String getSupplier() { return supplier; }

    // دالة مساعدة عشان نعرف الصنف ناقص ولا لأ
    public boolean isLowStock() {
        return quantity <= minQuantity;
    }

    public void addItem() {}
    public void updateItem() {}

    public boolean isExpired() {
        if (expiryDate == null || expiryDate.isEmpty()) return false;

        String today = java.time.LocalDate.now().toString();
        return expiryDate.compareTo(today) < 0;
    }
}