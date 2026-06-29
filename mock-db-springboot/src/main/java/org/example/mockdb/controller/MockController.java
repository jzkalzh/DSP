package org.example.mockdb.controller;

import org.example.mockdb.config.MockProperties;
import org.example.mockdb.model.ApiResponse;
import org.example.mockdb.model.InsertSummary;
import org.example.mockdb.repository.MetadataRepository;
import org.example.mockdb.service.AsyncMockRunner;
import org.example.mockdb.service.BusinessMockService;
import org.example.mockdb.service.GenericMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mock")
public class MockController {
    private final BusinessMockService businessMockService;
    private final GenericMockService genericMockService;
    private final MetadataRepository metadataRepository;
    private final AsyncMockRunner asyncMockRunner;
    private final MockProperties properties;

    public MockController(BusinessMockService businessMockService,
                          GenericMockService genericMockService,
                          MetadataRepository metadataRepository,
                          AsyncMockRunner asyncMockRunner,
                          MockProperties properties) {
        this.businessMockService = businessMockService;
        this.genericMockService = genericMockService;
        this.metadataRepository = metadataRepository;
        this.asyncMockRunner = asyncMockRunner;
        this.properties = properties;
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("mock-db 服务正常", "OK");
    }

    @GetMapping("/tables")
    public ApiResponse<List<String>> tables() {
        return ApiResponse.ok("查询表成功", metadataRepository.listTables());
    }

    @GetMapping("/count/{table}")
    public ApiResponse<Map<String, Object>> count(@PathVariable String table) {
        return ApiResponse.ok("查询成功", metadataRepository.count(table));
    }

    @PostMapping("/trademark")
    public ApiResponse<InsertSummary> trademark(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("品牌维度数据生成成功", businessMockService.mockBaseTrademark(limit(count)));
    }

    @PostMapping("/base-dic")
    public ApiResponse<InsertSummary> baseDic(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("字典维度数据生成成功", businessMockService.mockBaseDic(limit(count)));
    }

    @PostMapping("/favor")
    public ApiResponse<InsertSummary> favor(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("收藏事实数据生成成功", businessMockService.mockFavorInfo(limit(count)));
    }

    @PostMapping("/cart")
    public ApiResponse<InsertSummary> cart(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("购物车数据生成成功", businessMockService.mockCartInfo(limit(count)));
    }

    @PostMapping("/comment")
    public ApiResponse<InsertSummary> comment(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("评论数据生成成功", businessMockService.mockCommentInfo(limit(count)));
    }

    @PostMapping("/order")
    public ApiResponse<InsertSummary> order(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("订单、明细、支付数据生成成功", businessMockService.mockOrderWithDetailAndPayment(limit(count)));
    }

    @PostMapping("/core-all")
    public ApiResponse<InsertSummary> coreAll(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("核心链路数据生成成功", businessMockService.mockCoreAll(limit(count)));
    }

    /**
     * 通用造数：根据数据库表结构自动生成字段值，适合临时测试任意表。
     */
    @PostMapping("/table/{table}")
    public ApiResponse<Map<String, Object>> table(@PathVariable String table,
                                                  @RequestParam(defaultValue = "1") int count) {
        int inserted = genericMockService.mockTable(table, limit(count));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("table", table);
        result.put("inserted", inserted);
        return ApiResponse.ok("通用表造数成功", result);
    }

    @PostMapping("/default-tables")
    public ApiResponse<InsertSummary> defaultTables(@RequestParam(defaultValue = "1") int count) {
        return ApiResponse.ok("默认表造数成功", genericMockService.mockTables(properties.getDefaultTables(), limit(count)));
    }

    @PostMapping("/run")
    public ApiResponse<String> run(@RequestParam(defaultValue = "100") int rounds,
                                   @RequestParam(required = false) Long sleepMs) {
        long realSleep = sleepMs == null ? properties.getSleepMs() : sleepMs;
        boolean started = asyncMockRunner.startCoreLoop(limit(rounds), realSleep);
        if (!started) {
            return ApiResponse.fail("任务已经在运行中");
        }
        return ApiResponse.ok("后台模拟任务已启动", "rounds=" + rounds + ", sleepMs=" + realSleep);
    }

    @PostMapping("/stop")
    public ApiResponse<String> stop() {
        asyncMockRunner.stop();
        return ApiResponse.ok("已发送停止命令", "STOP");
    }

    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> logs() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("running", asyncMockRunner.isRunning());
        result.put("logs", asyncMockRunner.latestLogs());
        return ApiResponse.ok("查询日志成功", result);
    }

    private int limit(int count) {
        if (count < 1) {
            return 1;
        }
        return Math.min(count, 1000);
    }
}
