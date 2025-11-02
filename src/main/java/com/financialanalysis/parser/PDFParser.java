package com.financialanalysis.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Parses PDF files and extracts text content
 */
public class PDFParser {
    private static final Logger logger = LoggerFactory.getLogger(PDFParser.class);

    /**
     * Extracts text from a PDF file
     *
     * @param pdfFile the PDF file to parse
     * @return the extracted text content
     * @throws IOException if there's an error reading the PDF
     */
    public String extractText(File pdfFile) throws IOException {
        logger.info("Extracting text from PDF: {}", pdfFile.getName());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            logger.info("Successfully extracted {} characters from {}", text.length(), pdfFile.getName());
            return text;
        } catch (IOException e) {
            logger.error("Error extracting text from PDF: {}", pdfFile.getName(), e);
            throw e;
        }
    }

    /**
     * Extracts text from a specific page range in a PDF file
     *
     * @param pdfFile the PDF file to parse
     * @param startPage the starting page (1-indexed)
     * @param endPage the ending page (1-indexed)
     * @return the extracted text content
     * @throws IOException if there's an error reading the PDF
     */
    public String extractText(File pdfFile, int startPage, int endPage) throws IOException {
        logger.info("Extracting text from PDF: {} (pages {}-{})", pdfFile.getName(), startPage, endPage);

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            String text = stripper.getText(document);
            logger.info("Successfully extracted {} characters from {} (pages {}-{})",
                    text.length(), pdfFile.getName(), startPage, endPage);
            return text;
        } catch (IOException e) {
            logger.error("Error extracting text from PDF: {}", pdfFile.getName(), e);
            throw e;
        }
    }
}
