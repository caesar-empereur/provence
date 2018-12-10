fuser -k -n tcp 8080
sleep 3
cd /app/project/hbase-model/app/build/libs
nohup java -jar app.war --logging.file=/app/project/hbase-model/log/hbase-model.log > /dev/null &
