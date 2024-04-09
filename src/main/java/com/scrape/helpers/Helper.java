package com.scrape.helpers;

import java.util.ArrayList;
import java.util.Random;

public class Helper {
    public static String randomString(String[] arrayString) {
        Random random = new Random();
        String randomElement = arrayString[random.nextInt(arrayString.length)];
        return randomElement;
    }

    public static String randomString(ArrayList<String> arrayString) {
        Random random = new Random();
        int randomIndex = random.nextInt(arrayString.size());
        String randomElement = arrayString.get(randomIndex);
        return randomElement;
    }
}
