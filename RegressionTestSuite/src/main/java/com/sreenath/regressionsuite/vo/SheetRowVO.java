package com.sreenath.regressionsuite.vo;

public class SheetRowVO {

    Object serialNo;
    Object testCaseId;
    Object tblName;
    Object rowKeyPrefix;
    Object columnName;
    Object columnFamily;
    Object expectedVal;
    Object actualVal;
    Object comments;
    Object status;

    public Object getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(Object serialNo) {
        this.serialNo = serialNo;
    }

    public Object getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Object testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Object getSasTblName() {
        return tblName;
    }

    public void setSasTblName(Object tblName) {
        this.tblName = tblName;
    }

    public Object getRowKeyPrefix() {
        return rowKeyPrefix;
    }

    public void setRowKeyPrefix(Object rowKeyPrefix) {
        this.rowKeyPrefix = rowKeyPrefix;
    }

    public Object getColumnName() {
        return columnName;
    }

    public void setColumnName(Object columnName) {
        this.columnName = columnName;
    }

    public Object getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(Object columnFamily) {
        this.columnFamily = columnFamily;
    }

    public Object getExpectedVal() {
        return expectedVal;
    }

    public void setExpectedVal(Object expectedVal) {
        this.expectedVal = expectedVal;
    }

    public Object getActualVal() {
        return actualVal;
    }

    public void setActualVal(Object actualVal) {
        this.actualVal = actualVal;
    }

    public Object getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SheetRowVO [serialNo=" + serialNo + ", testCaseId="
                + testCaseId + ", tblName=" + tblName + ", rowKeyPrefix="
                + rowKeyPrefix + ", columnName=" + columnName
                + ", columnFamily=" + columnFamily + ", expectedVal="
                + expectedVal + ", actualVal=" + actualVal + ", comments="
                + comments + ", status=" + status + "]";
    }
}
