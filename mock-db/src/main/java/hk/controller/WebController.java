package hk.controller;

import hk.bean.AppMain;
import hk.config.AppConfig;
import hk.core.Mocker;
import hk.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Controller
public class WebController {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    private List<String> recentLogs = new ArrayList<>();
    private boolean isRunning = false;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mockType", AppConfig.mock_type);
        model.addAttribute("mockUrl", AppConfig.mock_url);
        model.addAttribute("mockCount", AppConfig.mock_count);
        model.addAttribute("maxMid", AppConfig.max_mid);
        model.addAttribute("maxUid", AppConfig.max_uid);
        model.addAttribute("maxSkuId", AppConfig.max_sku_id);
        model.addAttribute("pageDuringMax", AppConfig.page_during_max_ms);
        model.addAttribute("errorRate", AppConfig.error_rate);
        model.addAttribute("logSleep", AppConfig.log_sleep);
        model.addAttribute("isRunning", isRunning);
        model.addAttribute("logs", recentLogs);
        return "index";
    }

    @PostMapping("/update-config")
    @ResponseBody
    public Map<String, Object> updateConfig(
            @RequestParam String mockType,
            @RequestParam String mockUrl,
            @RequestParam Integer mockCount,
            @RequestParam Integer maxMid,
            @RequestParam Integer maxUid,
            @RequestParam Integer maxSkuId,
            @RequestParam Integer pageDuringMax,
            @RequestParam Integer errorRate,
            @RequestParam Integer logSleep) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            AppConfig.mock_type = mockType;
            AppConfig.mock_url = mockUrl;
            AppConfig.mock_count = mockCount;
            AppConfig.max_mid = maxMid;
            AppConfig.max_uid = maxUid;
            AppConfig.max_sku_id = maxSkuId;
            AppConfig.page_during_max_ms = pageDuringMax;
            AppConfig.error_rate = errorRate;
            AppConfig.log_sleep = logSleep;
            result.put("success", true);
            result.put("message", "配置更新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置更新失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/run")
    @ResponseBody
    public Map<String, Object> runMock() {
        Map<String, Object> result = new HashMap<>();
        if (isRunning) {
            result.put("success", false);
            result.put("message", "模拟任务正在运行中，请等待完成");
            return result;
        }
        
        isRunning = true;
        recentLogs.clear();
        
        new Thread(() -> {
            try {
                for (int i = 0; i < AppConfig.mock_count; i++) {
                    if (!isRunning) break;
                    Mocker mocker = new Mocker();
                    List<AppMain> appMainList = mocker.doAppMock();
                    for (AppMain appMain : appMainList) {
                        String logStr = appMain.toString();
                        recentLogs.add(0, logStr);
                        if (recentLogs.size() > 100) {
                            recentLogs.remove(recentLogs.size() - 1);
                        }
                        log.info(logStr);
                        if(AppConfig.mock_type.equals("http")){
                            try {
                                HttpUtil.post(logStr);
                            } catch (Exception e) {
                                recentLogs.add(0, "发送失败: " + e.getMessage());
                            }
                        }
                        Thread.sleep(AppConfig.log_sleep);
                    }
                }
            } catch (Exception e) {
                log.error("模拟任务执行失败", e);
                recentLogs.add(0, "执行失败: " + e.getMessage());
            } finally {
                isRunning = false;
            }
        }).start();
        
        result.put("success", true);
        result.put("message", "模拟任务已启动");
        return result;
    }

    @PostMapping("/stop")
    @ResponseBody
    public Map<String, Object> stopMock() {
        Map<String, Object> result = new HashMap<>();
        isRunning = false;
        result.put("success", true);
        result.put("message", "模拟任务已停止");
        return result;
    }

    @GetMapping("/logs")
    @ResponseBody
    public Map<String, Object> getLogs() {
        Map<String, Object> result = new HashMap<>();
        result.put("logs", recentLogs);
        result.put("isRunning", isRunning);
        return result;
    }
}