package main

import (
	"fmt"
	"net"

	sv "github.com/Vancheszz/android-agent/internal/server"
)

func main() {
	port := ":9999"

	listener, err := net.Listen("tcp", port)
	if err != nil {
		fmt.Printf("Statring server error :%v\n", err)
	}
	defer listener.Close()
	fmt.Printf("Nidhogg running on port: %v\n", port)
	fmt.Printf("Wait connection")
	for {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Printf("Accept Err %v\n", err)
			continue
		}
		go sv.HandleConnection(conn) //running in gorutine
	}
}
