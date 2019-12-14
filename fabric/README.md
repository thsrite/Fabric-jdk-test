> Based on [Hyperledger Fabric Network boilerplate](https://github.com/wearetheledger/fabric-network-boilerplate)

# Energy Internet

This is a dev fabric network with 1 peer, 1 orderer and 1 CA.
```
peer0.org1.example.com:7051
orderer.example.com:7050
ca.example.com:7054
```

## network state preserve

Both orderers and peers' state locates in `/state` directory, you _MUST NOT_ remove the directory in production.
Regularly backup the whole dir if necessary.

## Starting 

1. Pull all fabric docker images. Pass in a version, or `1.4.2` used as default.
    ```bash
    ./scripts/bootstrap.sh [optional_custom_version]
    ```
2. Automatically clear earlier network, setup a fresh new network, install and instantiate chaincodes on it.
    ```bash
    ./scripts/startFabric.sh
    ```
3. Upgrade chaincode, version is handled automatically.
    ```bash
    ./scripts/upgrade-chaincode.sh
    ```
4. restart
    ```bash
    ./scripts/restart.sh
    ```
5. clear up
    ```bash
    ./script/clearup.sh
    ```


[铭数科技](www.mingbyte.com) \
All rights reserved.
