##!/bin/bash

BUILD_JAR=$(ls /home/ubuntu/app/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build 파일명: $JAR_NAME" >> /home/ubuntu/deploy.log

echo ">>> build 파일 복사" >> /home/ubuntu/deploy.log
DEPLOY_PATH=/home/ubuntu/app/
cp $BUILD_JAR $DEPLOY_PATH

echo ">>> 현재 실행중인 애플리케이션 pid 확인 후 일괄 종료" >> /home/ubuntu/deploy.log
sudo ps -ef | grep java | awk '{print $2}' | xargs kill -15

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo ">>> DEPLOY_JAR 배포"    >> /home/ubuntu/deploy.log
echo ">>> $DEPLOY_JAR의 $JAR_NAME를 실행합니다" >> /home/ubuntu/deploy.log
nohup java -jar $DEPLOY_JAR >> /home/ubuntu/deploy.log 2> /home/ubuntu/deploy_err.log &

## backup
##BUILD_JAR=$(ls /home/ubuntu/app/build/libs/*.jar)
##JAR_NAME=$(basename $BUILD_JAR)
##echo ">>> build 파일명: $JAR_NAME" >> /home/ubuntu/deploy.log
##
##echo ">>> build 파일 복사" >> /home/ubuntu/deploy.log
##DEPLOY_PATH=/home/ubuntu/
##cp $BUILD_JAR $DEPLOY_PATH
##
##echo ">>> 현재 실행중인 애플리케이션 pid 확인" >> /home/ubuntu/deploy.log
##CURRENT_PID=$(pgrep -f $JAR_NAME)
##
##if [ -z $CURRENT_PID ]
##then
##  echo ">>> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ubuntu/deploy.log
##else
##  echo ">>> kill -15 $CURRENT_PID"
##  kill -15 $CURRENT_PID
##  sleep 5
##fi
##
##DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
##echo ">>> DEPLOY_JAR 배포"    >> /home/ubuntu/deploy.log
##nohup java -jar $DEPLOY_JAR >> /home/ubuntu/deploy.log 2> /home/ubuntu/deploy_err.log &