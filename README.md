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

# Implementation Process and Project Analyzation

## Plan of Attack
Upon the intital inspection of Assignement 1, I quickly noticed that this would grow to become a very complex assignment with many different moving parts and restrictions. I realized that if I were to solve this assignement in an orderely fashion, I would need to decompose the task of creating a crossword-filling algorithim into many smaller tasks that in the end meet the goal of solving the puzzle. 

Those smaller tasks were:
1. puzzles that had no restraints (all cells in empty board are filled with "+")
2. puzzles with fixed letters in cells
3. puzzles with neg values in cells (cells with "-")
4. puzzles with digit values in cells
5. Creating a fully functional program that seamlessly integrates all of the previous tasks into one.


## Data Structures Used
I realized that arrays (1d and 2d) and hashmaps would be the main data structures I needed.

The Arrays I Used:
1. char completeBaord [][] - this 2d array represents our solution to any given crossword puzzle (this is out return value to fillPuzzle)
2. char backupBoard [][] - this 2d array represents our initial given crossword puzzle, this data data structure is needed for backtracking.
3. char alphabet [] - this array represents all of the letters in the alphabet. I ordered the letters in a way that pushed the less common letters to the end of the array.
4. Stringbuilder hwords[] - this array represented our formed words in the horizontal or row direction
5. Stringbuilder cwords [] - this array represented our formed words in the vertical or column direction
6. int rowLastNegPos [] - this array represented the last neg or blocked cell's index at a row
7. int colLastNegPos [] - this array represented the last neg or blocked cell's index at a col


The Hashmaps I Used:
1. HashMap<Character, Integer> charCount - this hashmap helps keep track of the amount of times that each letter in our alphabet array has showed up on our completed board.
2. HashMap<Character, Integer> charsLimit - this hashmap helps keep track of the frequency limit of each letter in our alphabet array. (initial limit for each letter is the total amount of cells in the board)


## General Algorithim Procedure for fillPuzzle method
My algorithim proceeded by first checking what value our board contained at a given cell. The only values that our board could contain are either +,-, a letter, or a digit from 1-9. 

If that value was a '+' then we would iterate across the entire alphabet until we could find a letter that could be placed in the board without violating any rules of the game and then increment its frequency by 1 and recurse to the next cell. Otherwise backtrack to the previous cell.

If that value was a '-' then we had to check if the string formed in the column direction starting from the last neg pos+1 to our row-1 was a word and we also had to make sure that word formed in the row direction starting from the last neg pos+1 to our col-1 was a word. If the string in the row and column direction were words then we could recurse to the next cell, if not then we would return false and backtrack to the previous cell. 

If that value was a letter then we had to check that when the letter is placed it does not invalidate our board by breaking any other rules, if it doesn't then recurse to the next cell, if it does then backtrack to the previous cell.

If that value was a digit from 1-9 we had to check iterate across our whole alphabet and find a letter that had a frequency that was either less than or equal to the digit value in that cell, and that the letter would not invalidate our board by breaking any other rules. If the letter placed didn't violate any rules recurse to the next cell. Otherwise backtrack to the previous cell.

In all of these cases if a valid insertion was possible at the final cell on our board we do not need to recurse as we can just return true.
In all of these cases if a valid insertion was not possible at the first cell on our board we can simply return false.


## General Algorithim Procedure for checkPuzzle method
My algorithim first checked to see if all the values in our solution board were equal to all the values in the empty board, if so we must return false because that means that we did not fill in the puzzle. Then we would iterate across the entire board and check to see if the blocked cells on the empty board were still blocked on the solution board and if the fixed letters on the empty board were still fixed on the solution board. Then we had to check if all the strings formed up to a blocked cell or a final row or column were words. Finally, we would check to see if the frequency of the letter stored at a digit value was less than or equal to that digit value.


## Coding/Debugging Issues I Ran Into
I ran into a lot of null pointer exception errors this was mainly due to not initializing the values inside my data structures. For example, the first null pointer exception that I got occured when I tried to append a character to my Stringbuilder objects inside my hwords and cwords arrays. I solved this error by first initializing the values inside my arrays of Strinbuilder objects with the empty string. I got the same error for a couple of other data structures so I just decided to make an initializer method that did all of the intializing for me.

When I first tried to deal with the case in which cells had digit values, I ran into a logical error that occurred whenever the board contained more than 1 digit value. My solution would print that the solution was true and my algorithim would allow for a letter to appear more than the allowed amount of times. I solved this by implementing the charsLimit hashmap that allowed me to store a letters frequency limit.




## Approximate Runtimes for Various Files (Estimated runtimes are denoted with ~)

|Given File|Runtime for fillPuzzle Method|Runtime(ms) for checkPuzzle Method|
|:-|:-|:-|
|test3a.txt|40 ms|<1 ms|
|test3b.txt|469 ms|<1 ms|
|test4a.txt|1381 ms|<1 ms|
|test4b.txt|1677 ms|<1 ms|
|test4c.txt|338 ms|<1 ms|
|test4d.txt|31 ms|<1 ms|
|test4e.txt|28 min|<1 ms|
|test4f.txt|675 ms|<1 ms|
|test5a.txt|2082 ms|<1 ms|
|test6a.txt|~6 hours|~<1 ms|
|test6b.txt|1821137 ms (30.3 min)|<1 ms|
|test6c.txt|~3 hours|~<1 ms|
|test7a.txt|~18 hours|~1 ms|
|test8a.txt|~39 hours|~1 ms|
|test8b.txt|~7 hours|~1 ms|
|test8c.txt|122136ms (2.03 min)|1 ms|



## Asymptotic Analysis for Worst Case Runtime of Program
The worst case runtime of our program will occur when we have exhausted all possible combinations and decided that no solution is possible. Thus, worst case runtime of our program will depend on the amount of possible combinations that could be formed. The amount of possible combinations that we can form will largely depend on:
1. The amount of open cells,C, in our board
2. The amount of possible letters, L, that could be placed at a given open cell
3. The amount of words, D, in our dictionary

Worst Case Runtime for fillPuzzle - O((L^C)*D)


The worst case runtime for checkPuzzle will occur when a solution is found to be true. Our checkPuzzle method runs in quadratic time and will simply depend on the amount of cells, R, in our board.

Worst Case Runtim for checkPuzzle - O(R^2)


Worst Case Runtime for our program - O((L^C)*D) + O(R^2)

