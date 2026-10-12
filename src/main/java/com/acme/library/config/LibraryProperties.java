package com.acme.library.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Validated 
@ConfigurationProperties(prefix = "library")
public record LibraryProperties(@NotBlank String timeZone, @NotNull Loan loan) {
    
    public record Loan(
        @Positive int periodDays,            
        @Positive int maxActivePerMember,     
        @PositiveOrZero int maxRenewals,      
        @Positive int renewalDays,            
        @NotNull @PositiveOrZero BigDecimal lateFeePerDay
    ) {
        
    }
}
