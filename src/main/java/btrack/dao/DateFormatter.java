package btrack.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface DateFormatter {

    static String isoDate(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    String localDate(LocalDateTime dt);
}
