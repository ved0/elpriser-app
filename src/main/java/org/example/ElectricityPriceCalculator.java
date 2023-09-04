package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ElectricityPriceCalculator {
    private final Map<Integer, Integer> priceByHour;

    private final Scanner input;

    public ElectricityPriceCalculator() {
        this.priceByHour = new HashMap<>();
        this.input = new Scanner(System.in);
    }

    private void yourChoice(int choice) {
        if ((priceByHour.isEmpty() && choice != 1)) {
            if (choice == 5) {
                readPricesFromFile();
            } else {
                System.err.println("Du måste mata in elpriserna i programmet först!" + ((choice > 5) ? " Samt välja ett giltigt val från menyn!" : ""));
            }
        } else {
            switch (choice) {
                case 1 -> setPriceByHour();
                case 2 -> minMaxAverage();
                case 3 -> sortLowestToHighestPrice();
                case 4 -> bestChargingTime();
                case 5 ->  readPricesFromFile();
                default -> System.err.println("Vänligen välj ett alternativ från menyn");
            }
        }
    }

    private void setPriceByHour() {
        System.out.println("Vänligen mata in elpriserna för dygnet (timme för timme)");
        System.out.println("=====================================================");
        for (int i = 0; i < 24; i++) {
            System.out.println("Lägg till priset (i öre) för intervallet " + fromHour(i) + " - " + toHour(i));
            if (input.hasNextInt()) {
                int price = input.nextInt();
                priceByHour.put(i, price);
            } else {
                System.err.println("**** Vänligen mata in ett giltigt värde (i öre)! ****");
                i--;
            }
            input.nextLine();
        }
        System.out.println("Tack! Elpriserna för dygnet är nu inlagda!");
    }

    private void minMaxAverage() {
        int minPrice = Collections.min(priceByHour.values());
        int maxPrice = Collections.max(priceByHour.values());
        int total = 0;
        Set<Integer> minHours = new HashSet<>();
        Set<Integer> maxHours = new HashSet<>();
        for (Map.Entry<Integer, Integer> e : priceByHour.entrySet()) {
            if (e.getValue().equals(minPrice)) {
                minHours.add(e.getKey());
            } else if (e.getValue().equals(maxPrice)) {
                maxHours.add(e.getKey());
            }
            total += e.getValue();
        }
        System.out.println("Lägsta priset under dagen är " + minPrice + " öre och infaller" + ((minHours.size() == 1) ? " kl. " : " dessa klockslag ") + formatPriceSet(minHours));
        System.out.println("Högsta priset under dagen är " + maxPrice + " öre och infaller" + ((maxHours.size() == 1) ? " kl. " : " dessa klockslag ") + formatPriceSet(maxHours));
        System.out.println("Genomsnittspriset för dagen är " + total / 24 + " öre per timme.");
    }

    private void sortLowestToHighestPrice() {
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(priceByHour.entrySet());
        list.sort(Map.Entry.comparingByValue());
        for (Map.Entry<Integer, Integer> e : list) {
            int hour = e.getKey();
            System.out.println(fromHour(hour) + "-" + toHour(hour) + "   " + e.getValue() + " öre");
        }
    }

    private void bestChargingTime() {
        List<Map.Entry<Integer, Integer>> hourPrices = new ArrayList<>(priceByHour.entrySet());
        int minSum = Integer.MAX_VALUE;
        int startingHour = 0;
        for (int i = 0; i < hourPrices.size() - 3; i++) {
            int sum = hourPrices.get(i).getValue() +
                    hourPrices.get(i + 1).getValue() +
                    hourPrices.get(i + 2).getValue() +
                    hourPrices.get(i + 3).getValue();
            minSum = Math.min(minSum, sum);
            if (minSum == sum) {
                startingHour = i;
            }
        }
        System.out.println("För att ladda bilen med lägst totalpris, bör du börja ladda kl " + fromHour(startingHour) + ".\nDå blir totalkostnaden " + minSum + " öre med ett medelpris på " + (double) minSum / 4 + " öre per timme.");
    }

    private void readPricesFromFile() {
        File fs = new File("src/main/resources/priser.csv");
        try (Scanner readFromFile = new Scanner(fs)) {
            readFromFile.useDelimiter(",");
            while (readFromFile.hasNext()) {
                for (int i = 0; i < 24; i++) {
                    priceByHour.put(i, Integer.valueOf(readFromFile.next()));
                }
            }
            System.out.println("Priserna från filen är nu inlagda i programmet.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (NumberFormatException e){
            System.err.println("Filen måste innehålla nummer (priset i öre) bara!");
        }
    }

    private void delay() {
        System.out.println("=====================================================");
        System.out.println();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String formatPriceSet(Set<Integer> setOfHours) {
        StringBuilder toReturn = new StringBuilder();
        for (int hour : setOfHours) {
            toReturn.append("[").append(fromHour(hour)).append("], ");
        }
        return toReturn.substring(0, toReturn.length() - 2) + ".";
    }

    private String fromHour(int hour) {
        return (hour < 10) ? "0" + hour : Integer.toString(hour);
    }

    private String toHour(int hour) {
        return (hour < 9) ? "0" + (hour + 1) : (hour + 1 == 24) ? "00" : Integer.toString(hour + 1);
    }

    private void showMenu() {
        System.out.println("Elpriser - meny");
        System.out.println("=================");
        System.out.println("1. Inmatning");
        System.out.println("2. Min, Max och Medel");
        System.out.println("3. Sortera");
        System.out.println("4. Bästa Laddningstid (4h)");
        System.out.println("5. Ladda in priserna från en fil");
        System.out.println("e. Avsluta");
    }

    public void start() {
        showMenu();
        String choice = input.nextLine();
        while (!choice.equalsIgnoreCase("e")) {
            try {
                yourChoice(Integer.parseInt(choice));
                delay();
                showMenu();
            } catch (Exception e) {
                System.err.println("Välj något av alternativen 1-5 eller avsluta programmet med 'e'");
            }
            choice = input.nextLine();
        }
        System.out.println("Tack för denna gång. Programmet avslutas nu.");
    }
}
