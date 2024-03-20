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

public class ConfigService {
    private Config config;
    private String filename;
    private Scanner input;

    public ConfigService(String filename, Scanner input) {
        this.filename = filename;
        this.input = input;
        this.config = loadConfig();
    }

    public Config getConfig() {
        return config;
    }

    public void displayConfig() {
        int counter = 1;
        Class<? extends Config> configClass = config.getClass();

        System.out.println();

        for (Field field : configClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(config);
                System.out.println(counter + ". " + field.getName() + ": " + fieldValue);
                counter++;
            } catch (IllegalAccessException e) {
                System.err.println("Error accessing field: " + field.getName());
            }
        }
    }

    private Config loadConfig() {
        try (Reader reader = new FileReader(filename)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .setPrettyPrinting()
                    .create();
            Config config = gson.fromJson(reader, Config.class);
            return Objects.requireNonNullElseGet(config, Config::new);
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            return new Config();
        }
    }

    public void updateConfig() {
        System.out.println("\nSelect a value to update (enter the number), or 0 to continue:");

        String choice = input.nextLine();
        while (!choice.equals("0")) {
            try {
                int choiceNum = Integer.parseInt(choice);
                updateValue(choiceNum);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            System.out.println("\nUpdated Config:");
            displayConfig();
            System.out.println("\nUpdate another value (enter the number), or 0 to continue:");
            choice = input.nextLine();
        }
        saveConfig();
    }

    private void updateValue(int choice) throws Exception {
        Field[] fields = Config.class.getDeclaredFields();
        if (choice >= 1 && choice <= fields.length) {
            Field fieldToUpdate = fields[choice - 1];
            fieldToUpdate.setAccessible(true); // Allow access to private fields

            if (fieldToUpdate.getType() == LocalDate.class) {
                fieldToUpdate.set(config, getDateFromUser("Enter new " + fieldToUpdate.getName() + " (YYYY-MM-DD): "));
            } else if (fieldToUpdate.getType() == int.class) {
                fieldToUpdate.setInt(config, getIntFromUser("Enter new " + fieldToUpdate.getName() + ": "));
            } else {
                System.out.println("Update not supported for the selected field type.");
            }
        } else {
            System.out.println("Invalid Choice.");
        }
    }

    private void saveConfig() {
        try (Writer writer = new FileWriter(filename)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .setPrettyPrinting()
                    .create();
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    private LocalDate getDateFromUser(String prompt) {
        while (true) {
            System.out.println(prompt);
            try {
                return LocalDate.parse(input.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Please use YYYY-MM-DD.");
            }
        }
    }

    private int getIntFromUser(String prompt) {
        System.out.println(prompt);
        return input.nextInt();
    }
}
