FROM openjdk:8
VOLUME /tmp
EXPOSE 8090
ADD ./target/zuul-server.jar zuul-server-image.jar
ENTRYPOINT ["java","-jar","-Dspring.cloud.config.uri=http://config-server:8888","/zuul-server-image.jar"]