package linda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ProvinceKeywordController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/province/top")
    public List<Map<String, Object>> provinceTop() {
        try {
            String sql =
                    "select " +
                    "province_id, " +
                    "any(province_name) as province_name, " +
                    "sum(order_amount) as order_amount, " +
                    "sum(order_count) as order_count " +
                    "from province_stats_2026 " +
                    "group by province_id " +
                    "order by order_amount desc " +
                    "limit 50";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @GetMapping("/keyword/top")
    public List<Map<String, Object>> keywordTop() {
        try {
            String sql =
                    "select " +
                    "keyword, " +
                    "source, " +
                    "sum(ct) as ct " +
                    "from keyword_stats_2026 " +
                    "group by keyword, source " +
                    "order by ct desc " +
                    "limit 50";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
