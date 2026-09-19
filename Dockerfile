FROM debian:trixie

RUN apt update && apt install -y curl maven fakeroot binutils && \
    curl -L -o /tmp/jdk.deb "https://download.bell-sw.com/java/17.0.20.1+1/bellsoft-jdk17.0.20.1+1-linux-amd64-full.deb" && \
    apt install -y /tmp/jdk.deb && \
    rm /tmp/jdk.deb && \
    rm -rf /var/lib/apt/lists/*
