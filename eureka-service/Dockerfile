FROM eclipse-temurin:17-jdk-alpine

#This seeks any .jar file in the target directory
ARG JAR_FILE=target/*.jar

#It's copied and renamed to "app.jar" for standar
COPY ${JAR_FILE} app.jar

#Execute
ENTRYPOINT ["java","-jar","/app.jar"]