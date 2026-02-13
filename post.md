# POST

## Introduction
The goal of this project is to develop a virtual space in which multiple users can draw lines in real-time.

<!-- Potentially a video of how the board works. -->

## Architecture
This is the architecture of the system:
<br>![architecture](/doc/arc.png)

<!-- This is only an image that is useful like a summary of how the system works. -->

<!-- A diagram that describe that each client need to be connected  -->
Each client is connected to a virtual board. Each client has the possibility do draw and delete lines from that virtual board. We want that all the copies of the same virtual boards that are contained in different clients, are synced obtaining the same exact board on all the clients.

To obtain this result, each client needs to communicate to other clients the commands that are executed on that client. So, for example, you need to communicate each command that is executed on a board from each client.
So, ideally, if we have a reliable connection(messages are delivered exactly one time to a user), we can exchange the commands that are executed on the board, and then applying all the commands, we can obtain the same board on every client.

Problem: If we assume to have three clients that are operating on the same board. If the client_1 and client_2 create a new line, they send to other clients the commands to create new lines. We can't make assumption on the order in which these commands are received by the client_3. So, if we apply the commands in the order in which we received these lines, we will obtain different boards on different clients.

Problem: We assume to have 3 clients that are operating on the same board. 
1. client_1 create a line. 
2. client_2 receive the command to create the line.
3. client_2 delete that line.
4. client_3 receive the command to delete the line.
5. client_3 receive the command to create the line.

In this case, the order in which the commands are received, can bring to a wrong result.
<!-- Problem: We assume to have three clients that are operating on the same board. client_1 and client_2 delete the same line in the same moment. The client_3 receive two commands to delete the same line. What happens? -->

With the current solution, the board that we obtain depend on the base of the order in which we receive commands.

We need to find an agreement between all the clients about the ordering in which the commands are applied.

With this architecture is difficult to obtain the agreement about the ordering. 


<!-- T: Talk about the undeterministic nature of the problem (START)-->
<!-- T: Add ref to a book or notes of the professor, even about the theorem -->
In general we know that the agreement between machines that are asynchronus is an unsolvable problem with deterministic algorithm.
<!-- T: Talk about the undeterministic nature of the problem (END)-->


<!-- T: Talk about the possible solutions and problems (START)-->
There are a lot of algorithms that solve the problem under some constaints or/and with a certain probability.

For example, the BenOr algorithm.

This type of solution, can easily lead to some problem in performance. In fact, we need to exchange multiple messages for each single agreement.
This type of solution became also really complex.
(Because we need to repeat multiple time a message for each single agreement)
<!-- T: Talk about the possible solutions and problems (END)-->


<!-- T: Talk about how using id in the correct way, we can easily obtain the agreement that we need, but it's difficult to agree about the id (START) -->
Now that we have understood a little bit the problems that we have with the current solution, let's assume that each client has the ability to embed in a command a unique ID that is used to specify the total order in which the commands need to be applied to obtain the same exact board.

At this point, if we receive commands in different order, we can always apply commands in the correct order. So, the commands are "commutative".
Even if we receive two commands that are equal operation, we can identify, for example the line that we want to delete. If we find the line, we delete it, otherwise, we don't find the line and we are ok with it.
<!-- T: Talk about how using id in the correct way, we can easily obtain the agreement that we need, but it's difficult to agree about the id (END) -->

<br>

How we can easily reach this total order?

<!-- Image: each client connected to the server, and the server connected to all clients -->
Let's assume to add a new actor in the middle of clients, a server.
Each actor communicate only with the server, and the server exchange messages with all the clients.

The idea now is that no client directly communicate with another client. Each client can communicate only with the server. So, when a client execute a command, send it to the server, and then the server send the command to all other clients.

We establish that is the server to decide the order in which commands are applied. Now, it's easy to arrive to an agreement above the order in which commands are applied.

In our case, ID of commands are generated on the base of the time in which the commands is received by the server + the id of the user.

<br>

Why we can't simply generate ID of commands based on the current time of each clients? It's difficult beacuse each client is totally asynchronus. This problem is for example solved in the BenOr or/and Paxos algorithm, where we establish round for each decision. This type of solution come with the cost we talked about before.

After we found this solution, we discovered tat similar solutions are used in practice:
<!-- Images of Google Docs and... minecraft? -->

They are called OT(Operational Transformations).

There are also other problems. <!-- What? -->

<br>

So, now the server has two purposes:
- Is the actor that is trusted and listened by all the clients to generate the total order of the commands.
- Send each command that is sent by a client to all the clients.

In terms of the architecture it's can be useful to assign this tasks to two different actors. To be precise, we will use Azure App Service to power a Spring Web Server that is used to specify the total order(and even for other purposes, we will discover that the server it's useful to solve multiple problems in an easy way).

In this case, we don't need only to sends messages from client to server, but we also need to send messages from server to the client. We will use, for this purposes a WebSocket connection, that give to the server the possibility to send a message to a client.

To handle connection and to spread a commands between all the clients, we will rely above Azure SignalR.

<br>

<!-- T: Talk about the fact that the server is also useful to store temporarily all the commands that are generated by the clients. So we can rely on the server to send all the commands to a client that lately joined the board, so doesn't received the commands when they arrived to the server -->

In our solution, we've decide to not store on the server the commands but directly the resulting boards. Each time the server receive a command from a client, decide the ID to assign to the command, send to all the clients the command and then apply the command to the board. This approach, where we aren't storing directly a dump of the commands in the server, we are easily storing less data. 

<!-- T: Permanent storage (START) -->
In the solution explained until now, we are storing board only in volatile storage. We want to give to the end user the possibility to store boards and re-load them in future to be modified. We give to the end-user two different kinds of storage options:
- To store on their disk the board.
- To store in cloud the board.


<!-- T: Permanent storage (END) -->

<br>
<!-- T: Serverless operations (START) -->
We want also to give to the end user the possibility to:
- Delete boards.
- Upload boards.
- Download boards(That aren't currently shared).

For this type of operations, is easy to interact with Blob Storage from Function-As-Service. So we decided to implement this operations with Azure Functions.
<!-- T: Serverless operations (END) -->

<br>
The last piece that we miss is an efficient way to store secrets. Each service of Azure need some key/token to work. In Azure, to retrieve this information at run-time, usually is used Azure Key Vault. This service, give the possiblity to store in a single point of true secrets and retrieve them from multiple services at run-time.