### TDD Banking Kata

TODO: move tests into each module

This solution implements a very simple banking system that supports transactions in terms of atomicity, consistency, and isolation. Although initially called TDD kata, this solution didn't strictly apply the TDD principles. Rather than following tests, I used tests to specify some contracts and express how a certain method should perform in given context, also leaving rooms for future CI/CD pipelines.

Writing banking system in a modern programming language to replace COBOL code could be challenging. One interesting aspect is to discuss the transaction, which is critical to maintain a banking system. In general, database transactions have four properties that should be implemented, which are ACID properties. 

We mainly discuss the atomicity here. More concretely, we implement transactions that are either executed as a whole or none. This design is inspired by the undo log of MySQL.

In order not to make things too complex, the database and data access adapter are implemented in a minimalist way just to present some concepts. Real world DBMS and ORM could be very complex.

Of course it’s unnecessary to implement a database for designing a banking system. I did so because most of the solutions of this kata do not use an external DBMS. In my case, this means that I have to build something to provide transaction supports. A more practical way of solving this kata would be just rely on Java/Kotlin frameworks, existing DBMS like MySQL and adapters like JDBC and wrap the transactional function with `transaction { ... }`.

Anecdotally, the data type used to save amounts and identities are `ULong`, due to the lack of datatype built for financial cases in Kotlin.

Several concurrency and coroutines primitives were applied. Layers beneath the banking core were implemented with asynchronous methods to represent the server-side logics which could be executed in a highly concurrent environment. Layers above are written in blocking method calls to simulate web requests which are mainly blocking. Some pitfalls like the usage of `runBlocking`, the id counter not secured by a mutex, or some issues in testing, may exist due to my lack of mastery in coroutines or lack of experience regarding concurrent programming.

It was my intention to not use any production-ready Web frameworks to focus on system design.

