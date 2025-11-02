package com.financialanalysis.storage;

import com.financialanalysis.model.Company;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles storage and retrieval of company data using JSON files
 */
public class DataStore {
    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);
    private static final String DATA_DIR = "data/companies";
    private final Gson gson;

    public DataStore() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDataDirectoryExists();
    }

    /**
     * Saves a company's data to a JSON file
     *
     * @param company the company to save
     * @throws IOException if there's an error writing the file
     */
    public void saveCompany(Company company) throws IOException {
        String fileName = sanitizeFileName(company.getTicker()) + ".json";
        Path filePath = Paths.get(DATA_DIR, fileName);

        logger.info("Saving company data to: {}", filePath);

        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(company, writer);
            logger.info("Successfully saved company: {} ({})", company.getName(), company.getTicker());
        } catch (IOException e) {
            logger.error("Error saving company data for {}", company.getTicker(), e);
            throw e;
        }
    }

    /**
     * Loads a company's data from a JSON file
     *
     * @param ticker the company ticker symbol
     * @return the loaded company, or null if not found
     * @throws IOException if there's an error reading the file
     */
    public Company loadCompany(String ticker) throws IOException {
        String fileName = sanitizeFileName(ticker) + ".json";
        Path filePath = Paths.get(DATA_DIR, fileName);

        if (!Files.exists(filePath)) {
            logger.warn("Company data not found for ticker: {}", ticker);
            return null;
        }

        logger.info("Loading company data from: {}", filePath);

        try (Reader reader = new FileReader(filePath.toFile())) {
            Company company = gson.fromJson(reader, Company.class);
            logger.info("Successfully loaded company: {} ({})", company.getName(), company.getTicker());
            return company;
        } catch (IOException e) {
            logger.error("Error loading company data for {}", ticker, e);
            throw e;
        }
    }

    /**
     * Loads all companies from the data directory
     *
     * @return list of all companies
     * @throws IOException if there's an error reading files
     */
    public List<Company> loadAllCompanies() throws IOException {
        List<Company> companies = new ArrayList<>();
        File dataDir = new File(DATA_DIR);

        if (!dataDir.exists() || !dataDir.isDirectory()) {
            logger.warn("Data directory does not exist: {}", DATA_DIR);
            return companies;
        }

        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            logger.info("No company data files found");
            return companies;
        }

        logger.info("Loading {} company files", files.length);

        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                Company company = gson.fromJson(reader, Company.class);
                companies.add(company);
                logger.debug("Loaded company: {}", company.getName());
            } catch (IOException e) {
                logger.error("Error loading company from file: {}", file.getName(), e);
            }
        }

        logger.info("Successfully loaded {} companies", companies.size());
        return companies;
    }

    /**
     * Checks if a company exists in storage
     *
     * @param ticker the company ticker symbol
     * @return true if the company exists, false otherwise
     */
    public boolean companyExists(String ticker) {
        String fileName = sanitizeFileName(ticker) + ".json";
        Path filePath = Paths.get(DATA_DIR, fileName);
        return Files.exists(filePath);
    }

    /**
     * Deletes a company's data
     *
     * @param ticker the company ticker symbol
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteCompany(String ticker) {
        String fileName = sanitizeFileName(ticker) + ".json";
        Path filePath = Paths.get(DATA_DIR, fileName);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.info("Deleted company data for ticker: {}", ticker);
            } else {
                logger.warn("Company data not found for ticker: {}", ticker);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Error deleting company data for {}", ticker, e);
            return false;
        }
    }

    /**
     * Ensures the data directory exists
     */
    private void ensureDataDirectoryExists() {
        Path path = Paths.get(DATA_DIR);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created data directory: {}", DATA_DIR);
            }
        } catch (IOException e) {
            logger.error("Error creating data directory", e);
        }
    }

    /**
     * Sanitizes a file name by removing invalid characters
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
