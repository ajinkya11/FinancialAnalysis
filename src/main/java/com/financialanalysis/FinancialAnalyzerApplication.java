package com.financialanalysis;

import com.financialanalysis.cli.FinancialAnalyzerCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Main Spring Boot application for the Airline Financial Analyzer
 */
@SpringBootApplication
public class FinancialAnalyzerApplication implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final FinancialAnalyzerCommand financialAnalyzerCommand;
    private int exitCode;

    public FinancialAnalyzerApplication(IFactory factory, FinancialAnalyzerCommand financialAnalyzerCommand) {
        this.factory = factory;
        this.financialAnalyzerCommand = financialAnalyzerCommand;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(FinancialAnalyzerApplication.class, args)));
    }

    @Override
    public void run(String... args) throws Exception {
        // Register JANSI for colored output
        org.fusesource.jansi.AnsiConsole.systemInstall();

        try {
            CommandLine commandLine = new CommandLine(financialAnalyzerCommand, factory);

            // Add subcommands
            commandLine.addSubcommand("compare", new FinancialAnalyzerCommand.CompareCommand());
            commandLine.addSubcommand("info", new FinancialAnalyzerCommand.InfoCommand());
            commandLine.addSubcommand("cache", new FinancialAnalyzerCommand.CacheCommand());

            exitCode = commandLine.execute(args);
        } finally {
            org.fusesource.jansi.AnsiConsole.systemUninstall();
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
