#!/bin/bash

set -ev

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# don't rewrite paths for Windows Git Bash users
export MSYS_NO_PATHCONV=1
starttime=$(date +%s)
LANGUAGE=${1:-"golang"}
cd $DIR

# launch network; create channel and join peer to channel
cd ../network
docker-compose -f docker-compose.yml down

#remove old images
echo "REMOVE ALL OLD IMAGES"
docker ps -aq -f name=dev- | xargs docker rm | true
docker rmi $(docker images dev-* -q) | true

docker-compose -f docker-compose.yml up -d ca.example.com orderer.example.com peer0.org1.example.com couchdb

# wait for Hyperledger Fabric to start
# incase of errors when running later commands, issue export FABRIC_START_TIMEOUT=<larger number>
export FABRIC_START_TIMEOUT=10
#echo ${FABRIC_START_TIMEOUT}
sleep ${FABRIC_START_TIMEOUT}

# Now launch the CLI container in order to install, instantiate chaincode
# and prime the ledger with our 10 cars
docker-compose -f docker-compose.yml up -d cli

#cd ../
#./scripts/upgrade-chaincode.sh
