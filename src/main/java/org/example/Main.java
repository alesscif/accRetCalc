package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import static org.apache.poi.ss.formula.functions.FinanceLib.pmt;

public class Main {

    private static final int[] brackets = {0, 11600, 47150, 100525, 191950, 243725, 609350, Integer.MAX_VALUE};
    private static double standardDeduction = 14600;
    private static final double socialSecurityTaxRate = 0.062;
    private static final double medicareTaxRate = 0.0145;
    private static final double socialSecurityWageLimit = 168600;
    private static final double[] bracketTaxRates = {0, 0.10, 0.12, 0.22, 0.24, 0.32, 0.35, 0.37};
    private static double preTaxRetirementLimit = 23000;
    private static final double apyAfterInflation = 3; // Changed to camelCase
    private static final double inflationPercent = 4; // Changed to camelCase

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // get user input
        LocalDate birthday = getDateFromUser(input, "Enter birth date (YYYY-MM-DD):");
        LocalDate startDate = getDateFromUser(input, "\nEnter simulation start date (YYYY-MM-DD):");
        int startingSalary = getIntFromUser(input, "\nEnter starting salary:");
        int biYearlySalaryGrowth = getIntFromUser(input, "\nEnter projected bi-yearly salary growth:");
        int retirementAge = getIntFromUser(input, "\nEnter target retirement age:");
        int monthlySpend = getIntFromUser(input, "\nEnter monthly expenses (excluding rent):");
        int rent = getIntFromUser(input, "\nEnter monthly rent:");
        int startingSavings = getIntFromUser(input, "\nEnter savings at simulation start:");

        // begin simulation
        int accumulatedSavings = startingSavings;
        int currentYear = startDate.getYear();
        double currentSalary = startingSalary - biYearlySalaryGrowth; // Account for initial
        int salaryAdjustmentToggle = 0;

        while (currentYear < birthday.plusYears(retirementAge).getYear()) {
            currentSalary = salaryAdjustmentToggle == 0 ? currentSalary + biYearlySalaryGrowth : currentSalary;
            accumulatedSavings = updateSavings(accumulatedSavings, currentSalary, monthlySpend, rent);
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

    private static LocalDate getDateFromUser(Scanner input, String prompt) {
        while (true) {
            System.out.println(prompt);
            try {
                return LocalDate.parse(input.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Please use YYYY-MM-DD.");
            }
        }
    }

    private static int getIntFromUser(Scanner input, String prompt) {
        System.out.println(prompt);
        return input.nextInt();
    }

    private static int updateSavings(int accumulatedSavings, double currentSalary, int monthlySpend, int rent) {
        double netIncome = calculateAnnualNetIncome(currentSalary);
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

    public static double calculateAnnualNetIncome (double yearlySalary) {
        double socialSecurityDeduction = Math.min(yearlySalary * socialSecurityTaxRate,
                socialSecurityWageLimit * socialSecurityTaxRate);
        double medicareDeduction = yearlySalary * medicareTaxRate;
        double taxableIncome = yearlySalary - socialSecurityDeduction - medicareDeduction - standardDeduction - preTaxRetirementLimit;
        double federalIncomeTax = 0.0;

        for (int i = 1; i < brackets.length; i++) {
            if (taxableIncome >= brackets[i]) {
                federalIncomeTax += (brackets[i] - brackets[i - 1]) * bracketTaxRates[i];
            } else {
                federalIncomeTax += (taxableIncome - brackets[i - 1]) * bracketTaxRates[i];
                break;
            }
        }
        return yearlySalary - socialSecurityDeduction - medicareDeduction - federalIncomeTax;
    }

    public static double compound(double principal, double rate, double contribution) {

        double principalWithInterest = principal * Math.pow((1 + rate / 12), 12);
        double contributionsWithInterest = contribution * (Math.pow(1 + rate / 12, 12) - 1) / (rate / 12);

        return principalWithInterest + contributionsWithInterest;
    }
}