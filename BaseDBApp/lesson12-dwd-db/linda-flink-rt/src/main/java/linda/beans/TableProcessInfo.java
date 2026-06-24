package linda.beans;

import java.io.Serializable;

public class TableProcessInfo implements Serializable {

    private String sourceTable;
    private String operateType;
    private String sinkType;
    private String sinkTable;
    private String sinkColumns;
    private String sinkPk;
    private String sinkExtend;

    public TableProcessInfo() {
    }

    public TableProcessInfo(String sourceTable, String operateType, String sinkType,
                            String sinkTable, String sinkColumns, String sinkPk, String sinkExtend) {
        this.sourceTable = sourceTable;
        this.operateType = operateType;
        this.sinkType = sinkType;
        this.sinkTable = sinkTable;
        this.sinkColumns = sinkColumns;
        this.sinkPk = sinkPk;
        this.sinkExtend = sinkExtend;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getSinkType() {
        return sinkType;
    }

    public void setSinkType(String sinkType) {
        this.sinkType = sinkType;
    }

    public String getSinkTable() {
        return sinkTable;
    }

    public void setSinkTable(String sinkTable) {
        this.sinkTable = sinkTable;
    }

    public String getSinkColumns() {
        return sinkColumns;
    }

    public void setSinkColumns(String sinkColumns) {
        this.sinkColumns = sinkColumns;
    }

    public String getSinkPk() {
        return sinkPk;
    }

    public void setSinkPk(String sinkPk) {
        this.sinkPk = sinkPk;
    }

    public String getSinkExtend() {
        return sinkExtend;
    }

    public void setSinkExtend(String sinkExtend) {
        this.sinkExtend = sinkExtend;
    }
}
