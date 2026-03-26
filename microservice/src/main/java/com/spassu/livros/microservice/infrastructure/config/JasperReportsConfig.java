package com.spassu.livros.microservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.InputStream;

@Slf4j
@Configuration
public class JasperReportsConfig {

    /**
     * Loads the pre-compiled JasperReport from the classpath.
     *
     * <p>The {@code .jasper} file is generated at build time by the
     * {@code precompile-jasper-reports} Maven execution (exec-maven-plugin,
     * prepare-package phase), which runs {@link JasperPrecompiler} with the
     * full project classpath available. This avoids having to spawn javac at
     * runtime inside a Spring Boot fat JAR where nested JARs are not visible
     * to external processes.
     */
    @Bean
    @Lazy
    public JasperReport relatorioLivrosReport() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/reports/relatorio_livros.jasper")) {
            if (is == null) {
                throw new IllegalStateException(
                        "Pre-compiled report not found: /reports/relatorio_livros.jasper. "
                        + "Ensure the Maven build ran the precompile-jasper-reports phase.");
            }
            log.info("Loading pre-compiled JasperReport from classpath:/reports/relatorio_livros.jasper");
            return (JasperReport) JRLoader.loadObject(is);
        }
    }
}
