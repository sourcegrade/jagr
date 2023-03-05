FROM gradle:latest AS build

WORKDIR /home/gradle/src
COPY  . .
#RUN gradle build --no-daemon

#FROM openjdk:8-jre-alpine
#WORKDIR /app
#COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar

#ENTRYPOINT ["java", "-jar", "app.jar"]
