package filesystem

import (
	"bytes"
	"encoding/gob"
	"fmt"
	"log"
	"strings"
	"time"
)

// Superblock at 0
// inode bitmap at 1
// free space bitmap 2
// inode at 8, 80 inodes from 1 to 80, each 256
// data blocks at 28
var Disk [6172][1024]byte
var currentDirectory int16 = 1 //root is 1

func main() {
	InitializeFileSystem()
}

func Write(filePath string, data []string) {
	encodedData := EncodeToBytes(data)
	InodeNumber, isDirectory := FindInPath(filePath)
	inode := INode{}

	if isDirectory == 1 {
		Create(filePath)
		InodeNumber, isDirectory = FindInPath(filePath)
	}
	inodeBlock := int(InodeNumber*256) / 1024
	inodeInBlock := int(InodeNumber*256) - (inodeBlock * 1024)
	inode = ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
	for i := 0; i <= len(encodedData)/1024; i++ {
		if i == 0 {
			inode.DataBlock1 = calcFreeSpace()
			if len(encodedData) > 1024 {
				copy(Disk[inode.DataBlock1][:], encodedData[:1024])
			} else {
				copy(Disk[inode.DataBlock1][:], encodedData)
			}
		} else if i == 1 {
			inode.DataBlock2 = calcFreeSpace()
			if len(encodedData) > 2048 {
				copy(Disk[inode.DataBlock2][:], encodedData[1024:2048])
			} else {
				copy(Disk[inode.DataBlock2][:], encodedData[1024:])
			}
		} else if i == 2 {
			inode.DataBlock3 = calcFreeSpace()
			if len(encodedData) > 3072 {
				copy(Disk[inode.DataBlock3][:], encodedData[2048:3072])
			} else {
				copy(Disk[inode.DataBlock3][:], encodedData[2048:])
			}
		} else if i == 3 {
			inode.DataBlock4 = calcFreeSpace()
			indirectBlock := calcFreeSpace()
			indirectBlockBytes := EncodeToBytes(indirectBlock)
			copy(Disk[inode.DataBlock4][0:4], indirectBlockBytes)
			if len(encodedData) > 4096 {
				copy(Disk[indirectBlock][:], encodedData[3072:4096])
			} else {
				copy(Disk[indirectBlock][:], encodedData[3072:])
			}
		} else {
			indirectBlock := calcFreeSpace()
			indirectBlockBytes := EncodeToBytes(indirectBlock)
			copy(Disk[inode.DataBlock4][(i-3)*4:((i-3)*4)+4], indirectBlockBytes)
			if len(encodedData) > (i+1)*1024 {
				copy(Disk[indirectBlock][:], encodedData[(i*1024):(i+1)*1024])
			} else {
				copy(Disk[indirectBlock][:], encodedData[(i*1024):])
			}
		}
	}
	inode.IsValid = 1
	inode.LastModified = time.Now().Unix()
	encodedInode := EncodeToBytes(inode)
	copy(Disk[inodeBlock+8][inodeInBlock:inodeInBlock+256], encodedInode)
}

func Append(filePath string, data []string) {
	currentString := Read(filePath)
	arrayString := strings.Split(currentString, "")
	arrayString = append(arrayString, data...)
	Write(filePath, arrayString)
}

func Create(filePath string) {
	directory, isDirectory := FindInPath(filePath)
	if isDirectory == 0 {
		fmt.Println("File already exists")
	} else {
		inodeLocation := calcFreeInode()
		initializeInode(inodeLocation)
		inodeBlock := int((directory)*256) / 1024
		inodeInBlock := int((directory)*256) - (inodeBlock * 1024)
		directoryInode := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
		directoryMap := make(map[string]int16)
		decoder := gob.NewDecoder(bytes.NewReader(Disk[directoryInode.DataBlock1][:]))
		err := decoder.Decode(&directoryMap)
		if err != nil {
			log.Fatal("Couldn't read directory: ", err)
		}
		directoryMap[filePath] = inodeLocation + 1
		directoryMapBytes := EncodeToBytes(directoryMap)
		copy(Disk[directoryInode.DataBlock1][:], directoryMapBytes)
	}
}

func Unlink(filePath string) {
	pathArray := strings.Split(filePath, "/")
	directoryPath := ""
	directory := INode{}
	for i := 0; i < len(pathArray)-1; i++ {
		directoryPath += (pathArray[i] + "/")
	}
	if directoryPath == "" {
		inodeBlock := int((currentDirectory-1)*256) / 1024
		inodeInBlock := int((currentDirectory-1)*256) - (inodeBlock * 1024)
		directory = ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
	} else {
		directoryLocation, isDirectory := FindInPath(directoryPath)
		if isDirectory == 0 {
			log.Fatal("Couldn't find directory for unlink.")
		} else {
			inodeBlock := int((directoryLocation)*256) / 1024
			inodeInBlock := int((directoryLocation)*256) - (inodeBlock * 1024)
			directory = ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
		}
	}
	directoryMap := make(map[string]int16)
	decoder := gob.NewDecoder(bytes.NewReader(Disk[directory.DataBlock1][:]))
	err := decoder.Decode(&directoryMap)
	if err != nil {
		log.Fatal("Couldn't read directory map: ", err)
	}
	fileLocation := directoryMap[pathArray[len(pathArray)-1]]
	inodeBlock := int((fileLocation-1)*256) / 1024
	inodeInBlock := int((fileLocation-1)*256) - (inodeBlock * 1024)
	fileInode := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
	if fileInode.IsDirectory == 1 {
		deleteMap := make(map[string]int16)
		decoder := gob.NewDecoder(bytes.NewReader(Disk[fileInode.DataBlock1][:]))
		err := decoder.Decode(&deleteMap)
		if err != nil {
			log.Fatal("Couldn't read delete directory map: ", err)
		}
		if len(deleteMap) > 2 {
			log.Fatal("Delete files within the directory before deleting.")
		}
	}
	Disk[1][fileLocation-1] = 0
	if fileInode.DataBlock1 > -1 {
		Disk[2][fileInode.DataBlock1] = 0
	}
	if fileInode.DataBlock2 > -1 {
		Disk[2][fileInode.DataBlock2] = 0
	}
	if fileInode.DataBlock3 > -1 {
		Disk[2][fileInode.DataBlock3] = 0
	}
	if fileInode.DataBlock4 > -1 {
		invalid := 0
		var indirectBlock int32
		i := 0
		for invalid < 1 {
			indirectBlockBytes := Disk[fileInode.DataBlock4][(i * 4) : (i*4)+4]
			if indirectBlockBytes[0] == 0 {
				invalid = 1
			} else {
				decoder := gob.NewDecoder(bytes.NewReader(indirectBlockBytes))
				err := decoder.Decode(&indirectBlock)
				if err != nil {
					log.Fatal("Couldn't read indirect block: ", err)
				}
				Disk[2][indirectBlock] = 0
			}
			i++
		}
		Disk[2][fileInode.DataBlock4] = 0
	}
	delete(directoryMap, pathArray[len(pathArray)-1])
	directoryMapBytes := EncodeToBytes(directoryMap)
	copy(Disk[directory.DataBlock1][:], directoryMapBytes)
}

func Read(filePath string) string {
	result := ""
	inodeNumber, isDirectory := FindInPath(filePath)
	if inodeNumber == -1 {
		result = "File does not exist"
	} else {
		inodeBlock := int(inodeNumber*256) / 1024
		inodeInBlock := int(inodeNumber*256) - (inodeBlock * 1024)
		inode := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
		if inode.IsValid == 1 {
			if isDirectory == 1 {
				data := make(map[string]int16)
				if inode.DataBlock1 > -1 {
					decoder := gob.NewDecoder(bytes.NewReader(Disk[inode.DataBlock1][:]))
					err := decoder.Decode(&data)
					if err != nil {
						log.Fatal("Couldn't read file: ", err)
					}
					for key, value := range data {
						inodeBlock = int((value-1)*256) / 1024
						inodeInBlock = int((value-1)*256) - (inodeBlock * 1024)
						file := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
						if file.IsDirectory == 1 {
							result += ("Directory: " + key + "\tInode number: " + fmt.Sprint(value) + "\n")
						} else {
							result += ("Filename: " + key + "\tInode number: " + fmt.Sprint(value) + "\n")
						}
					}
				} else {
					result += "No data for directory"
				}
			} else {
				data := make([]byte, 1024)
				totalData := make([]byte, 0, 310272)
				if inode.DataBlock1 < 0 {
					result += "No data in file"
				} else {
					data = Disk[inode.DataBlock1][:]
					for i := 0; i < len(data); i++ {
						totalData = append(totalData, data[i])
					}
					if inode.DataBlock2 > -1 {
						data = Disk[inode.DataBlock2][:]
						for i := 0; i < len(data); i++ {
							totalData = append(totalData, data[i])
						}
					}
					if inode.DataBlock3 > -1 {
						data = Disk[inode.DataBlock3][:]
						for i := 0; i < len(data); i++ {
							totalData = append(totalData, data[i])
						}
					}
					if inode.DataBlock4 > -1 {
						invalid := 0
						var indirectBlock int32
						i := 0
						for invalid < 1 {
							indirectBlockBytes := Disk[inode.DataBlock4][(i * 4) : (i*4)+4]
							if indirectBlockBytes[0] == 0 {
								invalid = 1
							} else {
								decoder := gob.NewDecoder(bytes.NewReader(indirectBlockBytes))
								err := decoder.Decode(&indirectBlock)
								if err != nil {
									log.Fatal("Couldn't read indirect block: ", err)
								}
								data = Disk[indirectBlock][:]
								for i := 0; i < len(data); i++ {
									totalData = append(totalData, data[i])
								}
							}
							i++
						}
					}
					seperateStrings := make([]string, 0, 310272)
					decoder := gob.NewDecoder(bytes.NewReader(totalData))
					err := decoder.Decode(&seperateStrings)
					if err != nil {
						log.Fatal("Couldn't read file: ", err)
					}
					for i := 0; i < len(seperateStrings); i++ {
						result += seperateStrings[i]
					}
				}
			}
		} else {
			result = "File has no data"
		}
	}
	return result
}

func ChngDir(filePath string) {
	directory, isDirectory := FindInPath(filePath)
	if isDirectory == 1 {
		currentDirectory = directory + 1
	} else {
		fmt.Println("Path does not lead to directory")
	}
}

func Mkdir(filePath string) {
	parentDirectory := int16(1)
	isDirectory := byte(0)
	if filePath != "~" {
		parentDirectory, isDirectory = FindInPath(filePath)
		if isDirectory == 1 {
			directoryLocation := calcFreeInode()
			initializeInode(directoryLocation)
			inodeBlock := int(directoryLocation*256) / 1024
			inodeInBlock := int(directoryLocation*256) - (inodeBlock * 1024)
			directory := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
			inodeBlock = int((parentDirectory)*256) / 1024
			inodeInBlock = int((parentDirectory)*256) - (inodeBlock * 1024)
			parent := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
			directory.IsDirectory = 1
			directory.IsValid = 1
			directory.DataBlock1 = calcFreeSpace()
			fileMap := make(map[string]int16)
			fileMap["."] = directory.InodeNumber
			fileMap[".."] = parentDirectory + 1
			parentMap := make(map[string]int16)
			decoder := gob.NewDecoder(bytes.NewReader(Disk[parent.DataBlock1][:]))
			err := decoder.Decode(&parentMap)
			if err != nil {
				log.Fatal("Couldn't read parent: ", err)
			}
			splitPath := (strings.Split(filePath, "/"))
			fileName := splitPath[len(splitPath)-1]
			parentMap[fileName] = directory.InodeNumber
			parentMapBytes := EncodeToBytes(parentMap)
			copy(Disk[parent.DataBlock1][:], parentMapBytes)
			fileMapBytes := EncodeToBytes(fileMap)
			copy(Disk[directory.DataBlock1][:], fileMapBytes)
			directoryBytes := EncodeToBytes(directory)
			inodeBlock = int(directoryLocation*256) / 1024
			inodeInBlock = int(directoryLocation*256) - (inodeBlock * 1024)
			copy(Disk[inodeBlock+8][inodeInBlock:inodeInBlock+256], directoryBytes)
		} else {
			fmt.Println("Couldn't find parent directory")
		}
	} else {
		directoryLocation := calcFreeInode()
		initializeInode(directoryLocation)
		inodeBlock := int(directoryLocation*256) / 1024
		inodeInBlock := int(directoryLocation*256) - (inodeBlock * 1024)
		directory := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
		directory.IsDirectory = 1
		directory.IsValid = 1
		directory.DataBlock1 = calcFreeSpace()
		fileMap := make(map[string]int16)
		fileMap["."] = directory.InodeNumber
		fileMap[".."] = directory.InodeNumber
		fileMapBytes := EncodeToBytes(fileMap)
		copy(Disk[directory.DataBlock1][:], fileMapBytes)
		directoryBytes := EncodeToBytes(directory)
		copy(Disk[inodeBlock+8][inodeInBlock:inodeInBlock+256], directoryBytes)
	}
}

func FindInPath(path string) (int16, byte) {
	result := int16(0)
	isDirectory := byte(0)
	pathArray := strings.Split(path, "/")
	directoryLocation := currentDirectory - 1
	if strings.Compare(pathArray[0], "~") == 0 {
		directoryLocation = 0
	}
	directoryMap := make(map[string]int16)
	var i int
	if len(pathArray) > 1 {
		for i = 0; i < len(pathArray)-1; i++ {
			//go through until get to invalid file or end of path
			inodeBlock := int(directoryLocation*256) / 1024
			inodeInBlock := int(directoryLocation*256) - (inodeBlock * 1024)
			directory := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
			decoder := gob.NewDecoder(bytes.NewReader(Disk[directory.DataBlock1][:]))
			err := decoder.Decode(&directoryMap)
			if err != nil {
				log.Fatal("Couldn't read directory: ", err)
			}

			inodeNumber, ok := directoryMap[pathArray[i]]
			if ok {
				inodeNumber = inodeNumber - 1
				inodeBlock = int(inodeNumber*256) / 1024
				inodeInBlock = int(inodeNumber*256) - (inodeBlock * 1024)
				nextFile := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
				if nextFile.IsDirectory == 1 {
					directoryLocation = inodeNumber
				}
			}
		}
	}
	//get map from current directory to place
	inodeBlock := int(directoryLocation*256) / 1024
	inodeInBlock := int(directoryLocation*256) - (inodeBlock * 1024)
	directory := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
	decoder := gob.NewDecoder(bytes.NewReader(Disk[directory.DataBlock1][:]))
	err := decoder.Decode(&directoryMap)
	if err != nil {
		log.Fatal("Couldn't read directory: ", err)
	}
	inodeNumber, ok := directoryMap[pathArray[i]]
	if ok {
		result = inodeNumber - 1
		inodeBlock := int((inodeNumber-1)*256) / 1024
		inodeInBlock := int((inodeNumber-1)*256) - (inodeBlock * 1024)
		file := ReadINode(Disk[inodeBlock+8][inodeInBlock : inodeInBlock+256])
		if file.IsDirectory == 1 {
			isDirectory = 1
		}
	} else {
		result = directoryLocation
		isDirectory = 1
	}
	return result, isDirectory
}

func initializeRoot() {
	Mkdir("~")
	rootMap := make(map[string]int16)
	decoder := gob.NewDecoder(bytes.NewReader(Disk[28][:]))
	err := decoder.Decode(&rootMap)
	if err != nil {
		log.Fatal("Couldn't read root: ", err)
	}
	rootMap["~"] = rootMap["."]
	encodedRoot := EncodeToBytes(rootMap)
	copy(Disk[28][:], encodedRoot)
}

func initializeInode(inodeLocation int16) {
	newInode := INode{
		InodeNumber:  inodeLocation + 1,
		IsValid:      0,
		IsDirectory:  0,
		DataBlock1:   -1,
		DataBlock2:   -1,
		DataBlock3:   -1,
		DataBlock4:   -1,
		CreateTime:   time.Now().Unix(),
		LastModified: time.Now().Unix(),
	}
	encodedInode := EncodeToBytes(newInode)
	inodeBlock := int(inodeLocation*256) / 1024
	inodeInBlock := int(inodeLocation*256) - (inodeBlock * 1024)
	copy(Disk[inodeBlock+8][inodeInBlock:inodeInBlock+256], encodedInode)
}

func calcFreeSpace() int32 {
	location := 0
	found := false
	for i := 0; i < 6 && found == false; i++ {
		for j := 0; j < 1024 && found == false; j++ {
			if Disk[i+2][j] == 0 {
				location = (i * 1024) + j
				found = true
				Disk[i+2][j] = 1
			}
		}
	}
	return int32(location + 28)
}

func calcFreeInode() int16 {
	location := int16(0)
	found := false
	for i := 0; i < 80 && found == false; i++ {
		if Disk[1][i] == 0 {
			location = int16(i)
			found = true
			Disk[1][i] = 1
		}
	}
	return location
}

type INode struct {
	InodeNumber  int16
	IsValid      byte
	IsDirectory  byte
	DataBlock1   int32
	DataBlock2   int32
	DataBlock3   int32
	DataBlock4   int32 //indirect, block of blocks
	CreateTime   int64
	LastModified int64
}

type SuperBlock struct {
	INodeBitmapStart     int
	FreeBlockBitmapStart int
	DataBlockStart       int
	INodeStart           int
}

func InitializeFileSystem() {
	initializeSuperBlock()
	initializeRoot()
}

func initializeSuperBlock() {
	superBlock := SuperBlock{
		INodeBitmapStart:     1,
		FreeBlockBitmapStart: 2,
		INodeStart:           8,
		DataBlockStart:       28,
	}
	superBlockBytes := EncodeToBytes(superBlock)
	copy(Disk[0][:], superBlockBytes)
}

func ReadSuperBlock() SuperBlock {
	sBlock := SuperBlock{}
	decoder := gob.NewDecoder(bytes.NewReader(Disk[0][:]))
	err := decoder.Decode(&sBlock)
	if err != nil {
		log.Fatal("Couldn't read super block: ", err)
	}
	return sBlock
}

func ReadINode(inodeLocation []byte) INode {
	iBlock := INode{}
	decoder := gob.NewDecoder(bytes.NewReader(inodeLocation))
	err := decoder.Decode(&iBlock)
	if err != nil {
		log.Fatal("Couldn't read inode: ", err)
	}
	return iBlock
}

// from https://gist.github.com/SteveBate/042960baa7a4795c3565
func EncodeToBytes(p interface{}) []byte {

	buf := bytes.Buffer{}
	enc := gob.NewEncoder(&buf)
	err := enc.Encode(p)
	if err != nil {
		log.Fatal(err)
	}
	return buf.Bytes()
}
