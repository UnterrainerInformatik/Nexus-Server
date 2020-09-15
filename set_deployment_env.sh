#!/usr/bin/env bash

## This file will be used in the docker-compose.yml file automatically because of its name and location.
## So this is the place where to transfer the CI-variables to docker-compose.
echo "DB_PASSWORD=$DB_PASSWORD" >> .env
echo "DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD" >> .env
echo "NEXUS_SMTP_USER=$NEXUS_SMTP_USER" >> .env
echo "NEXUS_SMTP_PASSWORD=$NEXUS_SMTP_PASSWORD" >> .env