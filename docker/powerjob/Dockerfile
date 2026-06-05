FROM powerjob/powerjob-server:latest
# PowerJob 官方镜像缺 MySQL 驱动，手动加上
COPY mysql-connector-j-8.0.33.jar /powerjob-server.jar
RUN jar uf /powerjob-server.jar BOOT-INF/lib/mysql-connector-j-8.0.33.jar
