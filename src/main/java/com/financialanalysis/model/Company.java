package com.financialanalysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a company with its financial statements from multiple years
 */
public class Company {
    private String name;
    private String ticker;
    private String industry;
    private List<FinancialStatement> financialStatements;

    public Company() {
        this.financialStatements = new ArrayList<>();
    }

    public Company(String name, String ticker, String industry) {
        this.name = name;
        this.ticker = ticker;
        this.industry = industry;
        this.financialStatements = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public List<FinancialStatement> getFinancialStatements() {
        return financialStatements;
    }

    public void setFinancialStatements(List<FinancialStatement> financialStatements) {
        this.financialStatements = financialStatements;
    }

    public void addFinancialStatement(FinancialStatement statement) {
        this.financialStatements.add(statement);
    }

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", ticker='" + ticker + '\'' +
                ", industry='" + industry + '\'' +
                ", statements=" + financialStatements.size() +
                '}';
    }
}
