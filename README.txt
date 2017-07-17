=====INDEX=====
1.Included files
2.How to run
3.Console Commands

-----------------------------------------------------------------------------------------------------------

1.INCLUDED FILES:

All source files are within the following packages.

cs555RS/node/routing
cs555RS/node/transport
cs555RS/node/util
cs555RS/node/wireformats
READ_ME.txt
Makefile

-----------------------------------------------------------------------------------------------------------

2.HOW TO RUN:

Controller : java cs555RS.nodes.Controller <listenig_portnum>
Client : java cs555RS.nodes.Client <controller-host> <controller-port>
Client : java cs555RS.nodes.ChunkServer <controller-host> <controller-port>

-----------------------------------------------------------------------------------------------------------

3.CONSOLE COMMANDS

Client:
store <file_name>
read <file_name>
-----------------------------------------------------------------------------------------------------------
