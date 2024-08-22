FROM openjdk:21-bookworm AS build
WORKDIR /app
COPY LICENSE authors build.gradle.kts gradlew gradlew.bat logo.svg settings.gradle.kts version ./
# build-logic buildSrc core docs grader-api gradle launcher src
COPY build-logic build-logic
COPY buildSrc buildSrc
COPY core core
COPY docs docs
COPY grader-api grader-api
COPY gradle gradle
COPY launcher launcher
COPY src src
RUN ./gradlew assemble --no-daemon --console=verbose --stacktrace

FROM openjdk:21-bookworm AS run
# add user and group
RUN addgroup --system --gid 1000 jagr
RUN adduser --system --uid 1000 --ingroup jagr jagr
# install python3 and requests
RUN apt-get update && apt-get install -y python3 python3-requests python3-colorama
# install binaries
COPY --from=build /app/build/libs/*.jar /usr/share/jagr/jagr.jar
RUN echo "exec java -jar /usr/share/jagr/jagr.jar $@" > /usr/bin/jagr
RUN chmod +x /usr/bin/jagr
COPY lab-runner.py /usr/share/jagr/lab-runner.py
RUN echo "exec python3 /usr/share/jagr/lab-runner.py $@" > /usr/bin/lab-runner
# setup workdir
RUN chmod +x /usr/bin/lab-runner
RUN mkdir /job && chown jagr:jagr /job
USER jagr
WORKDIR /job
CMD ["python3", "/usr/share/jagr/lab-runner.py"]
