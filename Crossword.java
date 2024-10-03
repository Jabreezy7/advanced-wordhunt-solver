import java.util.HashMap;
public class Crossword implements WordPuzzleInterface
{
    
    char [] alphabet = {'a','c','b','s','i','m','t','o','d','h','r',
        'g','l','e','w','p','n','f','v','k','j','u','y','q','z','x'};  //Optimized sorting of alphabet more common letters placed first
        
    DictInterface dictionary;
    char [][] completedBoard;  // The board we will fill and return as a solution to a given board
    char [][] backupBoard;     // Backup board for backtracking

    StringBuilder [] hwords;   // Arrays of Stringbuilder objects that represents the words that are in the horizontal direction(row) and vertical direction(column)
    StringBuilder [] cwords;  

    int [] rowLastNegPos;      // Arrays of ints representing a rows last neg (blocked) position in the row and another to represent a cols last neg (blocked) position in the column
    int [] colLastNegPos; 

    HashMap<Character, Integer> charCount = new HashMap<>();  //Hashmap to keep count of how many times a letter is used on the board
    HashMap<Character, Integer> charsLimit = new HashMap<>();
    
    public char[][] fillPuzzle(char[][] board, DictInterface dictionary)
    {
        char[][] copyBoard = new char[board.length][board.length];   //Setting up backupboard
        char[][] copyCompletedBoard = new char[board.length][board.length];

        for(int i = 0; i < board.length; i++)                        // Copying all values of the original empty board into the backupBoard
        {
            for(int j = 0; j < board.length; j++)
            {
                copyBoard[i][j] = board[i][j];        
                copyCompletedBoard[i][j] = board[i][j];    
            }
        }
        backupBoard = copyBoard;
        completedBoard = copyCompletedBoard;

        initializer(board, dictionary);

        if(solve(0,0)==false)return null;    //Calling function solve which fills the board with the first solution it finds and doesn't change the board if no solution is found
        return completedBoard;
    } 



    //Start of CheckPuzzle method
    public boolean checkPuzzle(char[][] emptyBoard, char[][] filledBoard, DictInterface dictionary)
    {
        boolean validSolution = false; // Boolean value to keep track of whether or not our filled board holds all the same values as our empty board.

        initializer(emptyBoard, dictionary);

        //Checking to see if our filled board has all the exact same characters as our emptyBoard. In other words we are checking to see if there was no solution.
        for(int i = 0; i < emptyBoard.length; i++)
        {
            for(int j = 0; j < emptyBoard.length; j++)
            {
                if(Character.toLowerCase(emptyBoard[i][j])!= Character.toLowerCase(filledBoard[i][j])) validSolution = true;
            }
        }

        if(validSolution==false) return false;

        
        //We are going to iterate across the whole board
        for(int row = 0; row < filledBoard.length; row++)
        {
            for(int col = 0; col<filledBoard.length; col++)
            {
                //We must always, unconditionally, add the value of our solution board at an index or row and col to our formed words to check if they are valid or not
                addToFormedWords(row, col, filledBoard[row][col]);

                //If a character is a letter we must increment that letters frequency (amount of times it has showed up on the board).
                if(Character.isLetter(filledBoard[row][col]) == true)
                {
                    charCount.put(filledBoard[row][col], charCount.get(filledBoard[row][col])+1);
                }

                //Checking to see if the original cell had a neg value, if so that value should still be neg (Fixed value)
                if(emptyBoard[row][col] == '-' && filledBoard[row][col] != '-') return false;

                //Checking to see if the original cell was a letter, if so that value should still be the same letter (Fixed value)
                if(Character.isLetter(emptyBoard[row][col])==true && filledBoard[row][col]!= emptyBoard[row][col]) return false;

                //If a character is a digit, we must update the limit of the letter that was placed here. 
                if( Character.isDigit(emptyBoard[row][col]) == true )
                {
                    charsLimit.put(filledBoard[row][col], Character.getNumericValue(emptyBoard[row][col]) );
                }


                //Checking to see if the original cell value has a neg value. At this point we must try to detect if any strings are not words in our dictionary.
                if(emptyBoard[row][col] == '-' )
                {
                    //CASE 1: Checking to see if we are on the first cell of the board.
                    if(row==0 && col==0)
                    {
                        rowLastNegPos[row] = col;
                        colLastNegPos[col] = row;
                    }


                    //CASE 2: Checking to see if we are on any cell that is on the first row with the excpetion of the first cell on the first row
                    if(row==0 && col<=emptyBoard.length-1 && col!=0) 
                    {
                        if( (emptyBoard[row][col-1] != '-') &&  (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col-1)!=2 && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col-1)!=3))
                        {
                            return false;
                        }
                        rowLastNegPos[row] = col;
                        colLastNegPos[col] = row;
                    }


                    //CASE 3: Checking to see if we are on the first cell in a row with the exception of the first cell on the first row
                    if(col==0 && row!=0)
                    {
                        if( (emptyBoard[row-1][col] != '-') &&  (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row-1)!=2 && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row-1)!=3))
                        {
                            return false;
                        }

                        rowLastNegPos[row] = col;
                        colLastNegPos[col] = row;
                    }


                    //CASE 4: Checking to see if we are on any other remaining cells, that is, cells that are not on the first row or the first column.
                    if(col<=emptyBoard.length-1 && col!=0 && row!=0)
                    {
                        //If the cell we are evaluating at does not have a blocked cell above it or to the left of it and either of the words formed in the row or column direction are not words or prefixes and words then we must return false because we are at a neg value
                        if( (emptyBoard[row-1][col] != '-' && emptyBoard[row][col-1]!='-') && ( (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col-1)!=2 && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col-1)!=3) || (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row-1)!=2 && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row-1)!=3)))
                        {
                            return false;
                        }

                        //If the cell we are evaluating at does not have a blocked cell to the left of it and the word formed in the row direction is not a word or a prefix and a word then we must return false because we are at a neg value
                        if( (emptyBoard[row][col-1]!='-' && emptyBoard[row-1][col] =='-') && ((dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col-1)!=2 && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col-1)!=3)) )
                        {
                            return false;
                        }

                        //If the cell we are evaluating at does not have a blocked cell to above it and the word formed in the column direction is not a word or a prefix and a word then we must return false because we are at a neg value
                        if( (emptyBoard[row-1][col]!='-' && emptyBoard[row][col-1]=='-') && ((dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row-1)!=2 && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row-1)!=3)) )
                        {
                            return false;
                        }

                        //We have to remember to update the row's and column's last neg value
                        rowLastNegPos[row] = col;
                        colLastNegPos[col] = row;
                    }
                }


                //If our value is + then we only need to do a check at the end of the row to check for horizontal words and a check at the end of a column to check for vertical words.
                if(emptyBoard[row][col]=='+')
                {
                    //Checking for horizontal words at the last column
                    if(col==emptyBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col)!=2 && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1, col)!=3))
                    {
                        return false;
                    }

                    //Checking for vertical words at the last row
                    if(row==emptyBoard.length-1 && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row)!=2 && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1, row)!=3))
                    {
                        return false;
                    }
                }
            }
        }

        //Checking to see if the original cell had a digit value. If the frequency of the letter at this given cell is greater than the digit value and the frequency limit of the letter is less than or equal to the digit value we must return false
        for(int l = 0; l<emptyBoard.length; l++)
        {
            for(int k = 0; k<emptyBoard.length; k++)
            {
                if( (Character.isDigit(emptyBoard[l][k])==true) && (charCount.get(filledBoard[l][k]) > Character.getNumericValue(emptyBoard[l][k])) && charsLimit.get(filledBoard[l][k]) <= Character.getNumericValue(emptyBoard[l][k])  ) return false;
            }
        }
        
        return true;
    }
    //End of checkPuzzle method




    //Start of solve method
    public boolean solve(int row, int col)
    {
        
     if(row < completedBoard.length && col < completedBoard.length)
     {
      
        //Start of Checking for open cells
        if(completedBoard[row][col]=='+')
        {   

            for(int i = 0; i < alphabet.length; i++) //Checking for each value in the alphabet if a solution is valid
            {
                addToFormedWords(row, col, alphabet[i]);

                //CASE 1: Checking to see if we are on the final cell. A valid placement here would meet the following conditions:
                //1.the String on the row from the last blocked cell to the current cell MUST be a valid word in our dictionary
                //2.the String on the col from the last blocked cell to the current cell MUST be a valid word in our dictionary
                if( charCount.get(alphabet[i])< charsLimit.get(alphabet[i]) &&  col==completedBoard.length-1 && row==completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col) == 2 || dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )== 3) && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row ) == 2 || dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row)== 3))
                {
                    placeLetter(row, col, i);
                    return true;
                }

                //CASE 2: Checking to see if we are on the final row. A valid placement here would meet the following conditions:
                //1.the string on the row from the last blocked cell to the current cell can either be a prefix or a word
                //1. the string on the col from the last blocked cell to the current cell MUST be a valid word in our dictionary
                if(charCount.get(alphabet[i])< charsLimit.get(alphabet[i]) && row==completedBoard.length-1 && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row) == 2 || dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row )== 3) && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )!=0)
                {
                    placeLetter(row, col, i);

                    if(solve(row,col+1) == true) return true;    //recursive portion
                    
                    unplaceLetter(row, col, i);                  //backtracking portion
                }


                //CASE 3: Checking to see if we are on a final column in a row. A Valid placement here would meet the following conditions:
                //1. the string on the row from the last blocked cell to the current cell MUST be a valid word in our dictionary
                //2. the string on the col from the last blocked cell to the current cell can either be a prefix or a word
                if( charCount.get(alphabet[i])< charsLimit.get(alphabet[i]) &&  col==completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col ) == 2 || dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col)== 3) && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row)!=0)
                {
                    placeLetter(row, col, i);

                    if(solve(row+1,0) == true) return true;
                    
                    unplaceLetter(row, col, i);
                }

                //CASE 4: Checking to see if we are on any cells that aren't on the final row or the final column in a row. A valid placement here would meet the following conditions:
                //1. the string on the row from the last blocked cell to the current cell can either be a prefix or a word
                //2. the string on the col from the last blocked cell to the current cell can either be a prefix or a word
                if( charCount.get(alphabet[i])< charsLimit.get(alphabet[i]) && col<completedBoard.length-1 && row!=completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col ))!=0 && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row)) !=0)
                {
                    placeLetter(row, col, i);

                    if(solve(row, col+1) == true) return true;

                    unplaceLetter(row, col, i);
                    removeFromFormedWords(row, col);
                }

                //No valid insertion at the current letter so we must delete the invalid letter from our formed Strings and try the other letters.
                else
                {
                    removeFromFormedWords(row, col);
                }
            }
        } 
        //End of checking for open cells


        //Start of checking for cells with digits
        if(Character.isDigit(completedBoard[row][col])==true)    
        {            

            int limit = Character.getNumericValue(completedBoard[row][col]);

            for(int i = 0; i < alphabet.length; i++) //for each letter in the alphabet
            {
                int preLimit = charsLimit.get(alphabet[i]);
                if(charCount.get(alphabet[i]) <= limit-1)  // Checking to see if the letters frequency (amount of times on the board) is less than or equal to the allowed limit-1(because we are accounting for when that letter is placed)
                {
                    addToFormedWords(row, col, alphabet[i]);

                //CASE 1: Checking to see if we are on the final cell.
                if(col==completedBoard.length-1 && row==completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col) == 2 || dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )== 3) && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row ) == 2 || dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row)== 3))
                {
                    placeAtDigitCell(row, col, i, limit);
                    return true;
                }
 
                //CASE 2: Checking to see if we are on the final row.
                if(row==completedBoard.length-1 && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row) == 2 || dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row )== 3) && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )!=0)
                {
                    placeAtDigitCell(row, col, i, limit);

                    if(solve(row,col+1) == true) return true;   //recursive portion

                    unplaceAtDigitCell(row, col, i, preLimit);  //backtracking portion

                }

                //CASE 3: checking to see if we are on the final column in a row
                if(col==completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col ) == 2 || dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col)== 3) && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row)!=0)
                {
                    placeAtDigitCell(row, col, i, limit);

                    if(solve(row+1,0) == true) return true;

                    unplaceAtDigitCell(row, col, i, preLimit);
                    
                }

                //CASE 4: Checking to see if we are on any internal cells, cells that are not:
                //1. on the final row
                //2. on the final column
                if(col<completedBoard.length-1 && row!=completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col ))!=0 && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row)) !=0)
                {
                    placeAtDigitCell(row, col, i, limit);

                    if(solve(row, col+1) == true) return true;

                    unplaceAtDigitCell(row, col, i, preLimit);

                    removeFromFormedWords(row, col);
                }

                // No valid insertion at the current letter so we must delete the invalid letter from our formed Strings and try the other letters.
                else
                {
                    removeFromFormedWords(row, col);
                }
                }
            }
            return false;
        }
        //End of checking for cells with digits


        //Start of checking for cells with fixed characters 
        if(Character.isLetter(completedBoard[row][col])==true)
        {
            charCount.put(completedBoard[row][col],charCount.get(completedBoard[row][col])+1);
            addToFormedWords(row, col, completedBoard[row][col]);


            //CASE 1: Checking to see if we are on the final cell in the board
            if(col==completedBoard.length-1 && row==completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col ) == 2 || dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )== 3) && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row ) == 2 || dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row )== 3))
            {
                return true;
            }

            //CASE 2: Checking to see if we are on the final row in the board
            if(row==completedBoard.length-1 && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row ) == 2 || dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row )== 3) && dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )!=0)
            {
                if(solve(row,col+1) == true) return true;   //recursive portion
            }


            //CASE 3: Checking to see if we are on the final column in the board
            if(col==completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col ) == 2 || dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )== 3) && dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row )!=0)
            {
                if(solve(row+1,0) == true) return true;
            } 

            //CASE 4: Checking to see if we are on any internal cells, cells that are not:
            //1. on the final row
            //2. on the final column
            if(col<completedBoard.length-1 && row!=completedBoard.length-1 && (dictionary.searchPrefix(hwords[row], rowLastNegPos[row]+1,col )!=0) && (dictionary.searchPrefix(cwords[col], colLastNegPos[col]+1,row )!=0) )
            {
                if(solve(row, col+1) == true) return true;
            }


            //If code reaches this point then there is no possible solution with the current board we must retstore our values and return false
            removeFromFormedWords(row, col);
            charCount.put(completedBoard[row][col],charCount.get(completedBoard[row][col])-1);
            return false;

        } //End of checking for cells with fixed characters


        //Start of checking for blocked cells (cells with -)
        if(completedBoard[row][col]=='-')
        {

            int rowPrevNegPos = rowLastNegPos[row];     //Keeping track of our previous blocked cell so that we can restore that value if no solution is found
            int colPrevNegPos = colLastNegPos[col];

            addToFormedWords(row, col, '-');

            //CASE 1: Checking to see if we are on the last cell of the board 
            if(col==completedBoard.length-1 && row==completedBoard.length-1)
            {
                //If the cell we are evaluating at has a neg cell on top of it and to the left of it we dont need to check for valid words in both the row and column direction and we are on the last cell of the board so we can return true.
                if(completedBoard[row][col-1] == '-' && completedBoard[row-1][col]=='-')
                {
                    adjustLastNegPos(row, col);
                    return true;
                }

                //If the cell we are evaluating at has a neg cell to the left of it and letter on top of it we dont need to check for valid words in the row direction but we do need to make sure that the word formed in the column direction is either a word or a word and a prefix
                if(completedBoard[row][col-1] == '-' && completedBoard[row-1][col]!='-' && (dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==2 || dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==3) )
                {
                    adjustLastNegPos(row, col);
                    return true;
                }

                //If the cell we are evaluating at has a neg cell on top of it and a letter to the left of it we dont need to check for valid words in the column direction but we do need to make sure that the word formed in the row direction is either a word or a word and a prefix
                if(completedBoard[row-1][col]=='-' && completedBoard[row][col-1]!='-' && (dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==2 || dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==3))
                {
                    adjustLastNegPos(row, col);
                    return true;
                }


                //If the cell we are evaluating at doesn't have a neg cell to the left of it or on top it then we need to make sure that the words formed in both the row and column direction are either words or words and prefixes
                if(((dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==2 || dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==3) && (dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==2 || dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==3)))
                {
                    adjustLastNegPos(row, col);
                    return true;
                }
            }


            //CASE 2: Checking to see if we are on the first cell on the board
            if(col==0 && row==0)
            {
                adjustLastNegPos(row, col);

                if(solve(row,col+1) == true) return true;
            }

            
            //CASE 3: Checking to see if we are on the first row with the exception of the first cell on the first row
            if(row==0 && col!=0)
            {
                if( (completedBoard[row][col-1] == '-') ||  (dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==2 || dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==3)   )
                {
                    adjustLastNegPos(row, col);

                    if(col==completedBoard.length-1 && solve(row+1,0)==true) return true;

                    if(col<completedBoard.length-1 && solve(row,col+1)==true) return true;
                } 
            }


            //CASE 4: Checking to see if we are on the first column with the exception of the first cell on the first column
            if(col==0 && row!=0)
            {
                if(  (completedBoard[row-1][col] == '-') ||  (dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==2 || dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==3) )
                {
                    adjustLastNegPos(row, col);

                    if(solve(row,col+1) == true) return true;
                }
            }


            //CASE 5: Checking to see if we are on any internal cells, cells that are not:
            //1. on the first row
            //2. on the first column
            //3. on the last cell
            if(col<=completedBoard.length-1 && row!=0 && col!=0)
            {
                //If the cell we are evaluating at has a neg cell to the left of it and on top of it we dont need to check for valid words and we can recurse
                if(completedBoard[row][col-1] == '-' && completedBoard[row-1][col]=='-')
                {
                    adjustLastNegPos(row, col);
                    if(col==completedBoard.length-1 && solve(row+1,0)==true) return true;
                    if(col<completedBoard.length-1 && solve(row,col+1)==true) return true;
                }

                //If the cell we are evaluating at has a neg cell to the left of it and letter on top of it we dont need to check for valid words in the row direction but we do need to make sure that the word formed in the column direction is a word or a word and a prefix
                if(completedBoard[row][col-1] == '-' && completedBoard[row-1][col]!='-' && (dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==2 || dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==3) )
                {
                    adjustLastNegPos(row, col);
                    if(col==completedBoard.length-1 && solve(row+1,0)==true) return true;
                    if(col<completedBoard.length-1 && solve(row,col+1)==true) return true;
                }

                //If the cell we are evaluating at has a neg cell on top of it and letter to the left of it we dont need to check for valid words in the column direction but we do need to make sure that the word formed in the row direction is a word or a word and a prefix
                if(completedBoard[row-1][col]=='-' && completedBoard[row][col-1]!='-' && (dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==2 || dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==3))
                {
                    adjustLastNegPos(row, col);
                    if(col==completedBoard.length-1 && solve(row+1,0)==true) return true;
                    if(col<completedBoard.length-1 && solve(row,col+1)==true) return true;
                }


                //If the cell we are evaluating at doesn't have a neg cell to the left of it or on top it then we need to make sure that the words formed in both the row and column direction are either words or words and prefixes
                if(((dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==2 || dictionary.searchPrefix(hwords[row],rowLastNegPos[row]+1,col-1)==3) && (dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==2 || dictionary.searchPrefix(cwords[col],colLastNegPos[col]+1,row-1)==3)))
                {
                    adjustLastNegPos(row, col);
                    if(col==completedBoard.length-1 && solve(row+1,0)==true) return true;
                    if(col<completedBoard.length-1 && solve(row,col+1)==true) return true;
                }

            }

            //If code reaches this point then there is no possible solution with the current board we must return false
            removeFromFormedWords(row, col);
            rowLastNegPos[row] = rowPrevNegPos;
            colLastNegPos[col] = colPrevNegPos;
            return false;
        }
        //End of checking for blocked cells (cells with -)
    
      }
      
      return false;
    } //End of solve method





    //Start of Helper Methods

    //This method intializes all of our needed values and data structures using a given board and dictionary
    public void initializer(char [][] board, DictInterface dictionary)
    {
        this.dictionary = dictionary;
        hwords = new StringBuilder[board.length];    //initializing arrays of Stringbuilder objects in the row and column direction to be of length = board.length and all String builder values to be of value ""
        cwords = new StringBuilder[board.length];
        rowLastNegPos = new int[board.length];      //Initializing the arrays of lastNegPos in the row and column direction to be of length = board.length, and all values to -1
        colLastNegPos = new int[board.length];

        for(int i = 0; i < board.length; i++)
        {                                            
            hwords[i] = new StringBuilder("");
            cwords[i] = new StringBuilder("");
        }

        for(int i = 0; i < alphabet.length; i++)         //Setting up the keys in my hashmap to be all the letters in the alphabet with an initial count value of 0 (no letter has been used yet)
        {
            charCount.put(alphabet[i],0);
            charsLimit.put(alphabet[i], (board.length*2)+1);
        }

        for(int i = 0; i < board.length; i++)
        {
            rowLastNegPos[i] = -1;
            colLastNegPos[i] = -1;
        }
    }


    //This method will adjust the last row's or column's negative (blocked) position given a row and column.
    public void adjustLastNegPos(int row, int col)            
    {
        rowLastNegPos[row] = hwords[row].lastIndexOf("-");
        colLastNegPos[col] = cwords[col].lastIndexOf("-");
    }


    //This method will place a letter from our alphabet at a given i value to a given row and col on our filled board
    public void placeLetter(int row, int col, int i)
    {
        completedBoard[row][col] = alphabet[i];
        charCount.put(alphabet[i],charCount.get(alphabet[i])+1);
    }

    //This method will unplace a letter from our alphabet at a given i value to a given row and col on our filled board. (Backtracking helper method)
    public void unplaceLetter(int row, int col, int i)
    {
        completedBoard[row][col] = backupBoard[row][col];  
        charCount.put(alphabet[i],charCount.get(alphabet[i])-1);
    }

    //This method will act the same as place letter except for the fact that this method deals with original empty board values that contain numbers.
    //In this case we will also have to adjust the limit on the amount of times that letter can show up on our board.
    public void placeAtDigitCell(int row, int col, int i, int limit)
    {
        completedBoard[row][col] = alphabet[i];
        charCount.put(alphabet[i],charCount.get(alphabet[i])+1);
        charsLimit.put(alphabet[i],limit);
    }


    //This method will unplace a letter that was placed in a cell that originally contained a digit.
    //The method will also restore the letters limit frequency. (Backtracking helper method)
    public void unplaceAtDigitCell(int row, int col, int i, int preLimit)
    {
        completedBoard[row][col] = backupBoard[row][col];
        charCount.put(alphabet[i],charCount.get(alphabet[i])-1);
        charsLimit.put(alphabet[i],preLimit);
    }


    //This method will add a character to our arrays of horizontal words and column words.
    public void addToFormedWords(int row, int col, char letter )
    {
        hwords[row].append(letter);     // Adding a character to our StringBuilder objects so we can deteremine if our String is a prefix or word
        cwords[col].append(letter);
    }


    //This method will remove a character from our arrays of horizontal words and column words. (Backtracking helper method)
    public void removeFromFormedWords(int row, int col)
    {
        hwords[row].deleteCharAt(col);
        cwords[col].deleteCharAt(row);
    }

    //End of helper methods

}
//End of Class
