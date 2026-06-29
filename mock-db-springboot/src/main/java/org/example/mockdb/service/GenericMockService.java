package org.example.mockdb.service;

import org.example.mockdb.model.ColumnMeta;
import org.example.mockdb.model.InsertSummary;
import org.example.mockdb.repository.MetadataRepository;
import org.example.mockdb.repository.MockJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
public class GenericMockService {
    private final MetadataRepository metadataRepository;
    private final MockJdbcRepository mockJdbcRepository;
    private final GenericValueFactory valueFactory;

    public GenericMockService(MetadataRepository metadataRepository,
                              MockJdbcRepository mockJdbcRepository,
                              GenericValueFactory valueFactory) {
        this.metadataRepository = metadataRepository;
        this.mockJdbcRepository = mockJdbcRepository;
        this.valueFactory = valueFactory;
    }

    public int mockTable(String table, int count) {
        if (!metadataRepository.tableExists(table)) {
            throw new IllegalArgumentException("数据库中不存在表: " + table);
        }
        List<ColumnMeta> columns = metadataRepository.describe(table);
        int total = 0;
        for (int i = 0; i < count; i++) {
            LinkedHashMap<String, Object> row = valueFactory.createRow(columns);
            mockJdbcRepository.insert(table, row);
            total++;
        }
        return total;
    }

    public InsertSummary mockTables(List<String> tables, int countEach) {
        InsertSummary summary = new InsertSummary();
        for (String table : tables) {
            int count = mockTable(table, countEach);
            summary.add(table, count);
        }
        return summary;
    }
}
