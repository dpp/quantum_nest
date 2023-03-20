# Quantum Nest Design Thoughts

This file contains reverse chronological design thoughts.

## 2023/03/20 -- Some random thoughts

Putting down the random thoughts here for further exploration.

### Data Coloring

All the data exchanged between entities (will we call these Actors or is that phrase over-used)
must be "colored" such that attributes of the data are known.

Two important attributes that we care about:

* Did the data come from the "wire"... is it from an untrusted source
* Will the data be sent "off process" where that means will the data be "executed" at the shell or sent to a database as part of a query?

If we can color data (and calls into the system), we can identify "unclean" data and ensure we clean the data
before sending it to a "risky" destination.

### "On Behalf Of"

Every message is "on behalf of" some entity. Maybe it's part of an HTTPS request
and the user was authenticated. Maybe it's a background or chron job, once again
on behalf of an entity. Every message should contain some verified artifact (e.g.,
a signed [JTW](https://jwt.io/)).

### Time

Time is an event. In all things, time is a factor. How each Message Composition will
treat time (how long to wait for data, etc.) is an interesting open quest.

### Message Propogation Tracing/Observability

Every message gets a UUID and also includes the UUID(s) of the source Messages
(or maybe a Merkel Tree of the message UUIDs??) such that message fan-out
and fan-in can be tracked.

### Computation Durability

Different messages have different durability requirements.

For example,
if a query for a low importance item (e.g., "what's the temperature in
San Francisco?") fails, there's likely very little impact.

There may be other, complex transactions (keeping in mind that distributed
transactions is a [hard problem](https://developer.ibm.com/articles/use-saga-to-solve-distributed-transaction-management-problems-in-a-microservices-architecture/)).

Thus, the durability around the coordination of a distributed transaction has a much
higher requirement (and thus a higher memory/computational cost) than other computations.

There must be a way of designating the durability of a particular message processing
activity (and we need a name for message processing activity).

## 2023/03/18 -- Message Passing

Quantum Nest is a message passing system. All of Quantum Nest
is the creation of messages, the sending of messages, and the
receiving of messages (with some wiring among the messages).

The locus of message processing is not defined. And, the locus
of processing may migrate during the processing of a collection
of messages. Thus, all "state" related to message processing
must be easily serializable.

A common pattern in programming is to invoke a method and get
a return value. For example:

```objective-c
  result = [myObject messageName:parameter] 
```

Given that Objective-C and Smalltalk were designed
as [message](https://medium.com/javascript-scene/the-forgotten-history-of-oop-88d71b9b2d9f)
[passing](https://arxiv.org/pdf/1008.1459.pdf) systems, the above code
does not require synchronous execution. Also, given that Objective-C supports [Distributed Objects](https://www.blackholeinc.com/catalog/software/Software/NeXT/PortableDistributedObjects.shtml), the locus of the execution is not defined by
the syntax of a message send.

The implementation of a message passing with response system
requires passing, either implicitly or explicitly, the destination of
a response.

For method invocations in the same address space, that's typically done with the call stack.

For a distributed system, the place to send the response must be encoded in the `Envelope`
and that place must be serializable (along with the other contents of the `Envelope`)
and the response `Channel` must be discoverable within a cluster. And a reply `Channel`
must be able to migrate across the cluster.

Thus, in order to serialize an `Envelope` and it's contents, the `Channel` must be serializable.

