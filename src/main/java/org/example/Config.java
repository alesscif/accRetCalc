package org.example;

import java.time.LocalDate;

public class Config {
    private LocalDate birthday;
    private LocalDate startDate;
    private int startingSalary;
    private int biYearlySalaryGrowth;
    private int retirementAge;
    private int monthlySpend;
    private int rent;
    private int startingSavings;
    private int ny;

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
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
