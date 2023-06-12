package com.vanillage.raytraceantixray.util;

import java.util.concurrent.TimeUnit;

/**
 * @author TauCubed - nullvoxel@gmail.com
 */
public class TimeFormatting {

    public static final long DAY_NANOS = 86_400_000_000_000L;
    public static final long HOUR_NANOS = 3_600_000_000_000L;
    public static final long MINUTE_NANOS = 60_000_000_000L;
    public static final long SECOND_NANOS = 1_000_000_000L;
    public static final long MILLISECOND_NANOS = 1_000_000L;
    public static final long MICROSECOND_NANOS = 1_000L;

    public static String format(TimeUnit unit, long l) {
        return formatNanos(unit.toNanos(l));
    }

    public static String format(TimeUnit unit, long l, TimeUnit start) {
        return formatNanos(unit.toNanos(l), start);
    }

    public static String format(TimeUnit unit, long l, TimeUnit start, TimeUnit end) {
        return formatNanos(unit.toNanos(l), start, end);
    }

    public static String formatNanos(long nanos) {
        return formatNanos(nanos, TimeUnit.NANOSECONDS);
    }

    public static String formatNanos(long nanos, TimeUnit start) {
        return formatNanos(nanos, start, TimeUnit.DAYS);
    }

    public static String formatNanos(long nanos, TimeUnit start, TimeUnit end) {
        int startId = toUnitId(start);
        int endId = toUnitId(end);

        StringBuilder builder = new StringBuilder();

        if (endId > 5) {
            if (startId <= 6 && (nanos / DAY_NANOS > 0 || startId == 6)) {
                builder.append(nanos / DAY_NANOS).append(" d");
                if (startId < 6) {
                    builder.append(", ");
                }
            }
            nanos %= DAY_NANOS;
        }

        if (endId > 4) {
            if (startId <= 5 && (nanos / HOUR_NANOS > 0 || startId == 5)) {
                builder.append(nanos / HOUR_NANOS).append(" h");
                if (startId < 5) {
                    builder.append(", ");
                }
            }
            nanos %= HOUR_NANOS;
        }

        if (endId > 3) {
            if (startId <= 4 && (nanos / MINUTE_NANOS > 0 || startId == 4)) {
                builder.append(nanos / MINUTE_NANOS).append(" m");
                if (startId < 4) {
                    builder.append(", ");
                }
            }
            nanos %= MINUTE_NANOS;
        }

        if (endId > 2) {
            if (startId <= 3 && (nanos / SECOND_NANOS > 0 || startId == 3)) {
                builder.append(nanos / SECOND_NANOS).append(" s");
                if (startId < 3) {
                    builder.append(", ");
                }
            }
            nanos %= SECOND_NANOS;
        }

        if (endId > 1) {
            if (startId <= 2 && (nanos / MILLISECOND_NANOS > 0 || startId == 2)) {
                builder.append(nanos / MILLISECOND_NANOS).append(" ms");
                if (startId < 2) {
                    builder.append(", ");
                }
            }
            nanos %= MILLISECOND_NANOS;
        }

        if (endId > 0) {
            if (startId <= 1 && (nanos / MICROSECOND_NANOS > 0 || startId == 1)) {
                builder.append(nanos / MICROSECOND_NANOS).append(" µs");
                if (startId < 1) {
                    builder.append(", ");
                }
            }
            nanos %= MICROSECOND_NANOS;
        }

        if (startId == 0) {
            builder.append(nanos).append(" ns");
        }

        return builder.toString();
    }

    private static int toUnitId(TimeUnit src) {
        switch (src) {
            case NANOSECONDS:
                return 0;
            case MICROSECONDS:
                return 1;
            case MILLISECONDS:
                return 2;
            case SECONDS:
                return 3;
            case MINUTES:
                return 4;
            case HOURS:
                return 5;
            case DAYS:
                return 6;
            default:
                return 7;
        }
    }

}
