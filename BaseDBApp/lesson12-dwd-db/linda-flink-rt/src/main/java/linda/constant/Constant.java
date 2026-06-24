package linda.constant;

public class Constant {
    public static final String KAFKA_BROKERS =
            "hadoop101:9092,hadoop102:9092,hadoop103:9092";

    public static final String TOPIC_ODS_BASE_LOG = "topic_ods_base_log";
    public static final String TOPIC_ODS_BASE_DB = "topic_ods_base_db";

    public static final String TOPIC_DWD_PAGE_LOG = "dwd_page_log";
    public static final String TOPIC_DWD_START_LOG = "dwd_start_log";
    public static final String TOPIC_DWD_DISPLAY_LOG = "dwd_display_log";
    public static final String TOPIC_DWD_ERROR_LOG = "dwd_error_log";
}
