# Compile all java files
javac -d bin *.java
cd bin

Start-Process java -ArgumentList "Main"
Start-Process java -ArgumentList "HeavyProcessB"
Start-Process java -ArgumentList "HeavyProcessA"

cd ..