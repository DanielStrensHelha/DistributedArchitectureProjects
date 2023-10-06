#this script must be started from the same folder as the Client.class and MainServer.class
param(
    $readers = 1, #Currently useless 
    $updaters = 1
)
#To compile the files, run the following command : 
#javac MainServer$HandledClient.java; javac Client.java
#Or simply javac *.java

start java MainServer$HandledClient
for ($i=0; $i -lt $updaters; $i++) {
    start java Client
}

echo "To add more updaters and readers, use .\Start.ps1 -readers <number of readers> -updaters <number of updaters>"