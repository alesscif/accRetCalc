package org.example;

import java.time.LocalDate;

public class Config {
    private int startingAge;
    private int startingSalary;
    private int biYearlySalaryGrowth;
    private int retirementAge;
    private int monthlySpend;
    private int rent;
    private int startingSavings;
    private int ny;
    private boolean spendingFirst;
    private int targetMonthlySpending;
    private int targetSavingsRatePercent;

    public int getTargetSavingsRatePercent() {
        return targetSavingsRatePercent;
    }

    public void setTargetSavingsRatePercent(int targetSavingsRate) {
        this.targetSavingsRatePercent = targetSavingsRate;
    }

    public int getTargetMonthlySpending() {
        return targetMonthlySpending;
    }

    public void setTargetMonthlySpending(int targetMonthlySpending) {
        this.targetMonthlySpending = targetMonthlySpending;
    }

    public boolean isSpendingFirst() {
        return spendingFirst;
    }

    public void setSpendingFirst(boolean spendingFirst) {
        this.spendingFirst = spendingFirst;
    }

    public int getStartingAge() {
        return startingAge;
    }

    public void setStartingAge(int startingAge) {
        this.startingAge = startingAge;
    }

    public int getStartingSalary() {
        return startingSalary;
    }

    public void setStartingSalary(int startingSalary) {
        this.startingSalary = startingSalary;
    }

    public int getBiYearlySalaryGrowth() {
        return biYearlySalaryGrowth;
    }

    public void setBiYearlySalaryGrowth(int biYearlySalaryGrowth) {
        this.biYearlySalaryGrowth = biYearlySalaryGrowth;
    }

    public int getRetirementAge() {
        return retirementAge;
    }

    public void setRetirementAge(int retirementAge) {
        this.retirementAge = retirementAge;
    }

    public int getMonthlySpend() {
        return monthlySpend;
    }

    public void setMonthlySpend(int monthlySpend) {
        this.monthlySpend = monthlySpend;
    }

    public int getRent() {
        return rent;
    }

    public void setRent(int rent) {
        this.rent = rent;
    }

    public int getStartingSavings() {
        return startingSavings;
    }

    public void setStartingSavings(int startingSavings) {
        this.startingSavings = startingSavings;
    }

    public int getNy() {
        return ny;
    }

    public void setNy(int ny) {
        this.ny = ny;
    }
}
