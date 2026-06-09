@echo off
cd /d D:\BaiduNetdiskDownload\work\demo\docker\powerjob
java -Dfile.encoding=UTF-8 -Dpowerjob.network.local.address=127.0.0.1 -jar powerjob-server.jar ^
  --spring.profiles.active=daily ^
  --server.port=7700 ^
  --oms.mongodb.enable=false ^
  --spring.datasource.core.jdbc-url=jdbc:mysql://127.0.0.1:3306/powerjob?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=Asia/Shanghai ^
  --spring.datasource.core.username=powerjob ^
  --spring.datasource.core.password=powerjob123 ^
  --oms.storage.dfs.mysql_series.url=jdbc:mysql://127.0.0.1:3306/powerjob?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=Asia/Shanghai ^
  --oms.storage.dfs.mysql_series.username=powerjob ^
  --oms.storage.dfs.mysql_series.password=powerjob123
pause
