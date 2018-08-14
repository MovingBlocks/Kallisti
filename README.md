# Kallisti

Kallisti is a library for embedding fantasy computer virtual machines inside other JVM-based systems, particularly
JVM-based game engines.

Fantasy computers, in this meaning, refer to imaginary architectures designed for computer games, versus real-world
technology. (However, it does not exclude support for the latter!)

## Why?

* Many sandbox-leaning games like to feature, or make available, an in-game computer system. Having a library which
solves the most common problems and provides compatibility with existing "fantasy" in-game codebases greatly
simplifies that.
* Having a built-in simulator allows creators to test their in-game software without using the entire game engine, which
can be a costly and CPU/GPU-intensive process.
* Having a common interface makes it easier for you to create your own, compatible designs and peripherals which work
across all games and architectures.

## Key concepts

* Separation of the APIs of the game engine and of the virtual machine.
    * "Components" can be provided by the engine, and via implementing generic interfaces and automatically being wrapped
    through a rule-based system, the computer VM can translate it into APIs it understands internally.

## How to use

There are two main ways to use Kallisti:

* Embed it in your own project (for example, [KComputers](https://github.com/Terasology/KComputers) for the Terasology
game engine),
* Run the simulator.

To run the simulator, simply compile the project with ```gradle```, then run ```java -jar KallistiSimulator.jar simulator.json```,
where ```simulator.json``` is a [valid .JSON simulator definition](https://github.com/MovingBlocks/Kallisti/wiki/Simulator).

## Supported architectures

* (WIP) OpenComputers/JNLua, based on the [OpenComputers](http://github.com/MightyPirates/OpenComputers) mod project.
