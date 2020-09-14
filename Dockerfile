# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:14-alpine
# make the image smaller by deleting some JDK-only files
RUN rm -rf /opt/openjdk-14/jmods && rm -rf /opt/openjdk-14/lib/src.zip
# copy application (with libraries inside)
COPY target/application.jar /
COPY target/libs /libs/
EXPOSE 8080
EXPOSE 8443
# specify default command
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-cp", "/libs/*.jar", "-jar", "application.jar"]
