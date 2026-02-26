package com.brewforce.pkpl.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
    @PropertySource(value = "file:.env", ignoreResourceNotFound = true)
})
public class EnvConfig {
    // Kelas konfigurasi kosong untuk load property source
}