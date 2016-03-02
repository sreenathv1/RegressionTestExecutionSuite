package com.sreenath.regressionsuite.hbase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HbaseManager {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HbaseManager.class);

    private static Map<String, HTable> hTableMap = null;
    private static Configuration conf = null;

    static {
        conf = HBaseConfiguration.create();
        hTableMap = new HashMap<String, HTable>();
    }

    public static Map<String, String> rangeScanForLatestRecord(String rowKey,
            String tableName, String colName, String colFamily,
            AtomicBoolean isMultipleRecordsPresent) throws IOException {
        Map<String, String> tableValueMap = null;
        ResultScanner resultScanner = null;
        try {

            HTable hTable = hTableMap.get(tableName);
            if (null == hTable) {
                hTable = new HTable(conf, tableName);
                hTableMap.put(tableName, hTable);
            }

            byte[] rowKeyBytes = Bytes.toBytes(rowKey);
            byte[] startRowBytes = rowKeyBytes.clone();
            byte[] endRowBytes = rowKeyBytes.clone();

            int lengthOfRowKey = endRowBytes.length;
            byte lsb = endRowBytes[lengthOfRowKey - 1];
            lsb++;
            endRowBytes[lengthOfRowKey - 1] = lsb;

            Scan scan = new Scan(startRowBytes, endRowBytes);
            scan.addFamily(Bytes.toBytes(colFamily));

            resultScanner = hTable.getScanner(scan);

            int recCount = 0;
            for (Result r = resultScanner.next(); null != r; r = resultScanner
                    .next()) {
                if (++recCount > 1) {
                    isMultipleRecordsPresent.set(true);
                    break;
                }
                tableValueMap = new HashMap<String, String>();
                for (Cell cell : r.rawCells()) {
                    tableValueMap.put(
                            Bytes.toString(CellUtil.cloneQualifier(cell)),
                            Bytes.toString(CellUtil.cloneValue(cell)));
                }
            }
        } catch (IOException e) {
            throw new IOException(
                    "Exception during range scan for hbase record.", e);

        } finally {
            if (null != resultScanner) {
                resultScanner.close();
            }
        }
        return tableValueMap;
    }

    public static void closeHtables() {

        for (Map.Entry<String, HTable> entry : hTableMap.entrySet()) {
            HTable hTable = entry.getValue();
            try {
                hTable.close();
            } catch (IOException e) {
                LOGGER.error("Exception while closing hTables", e);
            }
        }
    }
}
