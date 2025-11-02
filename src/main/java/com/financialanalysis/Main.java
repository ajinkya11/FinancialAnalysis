package com.financialanalysis;

import com.financialanalysis.cli.CLI;
import picocli.CommandLine;

/**
 * Main entry point for the Financial Analysis CLI application
 */
public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CLI()).execute(args);
        System.exit(exitCode);
    }
}
