package com.lyfing.demo.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author lyfing
 */
public final class CommonUtils {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 首字母大写
     */
    public static String upper1st(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String lower1st(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    public static Date string2DateTime(String dateTimeStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FMT);
        return new Date(java.sql.Timestamp.valueOf(localDateTime).getTime());
    }

}
