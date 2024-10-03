# Advanced Word Hunt Solver

## Overview
The Advanced Word Hunt Solver is a Java program designed to solve complex crossword puzzles. It utilizes a recursive backtracking algorithm to fill in letters based on given constraints, such as fixed letters, blocked cells, and frequency limits imposed by digit values.

## Features
- **Dynamic Filling**: Automatically fills the board using a backtracking algorithm, exploring multiple configurations until a valid solution is found.
- **Constraint Handling**: Supports fixed letters, blocked cells, and cells that specify the maximum frequency of letters.
- **Dictionary Validation**: Ensures that all formed words are valid by checking against a provided dictionary.
- **Performance Metrics**: Logs runtimes for various puzzle configurations to analyze efficiency.
- **User-Friendly Interface**: Allows users to input puzzles and receive solutions in an easy-to-read format.

## Usage
To run the solver, compile the Java files and execute the main class with the puzzle file as an argument. The program will process the input and display the solved board.

## Example Usage
```java
java CrosswordTest dict8.txt test3a.txt
```

## Requirements
- Java 11 or higher
- A text file containing the puzzle configuration

