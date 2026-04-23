pull image mysql chạy port 3307 user: root pass: 123456


// Cách chạy

### b1: cd thư muc

cd tenthumuc

ví dụ:  cd eureka chạy mvn clean package -DskipTests

### b2 : build image

docker build -t <image-name> .

ví dụ: docker build -t eureka .

### b3 : chạy container

docker run -d -p <host-port>:<container-port> --name <container-name> <image-name>

ví dụ: docker run -d -p 8761:8761 --name eurekaContainer eureka

### nếu có thay đổi thì chạy