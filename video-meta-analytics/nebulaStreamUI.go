package main

import (
	"embed"
	"fmt"
	"io/fs"
	"net/http"
	"os"
)

//go:embed build
var embeddedFiles embed.FS

var elegantDevOpsDashboard = `
███████╗██╗     ███████╗ ██████╗  █████╗ ███╗   ██╗████████╗    ██████╗ ███████╗██╗   ██╗ ██████╗ ██████╗ ███████╗    ██████╗  █████╗ ███████╗██╗  ██╗██████╗  ██████╗  █████╗ ██████╗ ██████╗ 
██╔════╝██║     ██╔════╝██╔════╝ ██╔══██╗████╗  ██║╚══██╔══╝    ██╔══██╗██╔════╝██║   ██║██╔═══██╗██╔══██╗██╔════╝    ██╔══██╗██╔══██╗██╔════╝██║  ██║██╔══██╗██╔═══██╗██╔══██╗██╔══██╗██╔══██╗
█████╗  ██║     █████╗  ██║  ███╗███████║██╔██╗ ██║   ██║       ██║  ██║█████╗  ██║   ██║██║   ██║██████╔╝███████╗    ██║  ██║███████║███████╗███████║██████╔╝██║   ██║███████║██████╔╝██║  ██║
██╔══╝  ██║     ██╔══╝  ██║   ██║██╔══██║██║╚██╗██║   ██║       ██║  ██║██╔══╝  ╚██╗ ██╔╝██║   ██║██╔═══╝ ╚════██║    ██║  ██║██╔══██║╚════██║██╔══██║██╔══██╗██║   ██║██╔══██║██╔══██╗██║  ██║
███████╗███████╗███████╗╚██████╔╝██║  ██║██║ ╚████║   ██║       ██████╔╝███████╗ ╚████╔╝ ╚██████╔╝██║     ███████║    ██████╔╝██║  ██║███████║██║  ██║██████╔╝╚██████╔╝██║  ██║██║  ██║██████╔╝
╚══════╝╚══════╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝   ╚═╝       ╚═════╝ ╚══════╝  ╚═══╝   ╚═════╝ ╚═╝     ╚══════╝    ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ `

func main() {
	//Default port
	port := ":9000"
	//Fetch the user provided port
	if len(os.Args) > 1 {
		port = ":" + os.Args[1]
	}

	fmt.Println(elegantDevOpsDashboard)
	http.Handle("/", http.FileServer(getFileSystem()))
	fmt.Println("Listening on " + port)
	http.ListenAndServe(port, nil)
	fmt.Println("Shutting down Elegant DevOps Dashboard")
}

func getFileSystem() http.FileSystem {
	// Get the build subdirectory as the
	// root directory so that it can be passed
	// to the http.FileServer
	fsys, err := fs.Sub(embeddedFiles, "build")
	if err != nil {
		panic(err)
	}
	return http.FS(fsys)
}
