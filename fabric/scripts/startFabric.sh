#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error
set -e

CHAINCODE_VERSION=1.0
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


# don't rewrite paths for Windows Git Bash users
export MSYS_NO_PATHCONV=1
starttime=$(date +%s)
LANGUAGE=${1:-"golang"}
cd $DIR

# clean the keystore
rm -rf ~/.pig-fabric-store

# launch network; create channel and join peer to channel
cd ../network
./start.sh

# Now launch the CLI container in order to install, instantiate chaincode
# and prime the ledger with our 10 cars
docker-compose -f docker-compose.yml up -d cli

cd ../chaincode
# 1 chaincode
go mod download
go mod vendor
CHAINCODE_NAME=main
CC_SRC_PATH=mingbyte/main
# pack the chaincode
docker exec cli peer chaincode package -n "$CHAINCODE_NAME" -p "$CC_SRC_PATH" -v "$CHAINCODE_VERSION" /home/chaincode/"$CHAINCODE_NAME"-"$CHAINCODE_VERSION".pak
# install
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode install /home/chaincode/"$CHAINCODE_NAME"-"$CHAINCODE_VERSION".pak
# instantiate
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode instantiate -o orderer.example.com:7050 -C mychannel -n "$CHAINCODE_NAME" -l "$LANGUAGE" -v "$CHAINCODE_VERSION" -c '{"function":"init","Args":["'$CHAINCODE_VERSION'"]}' -P "OR ('Org1MSP.member','Org2MSP.member')"

CONTAINER_NAME="dev-peer0.org1.example.com-${CHAINCODE_NAME}"

# 2 check result
CID=$(docker ps -q -f status=running -f name=^/${CONTAINER_NAME})

while [ ! "${CID}" ]; do
    CID=$(docker ps -q -f status=running -f name=^/${CONTAINER_NAME})
    echo "CONTAINER_NAME not found";
    sleep 3;
done;

sleep 3;


printf "\nTotal setup execution time : $(($(date +%s) - starttime)) secs ...\n\n\n"