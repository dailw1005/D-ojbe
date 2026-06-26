package com.dailw.utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class TimeFormatUtil {

    private static final ZoneId ASIA_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter ISO_OFFSET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private TimeFormatUtil() {
    }

    public static String toIsoOffsetString(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ASIA_SHANGHAI)
                .format(ISO_OFFSET_FORMATTER);
    }
}
