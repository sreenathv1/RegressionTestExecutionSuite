# RegressionTestExecutionSuite
A utility to execute regression test cases on hbase tables.


# Functionality

This suite is a java component that reads the configured test cases from an input excel file, verifies the expected data and actual data from HBase and record the details into another excel file. A Pass/Fail count is also shown at the end of the execution. All the configurable properties are present inside the property file 'regression-test-config.properties'.

The column positions in the input excel file can be configured.

The input and output file paths are also configurable.

The Actual Value, Comments and Status of each test case will be updated in the output Excel file as per the test case.

If the particular record is not expected to be present in the hbase table, the expected value should be given as 'NO RECORD'

If the particular column is not expected to be present in the hbase table, the expected value should be given as 'NO COLUMN'


# Requirement

The following are the requirements for running the utility.

    CDH 5.1 or above
    HBase
    JRE 1.7 or above


# Execution

The component can be run using the following command


    java -cp <CLASSPATH>:com.sreenath.regressionsuite.execution.TestCaseExecuter


