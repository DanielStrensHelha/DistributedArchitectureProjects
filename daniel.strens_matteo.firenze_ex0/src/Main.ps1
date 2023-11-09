#Compile all and start the Brain
javac -d bin *.java
cd bin
Start-Process java -ArgumentList "Brain"

#Start the updaters
for ($i=0; $i -lt 5; $i++) {
    Start-Process java -ArgumentList "Cell", "updater", "10"
}

#Start the readers
for ($i=0; $i -lt 15; $i++) {
    Start-Process java -ArgumentList "Cell", "reader", "30"
}

cd ..