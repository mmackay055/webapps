# Web Apps

Contains three simple web applications that I built in school:
- WebCli - simple web client for fetching resources using HTTP or FTP
- WebSrv - simple web server for rendering files using HTTP
- WebProxSrv - simple reverse proxy server

All programs were compiled and tested using JDK 11


# Compile

Execute following command from project directory:

`javac -d bin src/webapps/*.java`

All class files will be in bin folder.

# Running

All three programs can be ran from project directory with the following command: 

`java -cp bin webapps.<program name>`

from the project directory.

The three programs are:
  WebCli - web client
  WebSrv - http webserver
  WebProxSrv - web proxy server

to view details about programs and how to use them pass the '-h' option.

`java -cp bin webapps.<program name> -h`
