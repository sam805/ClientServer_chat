# ClientServer_chat
This program includes one main methods and two classes, as follows:
analyseInput Method: This method handles inputs and setting network address and port parameters. It is also responsible to check if program should run in client or server side according to the inputs parameters.
Client and Server Classes: These are two classes which are used in this project. We need multithreading, since program should be able to listen what information are entered from keyboard and at the same time program must be able to listen on that port to see the what information has been sent to it. So, we need to threads, because reading from the port is a blocking method:
1)	Read from input
2)	Check the ports
The other parts of the program is exception handling for being more user friendly.
