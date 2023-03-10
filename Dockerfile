FROM gradle:latest AS build

WORKDIR /home/gradle/src
COPY  . .
RUN gradle :jagr-worker:shadowJar --no-daemon

FROM openjdk:19-jdk-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/worker/build/libs/*.jar /app/app.jar

ADD https://github.com/openfaas/faas/releases/download/0.18.10/fwatchdog /usr/bin
RUN chmod +x /usr/bin/fwatchdog

ENV fprocess="java -jar app.jar"

ENV exec_timeout="0"

ENV write_timeout="300"
ENV read_timeout="300"

CMD ["fwatchdog"]
