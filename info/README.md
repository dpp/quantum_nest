# Quantum Nest

Substantially all of development is a series of message management
and set/state management operations, both persistent and local/non-persistent
state.


> Huh What?!? Programming is so much more!!!


Turns out it's not.

An `HTTPS` request is just a message that has as part of its semantics
an `HTTPS` response message.

Authorization is simply message management (filtering).

Sometimes, a single message will "fan out" into a series of
messages, some of which have implied responses (a Remote
Procedure Call, RPC).

Every single `async` function in JavaScript or Rust is
simply an asynchronous message, and sometimes the response
to that message is required.

So much of how we do computing today is bridging between
the messages with schemas style description (e.g., 
[OpenAPI/Swagger](https://swagger.io/specification/)) and
associated bridges to imperative programming (code generators)
or storages schema management and bridges to imperative
languages (e.g., migration files and object/relational mappers).

