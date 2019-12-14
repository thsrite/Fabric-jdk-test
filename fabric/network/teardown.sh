#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error, print all commands.
set -e

# Shut down the Docker containers for the system tests.
docker-compose -f docker-compose.yml kill && docker-compose -f docker-compose.yml down

# remove the local state
rm -rf ~/.pig-fabric-store/*

# remove chaincode docker containers
docker rm $(docker container ls -aqf name=dev-peer0.org1.example.com-pig-fabric-)

# remove chaincode docker images
docker rmi $(docker images dev-peer0.org1.example.com-pig-fabric-* -q)

# Your system is now clean
