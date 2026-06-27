package linda.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class StatsController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public String test() {
        return "linda-flink-view ok";
    }

    @GetMapping("/api/visitor/recent")
    public List<Map<String, Object>> visitorRecent() {
        String sql =
                "select stt, edt, vc, ch, ar, is_new, " +
                "sum(pv_ct) pv, sum(uv_ct) uv, sum(sv_ct) sv, sum(uj_ct) uj, sum(dur_sum) dur_sum " +
                "from visitor_stats_2026 " +
                "group by stt, edt, vc, ch, ar, is_new " +
                "order by stt desc " +
                "limit 50";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/api/visitor/total")
    public Map<String, Object> visitorTotal() {
        String sql =
                "select " +
                "sum(pv_ct) pv, " +
                "sum(uv_ct) uv, " +
                "sum(sv_ct) sv, " +
                "sum(uj_ct) uj, " +
                "sum(dur_sum) dur_sum " +
                "from visitor_stats_2026";

        return jdbcTemplate.queryForMap(sql);
    }

    @GetMapping("/api/product/recent")
    public List<Map<String, Object>> productRecent() {
        String sql =
                "select stt, edt, sku_id, sku_name, " +
                "sum(display_ct) display_ct, " +
                "sum(click_ct) click_ct, " +
                "sum(favor_ct) favor_ct, " +
                "sum(cart_ct) cart_ct, " +
                "sum(order_ct) order_ct, " +
                "sum(paid_order_ct) paid_order_ct, " +
                "sum(comment_ct) comment_ct " +
                "from product_stats_2026 " +
                "group by stt, edt, sku_id, sku_name " +
                "order by stt desc " +
                "limit 50";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/api/product/top")
    public List<Map<String, Object>> productTop() {
        String sql =
                "select sku_id, sku_name, " +
                "sum(display_ct) display_ct, " +
                "sum(click_ct) click_ct, " +
                "sum(favor_ct) favor_ct, " +
                "sum(cart_ct) cart_ct, " +
                "sum(order_ct) order_ct, " +
                "sum(paid_order_ct) paid_order_ct " +
                "from product_stats_2026 " +
                "group by sku_id, sku_name " +
                "order by click_ct desc, display_ct desc " +
                "limit 20";

        return jdbcTemplate.queryForList(sql);
    }
}
