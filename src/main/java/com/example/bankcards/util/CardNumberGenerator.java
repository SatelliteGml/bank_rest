package com.example.bankcards.util;

import java.util.Random;

public class CardNumberGenerator {

    private static final String BIN = "4"; // Visa cards start with 4
    private static final int CARD_LENGTH = 16;
    private static final Random random = new Random();

    public static String generate() {
        StringBuilder cardNumber = new StringBuilder(BIN);

        for (int i = 0; i < CARD_LENGTH - 1; i++) {
            int digit = random.nextInt(10);
            cardNumber.append(digit);
        }

        int checkDigit = calculateCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private static int calculateCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    public static String generateCVV() {
        return String.format("%03d", random.nextInt(1000));
    }
}
