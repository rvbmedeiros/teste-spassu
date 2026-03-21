package com.spassu.livros.microservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Slf4j
@Configuration
public class JasperReportsConfig {

    @Bean
    public JasperReport relatorioLivrosReport() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/reports/relatorio_livros.jrxml")) {
            if (is == null) {
                throw new IllegalStateException("Report template not found: /reports/relatorio_livros.jrxml");
            }
            return JasperCompileManager.compileReport(is);
        }
    }
}
