package com.legyver.gradle.builddata;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildMetaAlgorithmTest {

    @Test
    public void localDate() {
        BuildMetaAlgorithm buildMetaAlgorithm = BuildMetaAlgorithm.LOCAL_DATE;
        LocalDate localDate = LocalDate.now();
        Properties input = new Properties();

        String result = buildMetaAlgorithm.apply("don't care", input);

        LocalDate parsed = LocalDate.parse(result);
        assertThat(parsed.getYear()).isEqualTo(localDate.getYear());
        assertThat(parsed.getMonthValue()).isEqualTo(localDate.getMonthValue());
        assertThat(parsed.getDayOfMonth()).isEqualTo(localDate.getDayOfMonth());

        PaddedIntegerAsserter paddedIntegerAsserter = new PaddedIntegerAsserter(input);
        paddedIntegerAsserter.assertExpected("compute.day", localDate.getDayOfMonth());
        paddedIntegerAsserter.assertExpected("compute.month-digit", localDate.getMonthValue());

        assertThat(input.getProperty("compute.month-name")).isEqualTo(localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        assertThat(input.getProperty("compute.year")).isEqualTo(String.valueOf(localDate.getYear()));
        assertThat(input.getProperty("compute.iso-local-date")).isNotNull();
    }

    @Test
    public void dateTime() {
        BuildMetaAlgorithm buildMetaAlgorithm = BuildMetaAlgorithm.DATE_TIME;
        LocalDateTime localDateTime = LocalDateTime.now();
        Properties input = new Properties();

        String result = buildMetaAlgorithm.apply("don't care", input);

        LocalDateTime parsed = LocalDateTime.parse(result);
        assertThat(parsed.getYear()).isEqualTo(localDateTime.getYear());
        assertThat(parsed.getMonthValue()).isEqualTo(localDateTime.getMonthValue());
        assertThat(parsed.getDayOfMonth()).isEqualTo(localDateTime.getDayOfMonth());

        PaddedIntegerAsserter paddedIntegerAsserter = new PaddedIntegerAsserter(input);
        paddedIntegerAsserter.assertExpected("compute.day", localDateTime.getDayOfMonth());
        paddedIntegerAsserter.assertExpected("compute.month-digit", localDateTime.getMonthValue());
        paddedIntegerAsserter.assertExpected("compute.hour", localDateTime.getHour());
        paddedIntegerAsserter.assertExpected("compute.minute", localDateTime.getMinute());

        assertThat(input.getProperty("compute.month-name")).isEqualTo(localDateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        assertThat(input.getProperty("compute.year")).isEqualTo(String.valueOf(localDateTime.getYear()));
        assertThat("compute.second").isNotNull();
    }

    @Test
    public void buildNumber() {
        BuildMetaAlgorithm buildMetaAlgorithm = BuildMetaAlgorithm.INCREMENT;

        {
            Properties properties = new Properties();
            String result = buildMetaAlgorithm.apply(null, properties);
            assertThat(result).isEqualTo("0000");
            assertThat(properties.getProperty("compute.build-number")).isEqualTo("0000");
        }
        {
            Properties properties = new Properties();
            String result = buildMetaAlgorithm.apply("", properties);
            assertThat(result).isEqualTo("0000");
            assertThat(properties.getProperty("compute.build-number")).isEqualTo("0000");
        }
        {
            Properties properties = new Properties();
            String result = buildMetaAlgorithm.apply(" ", properties);
            assertThat(result).isEqualTo("0000");
            assertThat(properties.getProperty("compute.build-number")).isEqualTo("0000");
        }
        {
            Properties properties = new Properties();
            String result = buildMetaAlgorithm.apply("0000", properties);
            assertThat(result).isEqualTo("0001");
            assertThat(properties.getProperty("compute.build-number")).isEqualTo("0001");
        }
        {
            Properties properties = new Properties();
            String result = buildMetaAlgorithm.apply("0999", properties);
            assertThat(result).isEqualTo("1000");
            assertThat(properties.getProperty("compute.build-number")).isEqualTo("1000");
        }
        {
            Properties properties = new Properties();
            String result = buildMetaAlgorithm.apply("9999", properties);
            assertThat(result).isEqualTo("0000");
            assertThat(properties.getProperty("compute.build-number")).isEqualTo("0000");
        }
    }

    private static class PaddedIntegerAsserter {
        private final Properties properties;

        private PaddedIntegerAsserter(Properties properties) {
            this.properties = properties;
        }

        public void assertExpected(String name, int expected) {
            String actualUnPadded = properties.getProperty(name);
            assertThat(actualUnPadded).isEqualTo(String.valueOf(expected));

            String actualPadded = properties.getProperty(name + "-2");
            if (expected > 9) {
                assertThat(actualPadded).isEqualTo(String.valueOf(expected));
            } else {
                assertThat(actualPadded).isEqualTo("0" + expected);
            }
        }
    }

}
