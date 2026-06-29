package org.example.mockdb.service;

import org.example.mockdb.model.InsertSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AsyncMockRunner {
    private final BusinessMockService businessMockService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<String> logs = Collections.synchronizedList(new ArrayList<String>());

    public AsyncMockRunner(BusinessMockService businessMockService) {
        this.businessMockService = businessMockService;
    }

    public boolean isRunning() {
        return running.get();
    }

    public List<String> latestLogs() {
        synchronized (logs) {
            int from = Math.max(0, logs.size() - 100);
            return new ArrayList<String>(logs.subList(from, logs.size()));
        }
    }

    public boolean startCoreLoop(final int rounds, final long sleepMs) {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        Thread thread = new Thread(() -> {
            try {
                for (int i = 1; i <= rounds && running.get(); i++) {
                    InsertSummary summary = businessMockService.mockCoreAll(1);
                    addLog("第 " + i + " 轮插入完成: " + summary.getTableCounts());
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                }
            } catch (Exception e) {
                addLog("运行失败: " + e.getMessage());
            } finally {
                running.set(false);
                addLog("模拟任务已停止");
            }
        });
        thread.setName("mock-db-runner");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public void stop() {
        running.set(false);
        addLog("收到停止命令");
    }

    private void addLog(String log) {
        logs.add(log);
    }
}
