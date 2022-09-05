package com.legyver.gradle.builddata;

import com.legyver.utils.propl.PropertyList;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Properties;

public enum BuildMetaAlgorithm {
    LOCAL_DATE {
        @Override
        public String apply(String oldBuildMeta, Properties input) {
            LocalDate localDate = LocalDate.now();

            input.put("compute.year", Integer.valueOf(localDate.getYear()).toString());
            input.put("compute.month-name", localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
            BuildMetaAlgorithm.putAndLeftPad("compute.month-digit", localDate.getMonthValue(), input);
            BuildMetaAlgorithm.putAndLeftPad("compute.day", localDate.getDayOfMonth(), input);
            input.put("compute.iso-local-date", localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return input.getProperty("compute.iso-local-date");
        }
    },
    DATE_TIME {
        @Override
        public String apply(String oldBuildMeta, Properties input) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            LOCAL_DATE.apply(oldBuildMeta, input);

            BuildMetaAlgorithm.putAndLeftPad("compute.hour", zonedDateTime.getHour(), input);
            BuildMetaAlgorithm.putAndLeftPad("compute.minute", zonedDateTime.getMinute(), input);
            BuildMetaAlgorithm.putAndLeftPad("compute.second", zonedDateTime.getSecond(), input);

            input.put("compute.iso-local-date-time", zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            input.put("compute.iso-zoned-date-time", zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            input.put("compute.iso-offset-date-time", zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            input.put("compute.rfc-1123-date-time", zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            return input.getProperty("compute.iso-local-date-time");
        }
    },
    INCREMENT {
        @Override
        public String apply(String oldBuildMeta, Properties input) {
            Integer initial = 0;
            if (oldBuildMeta != null && !oldBuildMeta.isBlank()) {
                initial = Integer.valueOf(oldBuildMeta);
                initial += 1;
                initial = initial % 10000;
            }
            String number = StringUtils.leftPad(initial.toString(), 4, "0");
            input.put("compute.build-number", number);
            return number;
        }
    };

    public abstract String apply(String oldBuildMeta, Properties input);

    private static void putAndLeftPad(String key, int value, Properties input) {
        String sVal = String.valueOf(value);
        input.put(key, sVal);
        input.put(key + "-2", StringUtils.leftPad(sVal, 2, "0"));
    }
}
