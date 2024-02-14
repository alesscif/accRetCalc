package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import static org.apache.poi.ss.formula.functions.FinanceLib.pmt;

public class Main {

    private static final int[] BRACKETS = {0, 11600, 47150, 100525, 191950, 243725, 609350, Integer.MAX_VALUE};
    private static double STANDARD_DEDUCTION = 14600;
    private static final double SOCIAL_SECURITY_TAX_RATE = 0.062;
    private static final double MEDICARE_TAX_RATE = 0.0145;
    private static final double SOCIAL_SECURITY_WAGE_LIMIT = 168600;
    private static final double[] BRACKET_TAX_RATES = {0, 0.10, 0.12, 0.22, 0.24, 0.32, 0.35, 0.37};
    private static double PRE_TAX_RETIREMENT_LIMIT = 23000;
    private static final double APY_AFTER_INFLATION = 3;
    private static final double INFLATION_PERCENT = 4;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // get user input
        LocalDate BIRTHDAY = getDateFromUser(input, "Enter birth date (YYYY-MM-DD):");
        LocalDate START_DATE = getDateFromUser(input, "\nEnter simulation start date (YYYY-MM-DD):");
        int STARTING_SALARY = getIntFromUser(input, "\nEnter starting salary:");
        int BI_YEARLY_SALARY_GROWTH = getIntFromUser(input, "\nEnter projected bi-yearly salary growth:");
        int RETIREMENT_AGE = getIntFromUser(input, "\nEnter target retirement age:");
        int MONTHLY_SPEND = getIntFromUser(input, "\nEnter monthly expenses (excluding rent):");
        int RENT = getIntFromUser(input, "\nEnter monthly rent:");
        int STARTING_SAVINGS = getIntFromUser(input, "\nEnter savings at simulation start:");

        // begin simulation
        int accumulatedSavings = STARTING_SAVINGS;
        int currentYear = START_DATE.getYear();
        double currentSalary = STARTING_SALARY - BI_YEARLY_SALARY_GROWTH; // Account for initial adjustment
        int salaryAdjustmentToggle = 0; // Used for bi-yearly increases

        while (currentYear < BIRTHDAY.plusYears(RETIREMENT_AGE).getYear()) {
            currentSalary = salaryAdjustmentToggle == 0 ? currentSalary + BI_YEARLY_SALARY_GROWTH : currentSalary;
            accumulatedSavings = updateSavings(accumulatedSavings, currentSalary, MONTHLY_SPEND, RENT);
            MONTHLY_SPEND = inflationAdjust(MONTHLY_SPEND, INFLATION_PERCENT);
            RENT = inflationAdjust(RENT, INFLATION_PERCENT + 1);
            updateTaxBrackets();
            currentYear++;
            salaryAdjustmentToggle = -salaryAdjustmentToggle + 1;  // Toggle adjustment
        }

        System.out.println("\n------------------------------------------------");
        System.out.println("\ntotal savings (in today's dollars): \n$" + accumulatedSavings);

        int withdrawalYears = BIRTHDAY.plusYears(100).getYear() - BIRTHDAY.plusYears(RETIREMENT_AGE).getYear();
        double initialWithdrawal = pmt(APY_AFTER_INFLATION / 100, withdrawalYears, accumulatedSavings * -1, 0, true);

        System.out.println("\nyear 1 monthly withdrawals (in today's dollars): \n$" + Math.round(initialWithdrawal / 12));
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
        return (int) Math.floor(compound(accumulatedSavings, APY_AFTER_INFLATION / 100, monthlySavings));
    }

    private static int inflationAdjust(double value, double rate) {
        return (int) (value * (1 + rate / 100));
    }

    private static void updateTaxBrackets() {
        for (int i = 1; i < BRACKETS.length; i++) {
            BRACKETS[i] =  inflationAdjust(BRACKETS[i], INFLATION_PERCENT - 0.25);
        }
        STANDARD_DEDUCTION = inflationAdjust(STANDARD_DEDUCTION, INFLATION_PERCENT - 0.25);
        PRE_TAX_RETIREMENT_LIMIT = inflationAdjust(PRE_TAX_RETIREMENT_LIMIT, INFLATION_PERCENT - 0.25);
    }

    public static double calculateAnnualNetIncome (double yearlySalary) {
        double socialSecurityDeduction = Math.min(yearlySalary * SOCIAL_SECURITY_TAX_RATE,
                SOCIAL_SECURITY_WAGE_LIMIT * SOCIAL_SECURITY_TAX_RATE);
        double medicareDeduction = yearlySalary * MEDICARE_TAX_RATE;
        double taxableIncome = yearlySalary - socialSecurityDeduction - medicareDeduction - STANDARD_DEDUCTION - PRE_TAX_RETIREMENT_LIMIT;
        double federalIncomeTax = 0.0;

        for (int i = 1; i < BRACKETS.length; i++) {
            if (taxableIncome >= BRACKETS[i]) {
                federalIncomeTax += (BRACKETS[i] - BRACKETS[i - 1]) * BRACKET_TAX_RATES[i];
            } else {
                federalIncomeTax += (taxableIncome - BRACKETS[i - 1]) * BRACKET_TAX_RATES[i];
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