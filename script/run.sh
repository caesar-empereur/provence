fuser -k -n tcp 8080
sleep 3
cd /app/project/hbase-model/build/libs
nohup java -jar hbase.war --logging.file=/app/project/hbase-model/log/hbase-model.log > /dev/null &
