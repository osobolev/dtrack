package btrack.web.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface DateFormatter {

    static String isoDate(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    String localDate(LocalDateTime dt);
}
