FROM adoptopenjdk/openjdk11:latest

RUN apt-get update && \
    apt-get install -y sqlite3 cron

COPY . /anteus
WORKDIR /anteus

RUN chmod +x /anteus/curl.sh
COPY curl.sh /etc/cron.d/curl.sh
COPY cron-job /etc/cron.d/cron-job
RUN /sbin/service cron start
RUN chmod 0644 /etc/cron.d/cron-job
RUN crontab /etc/cron.d/cron-job

RUN touch /var/log/cron.log

EXPOSE 7000

# When the container starts: build, test and run the app. Also starts cron service
CMD cron && ./gradlew build && ./gradlew test && ./gradlew run
