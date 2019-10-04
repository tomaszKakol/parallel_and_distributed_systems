# Dijkstra algorithm with Java RMI

all: compile
        @echo "Build completed"

compile: Client/*.java Server/*.java
        javac Client/*.java
        javac Server/*.java

server:
        #java -jar -Djava.rmi.server.hostname=${hostIP} Server.Server/jar
        java Server.Server ${hostIP} ${Ports}

client:
        #java -jar -Djava.rmi.server.hostname=127.0.1.1 Client/DijkstraClient.jar
        java Client.Client ${Testcase} ${hostIP} ${Ports}

clean:
        @echo 'clearing files...'
        rm Client/*.class
        rm Server/*.class
        rm Shared/*.class



