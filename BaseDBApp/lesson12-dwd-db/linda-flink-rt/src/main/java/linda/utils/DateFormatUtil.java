package linda.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    public static String toDate(Long ts) {
        if (ts == null) {
            return "";
        }
        return DATE_FORMAT_THREAD_LOCAL.get().format(new Date(ts));
    }
}
