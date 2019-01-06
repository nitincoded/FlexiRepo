FROM ubuntu:latest
MAINTAINER Nitin Reddy "redknitin@gmail.com"
EXPOSE 2020
RUN apt-get update
RUN apt-get install -y maven
ADD ./ /flexirepo/
WORKDIR /flexirepo/
CMD sh runme.sh
