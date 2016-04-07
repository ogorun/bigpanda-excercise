# bigpanda-excercise

Task
====

Your exercise is to implement a simple stream processing service that also exposes an HTTP interface.

You are provided with a blackbox executable that spits out an infinite stream of lines of event data encoded in JSON. You can download it here:
* Linux - https://s3-us-west-1.amazonaws.com/bp-interview-artifacts/generator-linux-amd64
* Mac OS X - https://s3-us-west-1.amazonaws.com/bp-interview-artifacts/generator-macosx-amd64
* Windows - https://s3-us-west-1.amazonaws.com/bp-interview-artifacts/generator-windows-amd64.exe

Please note that the stream might sometimes encounter errors and output corrupt JSON lines.

These are the requirements for your service:
- it should consume the output of the generator and gather the following stats: a count of events by event type and a count of words encountered in the data field of the events.
- it should expose these stats in an HTTP interface
- the processing of the generator data and the handling of the HTTP requests should not block each other

The architecture of your service should obviously decouple the data processing, HTTP handling, be testable, etc. You should implement this in Scala.


Solution description
====================

Solution consists of 3 main parts (divided on package level): 
- http - spray service for current state representing through HTTP
- streaming - akka-streaming, processing new messages 
- data - package with common data representing classes and repositories

Data storage - Redis

Possible improvements
=====================

- divide project to three different subprojects assembled to separate jars (data, http, streaming)
- implement lambda architecture for more robust solution: store events in idempotent way and prepare aggregations once an hour (for example), 
for get sum this stable part with data from current increment-like way only for new hour
- tune type of storage for real streaming data needs: streaming and http throughput, time to store data, variety of words and types
- add more testing/logging/configuration/environment separation and other needs of real production-ready product 