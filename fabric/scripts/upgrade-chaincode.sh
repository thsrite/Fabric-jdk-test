#!/usr/bin/env bash

starttime=$(date +%s)

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR
if [ $? -eq 1 ]; then
    exit 1
fi
cd ../chaincode
CHAINCODE_VERSION=$starttime
LANGUAGE=golang

# Enable the go modules feature
export GO111MODULE=on
# Set the GOPROXY environment variable
export GOPROXY=https://goproxy.io
go mod download
go mod vendor
CHAINCODE_NAME=main
CC_SRC_PATH=mingbyte/main
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode install -n "$CHAINCODE_NAME" -v "$CHAINCODE_VERSION" -p "$CC_SRC_PATH" -l "$LANGUAGE"
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode upgrade -o orderer.example.com:7050 -C mychannel -n "$CHAINCODE_NAME" -l "$LANGUAGE" -v "$CHAINCODE_VERSION" -c '{"function":"init","Args":["'$CHAINCODE_VERSION'"]}' -P "OR ('Org1MSP.member','Org2MSP.member')"


printf "\nUpgrade chaincode execution time : $(($(date +%s) - starttime)) secs ...\n\n\n"
echo $CHAINCODE_VERSION
echo "dev-peer0.org1.example.com-$CHAINCODE_NAME-$CHAINCODE_VERSION"

#docker logs dev-peer0.org1.example.com-$CHAINCODE_NAME-$CHAINCODE_VERSION -f
