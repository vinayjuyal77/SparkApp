package com.app.spark.utils.date;

import java.util.Date;

/**
 * DateTimeUnits
 * Define units used by {@link DateTimeUtils#getDateDiff(Date, Date, DateTimeUnits)}
 * and also {@link DateTimeUtils#formatDate(long, DateTimeUnits)}
 *
 */
@SuppressWarnings("WeakerAccess")
public enum DateTimeUnits {
    /**
     * Days
     */
    DAYS,
    /**
     * Hours
     */
    HOURS,
    /**
     * Minutes
     */
    MINUTES,
    /**
     * Seconds
     */
    SECONDS,
    /**
     * Milliseconds
     */
    MILLISECONDS,
}
