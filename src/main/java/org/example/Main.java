package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Scanner;
import static org.apache.poi.ss.formula.functions.FinanceLib.pmt;

public class Main {
    private static final int[] brackets = {0, 11600, 47150, 100525, 191950, 243725, 609350, Integer.MAX_VALUE};
    private static final double[] nyBrackets = {0, 8500, 11700, 13900, 80650, 215400, 1077550, 5000000, 25000000, Integer.MAX_VALUE};
    private static double standardDeduction = 14600;
    private static final double socialSecurityTaxRate = 0.062;
    private static final double medicareTaxRate = 0.0145;
    private static final double socialSecurityWageLimit = 168600;
    private static final double[] bracketTaxRates = {0, 0.10, 0.12, 0.22, 0.24, 0.32, 0.35, 0.37};
    private static final double[] nyBracketTaxRates = {0, 0.04, 0.045, 0.0525, 0.0585, 0.0625, 0.0685, 0.0965, 0.103, 0.109};
    private static double preTaxRetirementLimit = 23000;
    private static final double apyAfterInflation = 3;
    private static final double inflationPercent = 4;

    public static void main(String[] args) {
        String configFilename = "src/main/resources/profile.json";

        ConfigService configService = new ConfigService(configFilename, new Scanner(System.in));

        configService.displayConfig();

        configService.updateConfig();

        simulate(configService.getConfig());
    }

    private static void simulate(Config config) {
        int biYearlySalaryGrowth = config.getBiYearlySalaryGrowth();
        int ny = config.getNy();
        LocalDate birthday = config.getBirthday();
        int retirementAge = config.getRetirementAge();

        int accumulatedSavings = config.getStartingSavings();
        int currentYear = config.getStartDate().getYear();
        double currentSalary = config.getStartingSalary() - config.getBiYearlySalaryGrowth(); // Account for initial
        int monthlySpend = config.getMonthlySpend();
        int rent = config.getRent();

        int salaryAdjustmentToggle = 0;

        while (currentYear < config.getBirthday().plusYears(config.getRetirementAge()).getYear()) {
            currentSalary = salaryAdjustmentToggle == 0 ? currentSalary + biYearlySalaryGrowth : currentSalary;
            accumulatedSavings = updateSavings(accumulatedSavings, currentSalary, monthlySpend, rent, ny);
            monthlySpend = inflationAdjust(monthlySpend, inflationPercent);
            rent = inflationAdjust(rent, inflationPercent + 1);

            updateTaxBrackets();
            currentYear++;
            salaryAdjustmentToggle = -salaryAdjustmentToggle + 1;
        }

        System.out.println("\n------------------------------------------------");
        System.out.println("\nTotal savings (in today's dollars): \n$" + accumulatedSavings);

        int withdrawalYears = birthday.plusYears(100).getYear() - birthday.plusYears(retirementAge).getYear();
        double initialWithdrawal = pmt(apyAfterInflation / 100, withdrawalYears, accumulatedSavings * -1, 0, true);

        System.out.println("\nYear 1 monthly withdrawals (in today's dollars): \n$" + Math.round(initialWithdrawal / 12));
    }

    private static int updateSavings(int accumulatedSavings, double currentSalary, int monthlySpend, int rent, int ny) {
        double netIncome = calculateAnnualNetIncome(currentSalary, ny);
        int monthlySavings = (int) Math.floor(netIncome / 12) - (monthlySpend + rent);
        return (int) Math.floor(compound(accumulatedSavings, apyAfterInflation / 100, monthlySavings));
    }

    private static int inflationAdjust(double value, double rate) {
        return (int) (value * (1 + rate / 100));
    }

    private static void updateTaxBrackets() {
        for (int i = 1; i < brackets.length; i++) {
            brackets[i] =  inflationAdjust(brackets[i], inflationPercent - 0.25);
        }
        standardDeduction = inflationAdjust(standardDeduction, inflationPercent - 0.25);
        preTaxRetirementLimit = inflationAdjust(preTaxRetirementLimit, inflationPercent - 0.25);
    }

    public static double calculateAnnualNetIncome (double yearlySalary, int ny) {
        double socialSecurityDeduction = Math.min(yearlySalary * socialSecurityTaxRate,
                socialSecurityWageLimit * socialSecurityTaxRate);
        double medicareDeduction = yearlySalary * medicareTaxRate;
        double taxableIncome = yearlySalary - socialSecurityDeduction - medicareDeduction - standardDeduction - preTaxRetirementLimit;
        double incomeTax = 0.0;

        for (int i = 1; i < brackets.length; i++) {
            if (taxableIncome >= brackets[i]) {
                incomeTax += (brackets[i] - brackets[i - 1]) * bracketTaxRates[i];
            } else {
                incomeTax += (taxableIncome - brackets[i - 1]) * bracketTaxRates[i];
                break;
            }
        }

        if (ny == 1) {
            double nyStandardDeduction = 8000;
            double nyTaxableIncome = taxableIncome - nyStandardDeduction;
            double nyIncomeTax = 0.0;
            for (int i = 1; i < nyBrackets.length; i++) {
                if (nyTaxableIncome >= nyBrackets[i]) {
                    nyIncomeTax += (nyBrackets[i] - nyBrackets[i - 1]) * nyBracketTaxRates[i];
                } else {
                    nyIncomeTax += (nyTaxableIncome - nyBrackets[i - 1]) * nyBracketTaxRates[i];
                    break;
                }
            }
            incomeTax += nyIncomeTax;
        }

        return yearlySalary - socialSecurityDeduction - medicareDeduction - incomeTax;
    }

    public static double compound(double principal, double rate, double contribution) {

        double principalWithInterest = principal * Math.pow((1 + rate / 12), 12);
        double contributionsWithInterest = contribution * (Math.pow(1 + rate / 12, 12) - 1) / (rate / 12);

        return principalWithInterest + contributionsWithInterest;
    }
}