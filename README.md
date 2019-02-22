# Book Api

### Travis
[![Build Status](https://travis-ci.com/athenus-rufus/exam-app.svg?branch=master)](https://travis-ci.com/athenus-rufus/exam-app)
### Postman collection
[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/f400859b1ba3d94e4fee)
### Swagger documentation
Once the application is running, swagger documentation can be accessed from [swagger-ui.html](http://localhost:8081/swagger-ui.html#/book-controller)
### Docker
From root directory:
- Run `mvn clean install`
- Build an image `docker build --build-arg JAR_FILE=target/book-1.0-SNAPSHOT.jar .`
- Run image as container `docker run -p 4000:8081 <image id>`