package com.ruoyi.kingdee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 查询安全上限配置，对应 application.yml 中的 kingdee.query。
 */
@Component
@ConfigurationProperties(prefix = "kingdee.query")
public class KingdeeProperties {

    private int maxRowsPerTable = 2000;
    private int maxCandidates = 50;
    private int inClauseBatchSize = 500;

    public int getMaxRowsPerTable() {
        return maxRowsPerTable;
    }

    public void setMaxRowsPerTable(int maxRowsPerTable) {
        this.maxRowsPerTable = maxRowsPerTable;
    }

    public int getMaxCandidates() {
        return maxCandidates;
    }

    public void setMaxCandidates(int maxCandidates) {
        this.maxCandidates = maxCandidates;
    }

    public int getInClauseBatchSize() {
        return inClauseBatchSize;
    }

    public void setInClauseBatchSize(int inClauseBatchSize) {
        this.inClauseBatchSize = inClauseBatchSize;
    }
}
