package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

type Chaincode struct {
}

func (c *Chaincode) Init(stub shim.ChaincodeStubInterface) peer.Response {
	return shim.Success(nil)
}

// chaincode entry
func (c *Chaincode) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	fn, args := stub.GetFunctionAndParameters()

	fmt.Println("-------------------------------------")
	fmt.Println("Invoking:", fn)
	fmt.Println(args)
	fmt.Println("-------------------------------------")

	switch fn {
	case "hello":
		return c.hello(stub, args)
	}

	return peer.Response{
		Status:  404,
		Message: "Received unknown function invocation",
	}
}

func main() {
	err := shim.Start(new(Chaincode))
	if err != nil {
		fmt.Printf("Error starting Energy chaincode: %s ", err)
	}
}

func (c *Chaincode) hello(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	fmt.Println("Hello from chaincode.")

	return shim.Success([]byte("Hello"))
}
