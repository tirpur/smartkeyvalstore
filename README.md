# smartkeyvalstore

# Description
Simple Distributed Key Value Store using Akka HTTP with following highlights:
1. RESTful APIs to add, remove and fetch the keys.
2. Distributed i.e can be run as multiple instances with replication across all the peers.

# Environment
1. Uses SBT for build and dependency management.
2. Compile and tested using Scala 2.12.x
3. Uses scalatest for unit tests
4. Uses JSON format for input as well as the response.

# Usage
1. To run the server:
sbt run
It starts the server at default port 8080 and binds on all the interfaces.
To run it on a different port:
sbt -Ddkv.server.port=<PORT> run
To give list of peer end points which will have data replicated with us as below:
sbt -Ddkv.peer.endpoints=<HOST1:PORT1,HOST2:PORT2..> run

2. To test only run:
sbt test

# API Usage and examples
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
