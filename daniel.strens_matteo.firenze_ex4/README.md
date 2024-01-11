#TODO complete this file
Main.java: This is the entry point of your application. It should initialize the nodes and start the server for communication.

Node.java: This class represents a node in the system. It should have methods for handling transactions, maintaining the log of versions, and communicating with other nodes.

Transaction.java: This class represents a transaction. It should have methods for executing the transaction on a node.

Client.java: This class represents a client. It should read transactions from a local file and send them to the appropriate node.

Server.java: This class handles socket communication between nodes. It should have methods for sending and receiving data.