package com.expense.monthly.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Categorizes transaction descriptions using keyword matching.
 * Ported from Python simple_processor.py — purely stateless.
 *
 * Category rules are evaluated in order; first match wins.
 * Descriptions matching EXCLUDE_FILTERS are skipped entirely (transfers, salary, etc.)
 */
@Component
public class TransactionCategorizer {

    private static final LinkedHashMap<String, List<String>> CATEGORY_RULES = new LinkedHashMap<>();

    private static final List<String> EXCLUDE_FILTERS = List.of(
            "HSBC CARDS", "BPAY PAYMENT", "TRANSFER FROM XX", "TRANSFER TO XX",
            "TRANSFER TO PEARLER", "FAST TRANSFER FROM", "TRANSFER TO K R KAMATH",
            "TRANSFER TO KAUSHIK PATEL", "MEDICARE BENEFIT", "DIRECT CREDIT",
            "PARENTALLEAVEPAY", "SALARY", "DR RABIA SHAIKH", "RETURN",
            "ULTRASOUND FOR WOMEN", "WESTERN SYDNEY ONCOLOGY", "AGA OVHC", "AIOI NISSAY"
    );

    static {
        CATEGORY_RULES.put("Eating out", List.of(
                "DOORDASH", "MENULOG", "UBER EATS", "RESTAURANT", "GUZMAN Y GOMEZ",
                "MCDONALD", "KFC", "SUBWAY", "PIZZA", "CAFE", "BAKERY", "DUMPLINGS",
                "CURRY LOVERS", "LITTLE GREECE", "INCAFE", "CHAT THAI", "SUSHI",
                "NOODLE BAR", "FRIED BROTHERS", "INDOCHAINESE", "MRS WANG",
                "AMRITSARI DHABA", "MESSINA", "JAIPUR SWEETS", "SARAVANAA BHAVAN",
                "BIGBITE", "RR BIRYANI", "BANH MI & CO", "FRANGO", "SQ *THE URBAN CHOWK"
        ));
        CATEGORY_RULES.put("Groceries", List.of(
                "COLES", "WOOLWORTHS", "ALDI", "IGA", "SUNRISE FRUIT", "SUNRISE FRESH",
                "GREEN FARM MEAT", "BUTCHER", "GROCERY", "SUPERMARKET", "FRESH", "LITTLE INDIA"
        ));
        CATEGORY_RULES.put("Medicine", List.of(
                "CHEMIST WAREHOUSE", "SHOPBACK CHEMIST", "PHARMACY", "QSCAN",
                "LAVERTY PATHOLOGY", "DARCY ROAD PHARMACY", "DENTIST", "HOSPITAL",
                "CLINIC", "MEDICARE"
        ));
        CATEGORY_RULES.put("Transport", List.of(
                "TRANSPORTFORNSW", "OPAL", "TRAIN", "BUS", "FERRY",
                "UBER *TRIP", "UBER *RIDE", "TAXI", "RIDESHARE", "PARKING",
                "WILSON PARKING", "EASYPARK", "SPEEDWAY", "METRO PETROLEUM",
                "PETROLEUM", "PETROL"
        ));
        CATEGORY_RULES.put("Sandeep Fitness", List.of(
                "BOXFITNESS", "GYM", "FITNESS", "YOGA", "PILATES", "SPORT", "GOCARDLESS"
        ));
        CATEGORY_RULES.put("Entertainment", List.of(
                "NETFLIX", "SPOTIFY", "APPLE.COM/BILL", "APPLE MUSIC", "DISNEY",
                "PAYPAL *NETFLIX", "PAYPAL *DISNEY", "AMAZON PRIME", "MOVIE", "CINEMA",
                "OPENAI", "CHATGPT", "CLAUDE.AI", "ANTHROPIC", "AUDIBLE", "KINDLE"
        ));
        CATEGORY_RULES.put("Shopping", List.of(
                "AMAZON", "EBAY", "OFFICEWORKS", "COSTCO", "TARGET", "KMART",
                "BUNNINGS", "BIG W", "THE REJECT SHOP", "IKEA", "DISCOUNT PARTY WAREHOUSE",
                "HOME AND HUTCH", "MICHE BOUTIQUE", "CAKE DECORATING", "SMART DOLLAR",
                "SPECSAVERS", "CHEESECAKE SHOP", "UDEMY"
        ));
        CATEGORY_RULES.put("Internet", List.of(
                "AUSSIE BROADBAND", "TELSTRA", "INTERNET", "NBN", "BROADBAND", "VODAFONEAUS"
        ));
        CATEGORY_RULES.put("Mobile bill", List.of(
                "OPTUS", "VODAFONE", "PAYPAL *VODAFONE", "PREPAID", "BOOST MOBILE"
        ));
        CATEGORY_RULES.put("Medical insurance", List.of(
                "MEDIBANK PHI", "BUPA", "HCF", "NIB", "HEALTH FUND", "HEALTH INSURANCE"
        ));
        CATEGORY_RULES.put("Life insurance", List.of(
                "MEDIBANK LIFE", "AIA", "TAL LIFE", "LIFE INSURANCE"
        ));
        CATEGORY_RULES.put("Car insurance", List.of(
                "TOYOTA INSURANCE", "AIOI NISSAY", "NRMA", "AAMI", "RACV", "CAR INSURANCE"
        ));
        CATEGORY_RULES.put("Car Service", List.of(
                "PARRAMATTA TOYOTA", "CAR SERVICE", "AUTO SERVICE", "MECHANIC"
        ));
        CATEGORY_RULES.put("Day care", List.of("ADVANCED EL"));
        CATEGORY_RULES.put("Isha Swimming", List.of("CUMBERLAND COUNC"));
        CATEGORY_RULES.put("Personal Care", List.of(
                "HAIRCUT", "BARBER", "SALON", "BEAUTY", "SMP*BROWN BOYS", "BROWN BOYS"
        ));
        CATEGORY_RULES.put("Rent", List.of("STARR PARTNERS", "RENT", "REAL ESTATE"));
        CATEGORY_RULES.put("Personal Transfer", List.of(
                "TRANSFER TO", "PAYID", "OSKO", "TRANSFER FROM", "KALATHIL", "KAMATH"
        ));
        CATEGORY_RULES.put("Bills", List.of("BPAY", "DIRECT DEBIT", "SUBSCRIPTION"));
        CATEGORY_RULES.put("Fees", List.of(
                "INTERNATIONAL TRANSACTION FEE", "OVERSEAS TRANSACTION FEE", "FEE", "CHARGE"
        ));
    }

    /**
     * Returns the category for the given description, or "Other" if no rule matches.
     */
    public String categorize(String description) {
        String upper = description.toUpperCase();
        for (Map.Entry<String, List<String>> entry : CATEGORY_RULES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (upper.contains(keyword.toUpperCase())) {
                    return entry.getKey();
                }
            }
        }
        return "Other";
    }

    /**
     * Returns true if the description matches any exclude filter
     * (transfers, salary, income credits, etc.) — these rows should be skipped.
     */
    public boolean isExcluded(String description) {
        String upper = description.toUpperCase();
        return EXCLUDE_FILTERS.stream().anyMatch(f -> upper.contains(f.toUpperCase()));
    }

    /**
     * Returns all available category names (for frontend dropdowns).
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>(CATEGORY_RULES.keySet());
        categories.add("Other");
        return categories;
    }
}
