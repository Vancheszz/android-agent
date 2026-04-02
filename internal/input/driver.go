package input

//Author Jan Slowikowski
import (
	"fmt"
	"os"
	"syscall"
)

// /dev/input const
const (
	EV_KEY     = 0x01
	EV_ABS     = 0x03
	SYN_REPORT = 0x00
	BTN_TOUCH  = 0x14a //touch screen command
	ABS_X      = 0x00
	ABS_Y      = 0x01
	EVIOCGABS  = 0x80184540 //cord limits
)

type Driver struct {
	evDevice      *os.File
	fbData        []byte //framebuffer data
	MaxX          int32
	MaxY          int32
	ScreenW       int32
	ScreenH       int32
	BytesPerPixel int
}

type inputEnvet struct {
	Time  syscall.Timeval
	Type  uint16
	Code  uint16
	Value int32
}

type absInfo struct {
	Value      int32
	Minimum    int32
	Maximum    int32
	Fuzz       int32
	Flat       int32
	Resolution int32
}

func NewDriver(evPath, fbPath string, screenW, screenH int32, bytesPerPixel int) (*Driver, error) {
	//open input
	evFile, err := os.OpenFile(evPath, os.O_WRONLY, 0666)
	if err != nil {
		return nil, fmt.Errorf("failed to open input device %s: %w", evPath, err)

	}
	//open framebuffer
	fbFile, err := os.OpenFile(evPath, os.O_WRONLY, 0666)
	if err != nil {
		evFile.Close()
		return nil, fmt.Errorf("failed to open framebuffer device %s: %w", fbPath, err)

	}
	defer fbFile.Close()
	//memmap of framebuffer
	fbSize := int(screenW * screenH * int32(bytesPerPixel))
	//get data using mmap (pointer)
	fbData, err := syscall.Mmap(int(fbFile.Fd()), 0, fbSize, syscall.PROT_READ, syscall.MAP_SHARED)
	if err != nil {
		evFile.Close()
		return nil, fmt.Errorf("mmao failed: %w", err)
	}
	d := &Driver{
		evDevice:      evFile,
		fbData:        fbData,
		ScreenW:       screenW,
		ScreenH:       screenH,
		BytesPerPixel: bytesPerPixel,
	}
	if err := d.calibrate(); err != nil {
		evFile.Close()
		syscall.Munmap(fbData) //close framebuffer( or mem leak ahahah)
		return nil, err
	}
	return d, nil
}

func (d *Driver) calibrate() error {

	return nil
}
