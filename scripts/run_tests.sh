#!/bin/sh

# Force delete the log file before Maven runs
rm -f target/logs/log4j.log

echo "Running tests for group: $TEST_GROUP"

# Run TestNG tests for the specified group
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/RegressionScript.xml -Dgroups="$TEST_GROUP" -DTEST_GROUP="$TEST_GROUP"

# Check the exit status of the tests
if [ $? -eq 0 ]; then
    echo "Tests executed successfully for group: $TEST_GROUP"
else
    echo "Tests failed for group: $TEST_GROUP"
    exit 1
fi
