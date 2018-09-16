# smartkeyvalstore
Simple Distributed Key Value Store using Akka HTTP

Features:
1. RESTful APIs for add, remove and fetch the keys
2. Distributed i.e can be run as multiple instances with replication across all the peers.

How to use:
sbt run
It starts the server at port 8080
To run it on a different port
sbt -Ddkv.server.port=<PORT> run
To give list of peer end points which will have data replicated with us as below:
sbt -Ddkv.peer.endpoints=<HOST1:PORT1,HOST2:PORT2..> run

APIs:
1. Fetch a key
URI: /dkv/<KEY_TO_FETCH_>
Example:
curl -v -X GET "http://localhost:8080/dkv/mykey" --header "Content-Type: application/json"
Response as:
{"key":"mykey","value":"myvalue"}
In case it does not exist response will be as below:
{"error":"Key mykey does not exist!"}

2. Add a key
URI: /dkv
Payload is in the format (key:KEY,value:VALUE)
Example:
curl -v -X POST "http://localhost:8080/dkv" -d "{\"key\":\"mykey1\",\"value\":\"myvalue1\"}" --header "Content-Type: application/json"
Response as:
{"description":"Key mykey1 and value myvalue1 is added."}

3. Remove a key
URI: /dkv/<KEY_TO_REMOVE>
Example:
curl -v -X DELETE "http://localhost:8080/dkv/mykey" --header "Content-Type: application/json"
Response as:
{"description":"Key mykey1 is deleted."}
