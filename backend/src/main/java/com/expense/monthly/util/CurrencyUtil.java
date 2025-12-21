package com.expense.monthly.util;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for currency operations and validation.
 */
public class CurrencyUtil {

    /**
     * List of commonly supported currencies.
     */
    public static final List<String> SUPPORTED_CURRENCIES = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "KRW",
            "SGD", "HKD", "NOK", "SEK", "DKK", "PLN", "CZK", "HUF", "ILS", "ZAR",
            "BRL", "MXN", "RUB", "TRY", "NZD", "THB", "MYR", "PHP", "IDR", "VND"
    );

    /**
     * Validates if a currency code is supported.
     * 
     * @param currencyCode the currency code to validate
     * @return true if the currency is valid and supported
     */
    public static boolean isValidCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return false;
        }

        String upperCaseCode = currencyCode.trim().toUpperCase();
        
        // Check if it's a valid ISO currency code
        try {
            Currency.getInstance(upperCaseCode);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Check if it's in our supported list
        return SUPPORTED_CURRENCIES.contains(upperCaseCode);
    }

    /**
     * Validates and normalizes a currency code.
     * 
     * @param currencyCode the currency code to normalize
     * @return the normalized currency code
     * @throws IllegalArgumentException if the currency code is invalid
     */
    public static String validateAndNormalize(String currencyCode) {
        if (!isValidCurrency(currencyCode)) {
            throw new IllegalArgumentException("Invalid or unsupported currency code: " + currencyCode);
        }
        return currencyCode.trim().toUpperCase();
    }

    /**
     * Gets all supported currency codes.
     * 
     * @return set of supported currency codes
     */
    public static Set<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES.stream().collect(Collectors.toSet());
    }

    /**
     * Gets the currency symbol for a given currency code.
     * 
     * @param currencyCode the currency code
     * @return the currency symbol
     */
    public static String getCurrencySymbol(String currencyCode) {
        if (!isValidCurrency(currencyCode)) {
            return currencyCode;
        }
        
        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            return currency.getSymbol();
        } catch (IllegalArgumentException e) {
            return currencyCode;
        }
    }

    /**
     * Gets the display name for a currency code.
     * 
     * @param currencyCode the currency code
     * @return the currency display name
     */
    public static String getCurrencyDisplayName(String currencyCode) {
        if (!isValidCurrency(currencyCode)) {
            return currencyCode;
        }
        
        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            return currency.getDisplayName();
        } catch (IllegalArgumentException e) {
            return currencyCode;
        }
    }
}