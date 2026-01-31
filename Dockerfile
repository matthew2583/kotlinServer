FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*-all.jar app.jar
COPY sample-data/ ./sample-data/
EXPOSE 9000
ENTRYPOINT [ "java", "-jar", "app.jar" ]
CMD ["--shipments-file=sample-data/swg.csv", \
     "--trucks-file=sample-data/trucks.csv", \
     "--employees-file=sample-data/employees.csv", \
     "--port=9000", \
     "--secret=my-secret"]