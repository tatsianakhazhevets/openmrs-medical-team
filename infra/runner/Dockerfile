FROM maven:3.9.11-eclipse-temurin-17

RUN apt-get update \
    && apt-get install --yes --no-install-recommends bash curl jq libxml2-utils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
