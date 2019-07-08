package btrack.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface DateFormatter {

    static String isoDate(LocalDateTime dt) {
        // todo: strip leading zeroes???
        return dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    String localDate(LocalDateTime dt);
}
