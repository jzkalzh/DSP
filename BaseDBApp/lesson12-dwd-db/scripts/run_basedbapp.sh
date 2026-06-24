#!/bin/bash

APP_HOME=/opt/module/DSP/BaseDBApp/lesson12-dwd-db/linda-flink-rt
JAR_PATH=$APP_HOME/target/linda-flink-rt-0.0.1-SNAPSHOT-jar-with-dependencies.jar

echo "========== start dependencies =========="
zk.sh start
kafka.sh start
/opt/module/maxwell-1.25.0/maxwell.sh start
/opt/module/hbase/bin/start-hbase.sh

echo "========== build project =========="
cd $APP_HOME
mvn clean package -DskipTests

echo "========== submit BaseDBApp =========="
flink run -d \
-c linda.app.dwd.BaseDBApp \
$JAR_PATH
