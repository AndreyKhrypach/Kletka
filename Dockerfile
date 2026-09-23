FROM debian:trixie

RUN apt update && apt install -y curl maven fakeroot binutils && \
    LIBERICA_URL=$(curl -s "https://api.bell-sw.com/v1/liberica/releases?version-feature=17&os=linux&arch=x86&bitness=64&package-type=deb&bundle-type=jdk-full&version-modifier=latest" \
        | grep -o '"downloadUrl":"[^"]*"' \
        | head -1 \
        | cut -d'"' -f4) && \
    echo "Downloading Liberica from: $LIBERICA_URL" && \
    curl -L -o /tmp/jdk.deb "$LIBERICA_URL" && \
    apt install -y /tmp/jdk.deb && \
    rm /tmp/jdk.deb && \
    rm -rf /var/lib/apt/lists/*