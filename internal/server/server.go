package server

import (
	"encoding/binary"
	"fmt"
	"io"
	"net"

	pb "github.com/Vancheszz/android-agent/internal/ratatoskr"
	"google.golang.org/protobuf/proto"
)

func HandleConnection(conn net.Conn) {
	defer conn.Close()
	fmt.Println("Connect to Ratatoskr")

	for {
		//read header from ratatoskr
		header := make([]byte, 4)
		_, err := io.ReadFull(conn, header)
		if err != nil {
			if err == io.EOF {
				fmt.Printf("Agent was disconnected: %s\n", conn.RemoteAddr())
			} else {
				fmt.Printf("Header read error: %s\n", err)
			}
			return
		}
		//convert header to number (BigEndian Type)
		size := binary.BigEndian.Uint32(header)
		//read data
		payload := make([]byte, size)

		_, err = io.ReadFull(conn, payload)
		if err != nil {
			fmt.Printf("Reading Payload error: %s\n", err)
		}
		//Get Dump !
		dump := &pb.ScreenDump{}
		if err := proto.Unmarshal(payload, dump); err != nil {
			fmt.Printf("Err reading package: %v\n", err)
			continue
		}

		fmt.Printf("Dump: %s | Nodes: %d | Time: %d\n",
			dump.PackageName, len(dump.Nodes), dump.Timestamp)
		for i, node := range dump.Nodes {
			if i > 70 {
				fmt.Println("... и остальные ноды")
				break
			}

			fmt.Printf("  [Node %d] Class: %s | Text: '%s' | ResourceID: %s | Clickable: %v |  Bounds: [%d, %d, %d, %d]\n",
				i,
				node.ClassName,
				node.Text,
				node.ResourceId,
				node.IsClickable,
				node.Bounds.Left,
				node.Bounds.Top,
				node.Bounds.Right,
				node.Bounds.Bottom,
			)
		}

	}

}
