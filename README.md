# Kallisti

Kallisti is a library for embedding fantasy computer virtual machines inside other JVM-based systems, particularly
JVM-based game engines.

Fantasy computers, in this meaning, refer to imaginary architectures designed for computer games, versus real-world
technology. (However, it does not exclude support for the latter!)

Note: Documentation is a work in progress.

## Why?

* Many sandbox-leaning games like to feature, or make available, an in-game computer system. Having a library which
solves the most common problems and provides compatibility with existing "fantasy" in-game codebases greatly
simplifies that.
* Having a built-in simulator allows creators to test their in-game software without using the entire game engine, which
can be a costly and CPU/GPU-intensive process.
* Having a common interface makes it easier for you to create your own, compatible designs and peripherals which work
across all games and architectures.

## Key concepts

* "Components" can be provided by the engine, and via implementing generic interfaces and automatically being wrapped
through a rule-based system, the computer VM can translate it into APIs it understands internally.

## Supported architectures

* (WIP) OpenComputers/JNLua, based on the [OpenComputers](http://github.com/MightyPirates/OpenComputers) mod project.