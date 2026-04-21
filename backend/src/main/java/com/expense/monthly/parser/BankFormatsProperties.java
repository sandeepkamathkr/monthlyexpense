package com.expense.monthly.parser;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the list of bank CSV formats from application.yml (bank.formats[]).
 * Spring Boot binds this automatically — no @PropertySource or factory needed.
 *
 * To add a new bank: add one entry under bank.formats in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "bank")
@Data
public class BankFormatsProperties {

    private List<BankFormatConfig> formats = new ArrayList<>();
}
