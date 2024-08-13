package org.example;

import java.time.*;

public class Main {

    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    public static void main(String[] args) {

        LocalTime time = LocalTime.of(8, 30);
        final Instant currentTime = ZonedDateTime.of(
                LocalDate.of(2019, Month.FEBRUARY, 8),
                time,
                SINGAPORE).toInstant();

        final Clock clock = Clock.fixed(currentTime, SINGAPORE);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(clock);
        System.out.println(zonedDateTime);

    }
}
