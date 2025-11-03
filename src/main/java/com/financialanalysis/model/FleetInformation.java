package com.financialanalysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Fleet information for airline analysis
 */
public class FleetInformation {
    private int fiscalYear;
    private List<AircraftType> fleet;
    private List<AircraftOrder> orders;

    // Summary metrics
    private int totalAircraft;
    private int ownedAircraft;
    private int financeLeasedAircraft;
    private int operatingLeasedAircraft;
    private double averageFleetAge;

    public FleetInformation() {
        this.fleet = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    public FleetInformation(int fiscalYear) {
        this.fiscalYear = fiscalYear;
        this.fleet = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public List<AircraftType> getFleet() { return fleet; }
    public void setFleet(List<AircraftType> fleet) { this.fleet = fleet; }

    public List<AircraftOrder> getOrders() { return orders; }
    public void setOrders(List<AircraftOrder> orders) { this.orders = orders; }

    public int getTotalAircraft() { return totalAircraft; }
    public void setTotalAircraft(int totalAircraft) { this.totalAircraft = totalAircraft; }

    public int getOwnedAircraft() { return ownedAircraft; }
    public void setOwnedAircraft(int ownedAircraft) { this.ownedAircraft = ownedAircraft; }

    public int getFinanceLeasedAircraft() { return financeLeasedAircraft; }
    public void setFinanceLeasedAircraft(int financeLeasedAircraft) { this.financeLeasedAircraft = financeLeasedAircraft; }

    public int getOperatingLeasedAircraft() { return operatingLeasedAircraft; }
    public void setOperatingLeasedAircraft(int operatingLeasedAircraft) { this.operatingLeasedAircraft = operatingLeasedAircraft; }

    public double getAverageFleetAge() { return averageFleetAge; }
    public void setAverageFleetAge(double averageFleetAge) { this.averageFleetAge = averageFleetAge; }

    // Helper methods
    public void addAircraft(AircraftType aircraft) {
        this.fleet.add(aircraft);
        recalculateSummary();
    }

    public void addOrder(AircraftOrder order) {
        this.orders.add(order);
    }

    private void recalculateSummary() {
        totalAircraft = fleet.stream().mapToInt(AircraftType::getTotalCount).sum();
        ownedAircraft = fleet.stream().mapToInt(AircraftType::getOwned).sum();
        financeLeasedAircraft = fleet.stream().mapToInt(AircraftType::getFinanceLeased).sum();
        operatingLeasedAircraft = fleet.stream().mapToInt(AircraftType::getOperatingLeased).sum();
    }

    /**
     * Represents a specific aircraft type in the fleet
     */
    public static class AircraftType {
        private String type; // e.g., "Boeing 737-800", "Airbus A320"
        private String manufacturer;
        private String model;
        private int owned;
        private int financeLeased;
        private int operatingLeased;
        private int totalCount;
        private double averageAge;

        // Seating configuration
        private int firstClassSeats;
        private int businessClassSeats;
        private int premiumEconomySeats;
        private int economySeats;
        private int totalSeats;

        private double averageStageLength; // miles

        public AircraftType() {}

        public AircraftType(String type) {
            this.type = type;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public int getOwned() { return owned; }
        public void setOwned(int owned) {
            this.owned = owned;
            this.totalCount = owned + financeLeased + operatingLeased;
        }

        public int getFinanceLeased() { return financeLeased; }
        public void setFinanceLeased(int financeLeased) {
            this.financeLeased = financeLeased;
            this.totalCount = owned + financeLeased + operatingLeased;
        }

        public int getOperatingLeased() { return operatingLeased; }
        public void setOperatingLeased(int operatingLeased) {
            this.operatingLeased = operatingLeased;
            this.totalCount = owned + financeLeased + operatingLeased;
        }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public double getAverageAge() { return averageAge; }
        public void setAverageAge(double averageAge) { this.averageAge = averageAge; }

        public int getFirstClassSeats() { return firstClassSeats; }
        public void setFirstClassSeats(int firstClassSeats) { this.firstClassSeats = firstClassSeats; }

        public int getBusinessClassSeats() { return businessClassSeats; }
        public void setBusinessClassSeats(int businessClassSeats) { this.businessClassSeats = businessClassSeats; }

        public int getPremiumEconomySeats() { return premiumEconomySeats; }
        public void setPremiumEconomySeats(int premiumEconomySeats) { this.premiumEconomySeats = premiumEconomySeats; }

        public int getEconomySeats() { return economySeats; }
        public void setEconomySeats(int economySeats) { this.economySeats = economySeats; }

        public int getTotalSeats() { return totalSeats; }
        public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

        public double getAverageStageLength() { return averageStageLength; }
        public void setAverageStageLength(double averageStageLength) { this.averageStageLength = averageStageLength; }
    }

    /**
     * Represents aircraft on order
     */
    public static class AircraftOrder {
        private String type;
        private int quantity;
        private int deliveryYear;
        private double commitmentAmount; // in millions
        private String orderType; // "Firm", "Option", "Purchase Right"

        public AircraftOrder() {}

        public AircraftOrder(String type, int quantity, int deliveryYear) {
            this.type = type;
            this.quantity = quantity;
            this.deliveryYear = deliveryYear;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public int getDeliveryYear() { return deliveryYear; }
        public void setDeliveryYear(int deliveryYear) { this.deliveryYear = deliveryYear; }

        public double getCommitmentAmount() { return commitmentAmount; }
        public void setCommitmentAmount(double commitmentAmount) { this.commitmentAmount = commitmentAmount; }

        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
    }
}
