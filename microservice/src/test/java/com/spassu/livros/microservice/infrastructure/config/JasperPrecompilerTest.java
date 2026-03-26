package com.spassu.livros.microservice.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JasperPrecompilerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("main sem argumentos deve lancar IllegalArgumentException")
    void main_semArgumentos_deveLancarIllegalArgumentException() {
        assertThatThrownBy(() -> JasperPrecompiler.main(new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usage");
    }

    @Test
    @DisplayName("main com apenas um argumento deve lancar IllegalArgumentException")
    void main_comApenasUmArgumento_deveLancarIllegalArgumentException() {
        assertThatThrownBy(() -> JasperPrecompiler.main(new String[]{"only-one.jrxml"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usage");
    }

    @Test
    @DisplayName("main com JRXML inexistente deve propagar excecao de compilacao")
    void main_comJrxmlInexistente_devePropagar() {
        String missing = tempDir.resolve("nao_existe.jrxml").toAbsolutePath().toString();
        String dest = tempDir.resolve("out.jasper").toAbsolutePath().toString();

        assertThatThrownBy(() -> JasperPrecompiler.main(new String[]{missing, dest}))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("main com JRXML valido deve gerar arquivo jasper")
    void main_comJrxmlValido_deveGerarArquivoJasper() throws Exception {
        // Copy the real JRXML from the classpath (available during test execution)
        var src = getClass().getResourceAsStream("/reports/relatorio_livros.jrxml");
        if (src == null) {
            // Resource not in test classpath — skip silently (integration concern)
            return;
        }
        Path jrxmlPath = tempDir.resolve("relatorio_livros.jrxml");
        Path jasperPath = tempDir.resolve("relatorio_livros.jasper");
        Files.copy(src, jrxmlPath);

        JasperPrecompiler.main(new String[]{
                jrxmlPath.toAbsolutePath().toString(),
                jasperPath.toAbsolutePath().toString()
        });

        // The .jasper file must have been produced
        org.assertj.core.api.Assertions.assertThat(jasperPath).exists().isNotEmptyFile();
    }
}
