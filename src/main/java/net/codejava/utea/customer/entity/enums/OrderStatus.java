package net.codejava.utea.customer.entity.enums;

public enum OrderStatus {
    NEW,
    PENDING,
    CONFIRMED,
    PAID,
    SHIPPED,
    COMPLETED,
    CANCELLED;

    public static boolean isValid(String s) {
        if (s == null) return false;
        try { valueOf(s.toUpperCase()); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }
}