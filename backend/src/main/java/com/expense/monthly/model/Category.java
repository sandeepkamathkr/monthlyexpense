package com.expense.monthly.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Category {

    private Category() {}

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            "Eating out", "Groceries", "Medicine", "Transport", "Sandeep Fitness",
            "Entertainment", "Shopping", "Internet", "Mobile bill", "Medical insurance",
            "Life insurance", "Car insurance", "Car Service", "Day care", "Isha Swimming",
            "Personal Care", "Rent", "Personal Transfer", "Bills", "Fees","Electricity bill","Gas bill", "Other"
    ));
}