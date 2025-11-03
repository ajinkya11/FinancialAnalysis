package com.financialanalysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Segment information for geographic/business segment analysis
 */
public class SegmentInformation {
    private int fiscalYear;
    private List<Segment> segments;

    public SegmentInformation() {
        this.segments = new ArrayList<>();
    }

    public SegmentInformation(int fiscalYear) {
        this.fiscalYear = fiscalYear;
        this.segments = new ArrayList<>();
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }

    public void addSegment(Segment segment) {
        this.segments.add(segment);
    }

    public Segment getSegment(String name) {
        return segments.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Represents a single business or geographic segment
     */
    public static class Segment {
        private String name; // e.g., "Domestic", "Atlantic", "Pacific", "Latin America"
        private String type; // "Geographic" or "Business"

        // Revenue
        private double passengerRevenue;
        private double cargoRevenue;
        private double otherRevenue;
        private double totalRevenue;
        private double revenuePercentOfTotal;

        // Operating Statistics
        private double availableSeatMiles; // ASMs
        private double revenuePassengerMiles; // RPMs
        private double loadFactor;
        private double passengerRevenuePerASM; // PRASM
        private double totalRevenuePerASM; // RASM
        private double yield; // passenger revenue per RPM

        // Profitability (if disclosed)
        private double operatingIncome;
        private double operatingMargin;
        private double segmentAssets;

        // Additional context
        private String notes; // Key routes, competitive dynamics, etc.

        public Segment() {}

        public Segment(String name, String type) {
            this.name = name;
            this.type = type;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public double getPassengerRevenue() { return passengerRevenue; }
        public void setPassengerRevenue(double passengerRevenue) { this.passengerRevenue = passengerRevenue; }

        public double getCargoRevenue() { return cargoRevenue; }
        public void setCargoRevenue(double cargoRevenue) { this.cargoRevenue = cargoRevenue; }

        public double getOtherRevenue() { return otherRevenue; }
        public void setOtherRevenue(double otherRevenue) { this.otherRevenue = otherRevenue; }

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

        public double getRevenuePercentOfTotal() { return revenuePercentOfTotal; }
        public void setRevenuePercentOfTotal(double revenuePercentOfTotal) { this.revenuePercentOfTotal = revenuePercentOfTotal; }

        public double getAvailableSeatMiles() { return availableSeatMiles; }
        public void setAvailableSeatMiles(double availableSeatMiles) { this.availableSeatMiles = availableSeatMiles; }

        public double getRevenuePassengerMiles() { return revenuePassengerMiles; }
        public void setRevenuePassengerMiles(double revenuePassengerMiles) { this.revenuePassengerMiles = revenuePassengerMiles; }

        public double getLoadFactor() { return loadFactor; }
        public void setLoadFactor(double loadFactor) { this.loadFactor = loadFactor; }

        public double getPassengerRevenuePerASM() { return passengerRevenuePerASM; }
        public void setPassengerRevenuePerASM(double passengerRevenuePerASM) { this.passengerRevenuePerASM = passengerRevenuePerASM; }

        public double getTotalRevenuePerASM() { return totalRevenuePerASM; }
        public void setTotalRevenuePerASM(double totalRevenuePerASM) { this.totalRevenuePerASM = totalRevenuePerASM; }

        public double getYield() { return yield; }
        public void setYield(double yield) { this.yield = yield; }

        public double getOperatingIncome() { return operatingIncome; }
        public void setOperatingIncome(double operatingIncome) { this.operatingIncome = operatingIncome; }

        public double getOperatingMargin() { return operatingMargin; }
        public void setOperatingMargin(double operatingMargin) { this.operatingMargin = operatingMargin; }

        public double getSegmentAssets() { return segmentAssets; }
        public void setSegmentAssets(double segmentAssets) { this.segmentAssets = segmentAssets; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        // Helper methods
        public void calculateDerivedMetrics() {
            if (availableSeatMiles > 0) {
                if (revenuePassengerMiles > 0) {
                    this.loadFactor = (revenuePassengerMiles / availableSeatMiles) * 100;
                }
                if (passengerRevenue > 0) {
                    this.passengerRevenuePerASM = (passengerRevenue / availableSeatMiles) * 100; // in cents
                }
                if (totalRevenue > 0) {
                    this.totalRevenuePerASM = (totalRevenue / availableSeatMiles) * 100; // in cents
                }
            }
            if (revenuePassengerMiles > 0 && passengerRevenue > 0) {
                this.yield = (passengerRevenue / revenuePassengerMiles) * 100; // in cents
            }
            if (totalRevenue > 0 && operatingIncome > 0) {
                this.operatingMargin = (operatingIncome / totalRevenue) * 100;
            }
        }
    }
}
