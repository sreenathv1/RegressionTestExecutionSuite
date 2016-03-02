package com.sreenath.regressionsuite.execution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sreenath.regressionsuite.constants.Constants;
import com.sreenath.regressionsuite.hbase.HbaseManager;
import com.sreenath.regressionsuite.helper.ConfigReader;
import com.sreenath.regressionsuite.vo.SheetRowVO;

public class TestCaseExecuter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TestCaseExecuter.class);

    private String tablePrefix = "";
    private String tableSuffix = "";
    private String inputFilePath = "";
    private String outputFilePath = "";
    
    {
        tablePrefix = ConfigReader.getProperty(Constants.TABLE_PREFIX)
                .toUpperCase();
        tableSuffix = ConfigReader.getProperty(Constants.TABLE_SUFFIX)
                .toUpperCase();
        inputFilePath = ConfigReader.getProperty(Constants.INPUT_FILE_PATH);
        outputFilePath = ConfigReader.getProperty(Constants.OUTPUT_FILE_PATH);
    }

    private int passedCount = 0;
    private int failedCount = 0;

    public void executeAllTestCases() {

        List<SheetRowVO> excelRowList = new ArrayList<>();
        Map<String, Integer> indexMap = null;

        try {
            LOGGER.info("Executing Regression Test Cases...");
            LOGGER.info("Input Excel file location : {}\n", inputFilePath);
            FileInputStream inputFile = new FileInputStream(new File(
                    inputFilePath));

            XSSFWorkbook inputWorkbook = new XSSFWorkbook(inputFile);

            XSSFSheet inputSheet = inputWorkbook.getSheetAt(0);

            indexMap = createIndexMap();

            // Load the Excel Sheet rows into a list
            for (int row = 1; null != inputSheet.getRow(row); row++) {
                XSSFRow sheetRow = inputSheet.getRow(row);
                SheetRowVO sheetRowVO = loadExcelDataRow(sheetRow, indexMap);
                executeTestCase(sheetRowVO);
                LOGGER.info("Executing test case no : {}",
                        Math.round((long) sheetRowVO.getSerialNo()));
                excelRowList.add(sheetRowVO);
            }
            inputFile.close();

            HbaseManager.closeHtables();
            // Create and write into Output Excel File
            XSSFWorkbook outputWorkbook = new XSSFWorkbook();

            XSSFSheet outputWSheet = outputWorkbook.createSheet("RegressionTestReport");

            int rowNum = 0;
            Row row = outputWSheet.createRow(rowNum++);
            writeSheetHeaderRow(row, indexMap);

            for (SheetRowVO sheetRowVO : excelRowList) {

                row = outputWSheet.createRow(rowNum++);
                writeDataRow(row, sheetRowVO, indexMap);
            }

            // Write the workbook in file system
            FileOutputStream outputFile = new FileOutputStream(new File(
                    outputFilePath));
            outputWorkbook.write(outputFile);
            outputFile.close();
            LOGGER.info("Test Case Execution Successfully Completed.\n");
            LOGGER.info("Output Excel file location : {}\n", outputFilePath);
            LOGGER.info("----------Summary----------");
            LOGGER.info("Total Testcase count  : {}", passedCount + failedCount);
            LOGGER.info("Passed Testcase count : {}", passedCount);
            LOGGER.info("Failed Testcase count : {}", failedCount);
        } catch (Exception e) {
            LOGGER.error("Regression Test Case Execution Failed!!! ", e);
        }
    }

    private void executeTestCase(SheetRowVO sheetRowVO) throws IOException {

        String colName = (String) sheetRowVO.getColumnName();
        String rowKeyPrefix = (String) sheetRowVO.getRowKeyPrefix();
        String tableName = (String) sheetRowVO.getSasTblName();

        // Appending the prefix and suffix to the table name that was configured
        // in the property file.
        tableName = (tablePrefix + tableName + tableSuffix).trim();
        AtomicBoolean isMultipleRecordsPresent = new AtomicBoolean(false);
        String colFamily = (String) sheetRowVO.getColumnFamily();
        Map<String, String> tableValueMap = HbaseManager
                .rangeScanForLatestRecord(rowKeyPrefix, tableName, colName,
                        colFamily, isMultipleRecordsPresent);
        if (null == tableValueMap) {
            if (Constants.NO_RECORD.equals(sheetRowVO.getExpectedVal())) {
                sheetRowVO.setComments("Record not found.");
                sheetRowVO.setStatus("PASS");
                passedCount++;
            } else {
                sheetRowVO.setComments("Record not found.");
                sheetRowVO.setStatus("FAIL");
                failedCount++;
            }
        } else if (isMultipleRecordsPresent.get()) {
            sheetRowVO.setComments("Multiple records present.");
            sheetRowVO.setStatus("FAIL");
            failedCount++;
        } else {
            String actualValue = tableValueMap.get(colName);
            if (null == actualValue) {
                if (Constants.NO_COLUMN.equals(sheetRowVO.getExpectedVal())) {
                    sheetRowVO.setComments("Column not found.");
                    sheetRowVO.setStatus("PASS");
                    passedCount++;
                } else {
                    sheetRowVO.setComments("Column not found.");
                    sheetRowVO.setStatus("FAIL");
                    failedCount++;
                }
            } else if (actualValue.equals(sheetRowVO.getExpectedVal()
                    .toString())) {
                sheetRowVO.setActualVal(actualValue);
                sheetRowVO.setComments("NIL");
                sheetRowVO.setStatus("PASS");
                passedCount++;
            } else {
                sheetRowVO.setActualVal(actualValue);
                sheetRowVO.setComments("Unexpected value.");
                sheetRowVO.setStatus("FAIL");
                failedCount++;
            }
        }
    }

    private void writeDataRow(Row row, SheetRowVO sheetRowVO,
            Map<String, Integer> indexMap) {

        addCellToRow(row, sheetRowVO.getSerialNo(),
                indexMap.get(Constants.SERIAL_NO_INDEX));

        addCellToRow(row, sheetRowVO.getTestCaseId(),
                indexMap.get(Constants.TEST_CASE_ID_INDEX));

        addCellToRow(row, sheetRowVO.getSasTblName(),
                indexMap.get(Constants.TBL_NAME_INDEX));

        addCellToRow(row, sheetRowVO.getRowKeyPrefix(),
                indexMap.get(Constants.ROW_KEY_PREFIX_INDEX));

        addCellToRow(row, sheetRowVO.getColumnName(),
                indexMap.get(Constants.COLUMN_NAME_INDEX));

        addCellToRow(row, sheetRowVO.getColumnFamily(),
                indexMap.get(Constants.COLUMN_FAMILY_INDEX));

        addCellToRow(row, sheetRowVO.getExpectedVal(),
                indexMap.get(Constants.EXPECTED_VAL_INDEX));

        addCellToRow(row, sheetRowVO.getActualVal(),
                indexMap.get(Constants.ACTUAL_VAL_INDEX));

        addCellToRow(row, sheetRowVO.getComments(),
                indexMap.get(Constants.COMMENTS_INDEX));

        addCellToRow(row, sheetRowVO.getStatus(),
                indexMap.get(Constants.STATUS_INDEX));
    }

    private void addCellToRow(Row row, Object cellValue, Integer index) {
        Cell cell = row.createCell(index);
        if (cellValue instanceof String)
            cell.setCellValue((String) cellValue);
        else if (cellValue instanceof Integer)
            cell.setCellValue((Integer) cellValue);
        else if (cellValue instanceof Double)
            cell.setCellValue((Double) cellValue);
        else if (cellValue instanceof Long)
            cell.setCellValue((Long) cellValue);
    }

    private void writeSheetHeaderRow(Row row, Map<String, Integer> indexMap) {

        addCellToRow(row, "Serial No", indexMap.get(Constants.SERIAL_NO_INDEX));

        addCellToRow(row, "Test Case Id",
                indexMap.get(Constants.TEST_CASE_ID_INDEX));

        addCellToRow(row, "Table Name",
                indexMap.get(Constants.TBL_NAME_INDEX));

        addCellToRow(row, "Row Key Prefix",
                indexMap.get(Constants.ROW_KEY_PREFIX_INDEX));

        addCellToRow(row, "Column Name",
                indexMap.get(Constants.COLUMN_NAME_INDEX));

        addCellToRow(row, "Column Family",
                indexMap.get(Constants.COLUMN_FAMILY_INDEX));

        addCellToRow(row, "Expected Value",
                indexMap.get(Constants.EXPECTED_VAL_INDEX));

        addCellToRow(row, "Actual Value",
                indexMap.get(Constants.ACTUAL_VAL_INDEX));

        addCellToRow(row, "Comments", indexMap.get(Constants.COMMENTS_INDEX));

        addCellToRow(row, "Status", indexMap.get(Constants.STATUS_INDEX));
    }

    private static Map<String, Integer> createIndexMap() {
        Map<String, Integer> indexMap = new HashMap<String, Integer>();
        indexMap.put(Constants.SERIAL_NO_INDEX, Integer.parseInt(ConfigReader
                .getProperty(Constants.SERIAL_NO_INDEX)));
        indexMap.put(Constants.TEST_CASE_ID_INDEX, Integer
                .parseInt(ConfigReader
                        .getProperty(Constants.TEST_CASE_ID_INDEX)));
        indexMap.put(Constants.TBL_NAME_INDEX, Integer
                .parseInt(ConfigReader
                        .getProperty(Constants.TBL_NAME_INDEX)));
        indexMap.put(Constants.ROW_KEY_PREFIX_INDEX, Integer
                .parseInt(ConfigReader
                        .getProperty(Constants.ROW_KEY_PREFIX_INDEX)));
        indexMap.put(Constants.COLUMN_NAME_INDEX, Integer.parseInt(ConfigReader
                .getProperty(Constants.COLUMN_NAME_INDEX)));
        indexMap.put(Constants.COLUMN_FAMILY_INDEX, Integer
                .parseInt(ConfigReader
                        .getProperty(Constants.COLUMN_FAMILY_INDEX)));
        indexMap.put(Constants.EXPECTED_VAL_INDEX, Integer
                .parseInt(ConfigReader
                        .getProperty(Constants.EXPECTED_VAL_INDEX)));
        indexMap.put(Constants.ACTUAL_VAL_INDEX, Integer.parseInt(ConfigReader
                .getProperty(Constants.ACTUAL_VAL_INDEX)));
        indexMap.put(Constants.COMMENTS_INDEX, Integer.parseInt(ConfigReader
                .getProperty(Constants.COMMENTS_INDEX)));
        indexMap.put(Constants.STATUS_INDEX, Integer.parseInt(ConfigReader
                .getProperty(Constants.STATUS_INDEX)));

        return indexMap;
    }

    private static SheetRowVO loadExcelDataRow(XSSFRow sheetRow,
            Map<String, Integer> indexMap) {

        SheetRowVO sheetRowVO = new SheetRowVO();

        Cell cell = sheetRow.getCell(indexMap.get(Constants.SERIAL_NO_INDEX));
        if (null != cell) {
            sheetRowVO.setSerialNo(getCellValueAsString(cell));
        }
        cell = sheetRow.getCell(indexMap.get(Constants.TEST_CASE_ID_INDEX));
        if (null != cell) {
            sheetRowVO.setTestCaseId(getCellValueAsString(cell));
        }
        cell = sheetRow.getCell(indexMap.get(Constants.TBL_NAME_INDEX));
        if (null != cell) {
            sheetRowVO.setSasTblName(getCellValueAsString(cell));
        }
        cell = sheetRow.getCell(indexMap.get(Constants.ROW_KEY_PREFIX_INDEX));
        if (null != cell) {
            sheetRowVO.setRowKeyPrefix(getCellValueAsString(cell));
        }
        cell = sheetRow.getCell(indexMap.get(Constants.COLUMN_NAME_INDEX));
        if (null != cell) {
            sheetRowVO.setColumnName(getCellValueAsString(cell));
        }
        cell = sheetRow.getCell(indexMap.get(Constants.COLUMN_FAMILY_INDEX));
        if (null != cell) {
            sheetRowVO.setColumnFamily(getCellValueAsString(cell));
        }
        cell = sheetRow.getCell(indexMap.get(Constants.EXPECTED_VAL_INDEX));
        if (null != cell) {
            sheetRowVO.setExpectedVal(getCellValueAsString(cell));
        }
        return sheetRowVO;
    }

    private static Object getCellValueAsString(Cell cell) {
        Object value = "";
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_NUMERIC:
            value = cell.getNumericCellValue();
            String decVal = (value + "").split("\\.")[1];
            if (decVal.equals("0")) {
                value = Math.round((Double) value);
            }
            break;
        case Cell.CELL_TYPE_STRING:
            value = cell.getStringCellValue();
            break;
        }
        return value;
    }

    public static void main(String[] args) {
        TestCaseExecuter testCaseExecuter = new TestCaseExecuter();
        testCaseExecuter.executeAllTestCases();
    }
}