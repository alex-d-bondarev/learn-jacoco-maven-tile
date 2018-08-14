package com.learn.tile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Compare previous minimum jacoco coverage value with this build value.
 * Update minimum coverage value in case it was increased.
 *
 * This process is done in order to ensure that code test coverage is always increasing,
 * but not stuck on hardcoded level. For example:
 * 1. At project start code test coverage can be 10%;
 * 2. Once new tests are added it may be increased to 15%;
 * 3. When new feature is added it can miss new tests but still meet 10% level;
 * 4. Increasing minimum limit after step#2 will result in new tests within step#3.
 *
 */
public class JacocoCoverage {

    // TODO: Read from String args[]
    private Optional<String> totalCoverage;

    private String buildPropertiesPath = "./build.properties";
    private String propertyName = "jacoco.test.instruction";
    private String pathToJacocoHTML = "./target/jacoco-ut/index.html";
    private String totalCoverageTagCSSSelector = "tfoot > tr:first-child > td:first-child + td + td";

    public static void main(String args[]) {
       JacocoCoverage jc = new JacocoCoverage();
       jc.run();
    }

    private void run(){
        totalCoverage = getCoverageFromJacocoHTML(totalCoverageTagCSSSelector, pathToJacocoHTML);
        totalCoverage.ifPresent(coverage -> saveNewCoverageTo(coverage ,buildPropertiesPath));
    }

    private void saveNewCoverageTo(String coverage, String filePath){
        Properties props = new Properties();
        props.setProperty(propertyName, parseCoverageValue(coverage));

        try {
            FileOutputStream out = new FileOutputStream(filePath);
            props.store(out, "Build test coverage");
            out.close();
        }
        catch (IOException ex){
            System.out.println("Failed to save new property to " + filePath);
            ex.printStackTrace();
        }
    }

    private String parseCoverageValue(String totalCoverage){
        String onlyDigits = totalCoverage.replace("%", "");
        return  String.valueOf(
                Double.parseDouble(onlyDigits) / 100);
    }

    private Optional<String> getCoverageFromJacocoHTML(String cssSelector, String jacocoHTML){
        try{
            String html = new String(Files.readAllBytes(Paths.get(jacocoHTML)));
            Document doc = Jsoup.parse(html);
            Element tdWithTotal = doc.body().select(cssSelector).first();
            return Optional.of(tdWithTotal.text());
        }
        catch(IOException ex) {
            System.out.println("Failed to open " + jacocoHTML);
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
