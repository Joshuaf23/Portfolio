package main

import (
	"bufio"
	"fmt"
	"os"
	"os/exec"
	"strings"
	"main.go/filesystem"
)

func main() {
	filesystem.InitializeFileSystem()
	inputS := bufio.NewScanner(os.Stdin)
	fmt.Print(" > ")

	for inputS.Scan() {
		userText := inputS.Text()
		if strings.Contains(userText, ">>"){
			commands := strings.Split(userText, ">>")
			args1 := strings.Split(commands[0], " ")
			data := make([]string, 1)
			output := runCommand(args1)
			data[0] = output
			filesystem.Append(strings.Trim(commands[1], " "), data)
		} else if strings.Contains(userText, ">") {
			commands := strings.Split(userText, ">>")
			args1 := strings.Split(commands[0], " ")
			data := make([]string, 1)
			output := runCommand(args1)
			data[0] = output
			filesystem.Write(strings.Trim(commands[1], " "), data)
		} else {
			args := strings.Split(userText, " ")
			output := runCommand(args[:])
			fmt.Println(output)
		}
		fmt.Print(" > ")
	}
}

func runCommand(commands []string) string {
	result := ""
	switch commands[0] {
		case "exit":
			os.Exit(0)
		case "whoami":
			result = "Name: Joshua Fuller\nUser: jfuller6"
		case "cd":
			filesystem.ChngDir(commands[1])
		case "mkdir":
			filesystem.Mkdir(commands[1])
		case "rm":
			filesystem.Unlink(commands[1])
		case "mv":
			data := make([]string, 1)
			data[0] = filesystem.Read(commands[1])
			filesystem.Write(commands[2], data)
			filesystem.Unlink(commands[1])
		case "ls":
			result = filesystem.Read(".")
		case "cp":
			filesystem.Create(commands[2])
			data := make([]string, 1)
			data[0] = filesystem.Read(commands[1])
			filesystem.Write(commands[2], data)
		case "more":
			result = filesystem.Read(commands[1])
		default:
			output, execErr := exec.Command(commands[0], commands[1:]...).Output()
			if execErr != nil {
				fmt.Println("Command could not run: ", execErr)
			}
			result = string(output)
	}
	return result
}