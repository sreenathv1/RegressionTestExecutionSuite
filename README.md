# RegressionTestExecutionSuite
A utility to execute regression test cases on hbase tables.


# Introduction

This suite is a java component that reads the configured test cases from an input excel file, verifies the expected data and actual data from Hbase and record the details into another excel file. The status and comments of each test case is written to the output file. A Pass/Fail count is also shown at the end of the execution. All the configurable properties are present inside the property file 'regression-test-config.properties'.

# Requirement

The following are the requirements for running the utility.

    CDH 5.1 or above
    HBase
    JRE 1.7 or above


# Execution

The component can be run using the following command


    java -cp <CLASSPATH>:com.sreenath.regressionsuite.execution.TestCaseExecuter


