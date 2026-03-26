package com.spassu.livros.microservice.infrastructure.config;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperCompileManager;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Build-time utility: compiles a .jrxml report to a pre-compiled .jasper file.
 *
 * <p>This class is executed during the {@code prepare-package} Maven phase via
 * {@code exec-maven-plugin}. At that point the project classpath (including
 * jasperreports JARs) is fully available, so the javac sub-process spawned by
 * JasperReports can resolve all engine imports.
 *
 * <p>At runtime the application loads the pre-compiled {@code .jasper} file,
 * completely avoiding the classpath-visibility issue that occurs inside a
 * Spring Boot fat JAR.
 *
 * <p>Usage: {@code main(new String[]{sourcePath, destPath})}
 */
public class JasperPrecompiler {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Usage: JasperPrecompiler <source.jrxml> <dest.jasper>");
        }

        String src = args[0];
        String dest = args[1];

        System.out.println("[JasperPrecompiler] Source: " + src);
        System.out.println("[JasperPrecompiler] Dest  : " + dest);

        // Make sure the destination directory exists
        Files.createDirectories(Path.of(dest).getParent());

        // During exec:java the thread context classloader is a URLClassLoader
        // containing the project's full compile+runtime classpath. We propagate
        // those URLs to JasperReports so the spawned javac process can find all
        // engine classes (net.sf.jasperreports.engine.*).
        configureJasperCompilerClasspath();

        JasperCompileManager.compileReportToFile(src, dest);
        System.out.println("[JasperPrecompiler] Compiled successfully.");
    }

    private static void configureJasperCompilerClasspath() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (!(cl instanceof URLClassLoader urlCl)) {
            System.out.println("[JasperPrecompiler] Context classloader is not a URLClassLoader; "
                    + "using java.class.path as-is.");
            return;
        }

        String classpath = Arrays.stream(urlCl.getURLs())
                .map(url -> {
                    try {
                        return Path.of(url.toURI()).toAbsolutePath().toString();
                    } catch (Exception e) {
                        return url.getFile();
                    }
                })
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(File.pathSeparator));

        if (!classpath.isBlank()) {
            DefaultJasperReportsContext.getInstance()
                    .setProperty("net.sf.jasperreports.compiler.classpath", classpath);
            System.out.println("[JasperPrecompiler] Classpath entries set: "
                    + classpath.split(File.pathSeparator).length);
        }
    }
}
