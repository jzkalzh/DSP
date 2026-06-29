package org.example.mockdb.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertSummary {
    private int total;
    private Map<String, Integer> tableCounts = new LinkedHashMap<String, Integer>();

    public int getTotal() {
        return total;
    }

    public void add(String table, int count) {
        total += count;
        Integer old = tableCounts.get(table);
        tableCounts.put(table, old == null ? count : old + count);
    }

    public Map<String, Integer> getTableCounts() {
        return tableCounts;
    }

    public void setTableCounts(Map<String, Integer> tableCounts) {
        this.tableCounts = tableCounts;
    }
}
