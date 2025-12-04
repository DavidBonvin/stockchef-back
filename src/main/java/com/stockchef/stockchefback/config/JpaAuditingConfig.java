package com.stockchef.stockchefback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration pour l'audit automatique JPA
 * Permet le fonctionnement des annotations @CreatedDate, @LastModifiedDate, etc.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}