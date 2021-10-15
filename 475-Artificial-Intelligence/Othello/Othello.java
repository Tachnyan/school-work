import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/*
* Assignment 3 - Othello with minmax and beta pruning
* CSC 475 - Artificial Intelligence
* This program is the game Othello, that can be played by two players
* or by one player against an AI.
*
* implement option to use alpha beta pruning (on by default).
*/





public class Othello {

    static Scanner sc = new Scanner(System.in); //Scanner for user input

    static char[][] boardState = new char[8][8];//for storing current board state

    //values for dictating board spaces
    final static char BLACK = 'b';
    final static char WHITE = 'w';
    final static char EMPTY = '-';
    final static char VALID = '+';

    //determine max depth.
    final static int maxDepth = 9;


    //input method
    static String getInput()
    {
        String s = "";
        
        s = sc.nextLine();

        return s;
    }

    //Display Main menu
    static void displayMain()
    {
        System.out.println("=============================================================================");
        System.out.println("OTHELLO - CSC475 ASSIGNMENT 3 - AUTHOR: TACHNYAN");
        System.out.println();
        System.out.println("ENTER DESIRED COMMAND...");
        System.out.println();
        System.out.println("[1] Player vs AI");
        System.out.println("[2] Player vs Player");
        System.out.println("[0] Exit");
        return;

    }

    //Displays current board setup
    static void displayGame(char[][] board)
    {
        System.out.println("=======================================================================");
        System.out.println();
        System.out.println("+ A B C D E F G H");
        for(int i = 0; i < 8; i++)
        {
            System.out.print(i + " ");
            for(int j = 0; j < 8; j++)
            {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    //initializes the board for a new game
    static void initializeBoard()
    {
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                boardState[i][j] = EMPTY;
            }
        }

        boardState[3][3] = WHITE;
        boardState[3][4] = BLACK;
        boardState[4][3] = BLACK;
        boardState[4][4] = WHITE;

        return;
    }

    //Game state manager
    static void gameState(String state)
    {
        //gamestates
        switch(state)
        {
            case "-1": //main menu
                displayMain();
                return;
            case "1": //player vs AI
                initializeBoard();
                gameLoop("1");
                break;
            case "2": //Player vs Player
                initializeBoard();
                gameLoop("2");
                break;
            case "0":
                System.exit(0);
                break;
            default: //error
                System.out.println("Invalid Command: Input desired command..");
                return;
        }
        displayMain();
        return;
    }
    //Flipping Method
    static void flip(int row, int col, char t, char[][] board)//##CHANGED: ADDED IN ABILITY TO REFERENCE THE BOARD WE'RE INTERACTING WITH INSTEAD OF JUST CHANGING THE BOARDSTATE. THIS LETS FUNCTION GET USED IN MINIMAX()
    {

        if(row != 0 && (board[row-1][col] != t && board[row-1][col] != '-'))//check above //##CHANGED: THIS AND ALL FOLLOWING IF STATEMENTS WERE CHANGED TO INTERACT WITH THE BOARD BEING PASSED IN, ORIGINALLY CHANGED THE BOARDSTATE.
        {
            for(int i = row - 1; i >= 0; i-- )
            {
                if(board[i][col] == t)
                {
                    for(i++;i < row; i++) board[i][col] = t;
                    break;
                }
                if(board[i][col] == '-') break;
            }
        }
        if(row != 7 && (board[row+1][col] != t && board[row+1][col] != '-'))//below
        {
            for(int i = row + 1; i < 8; i++ )
            {
                if(board[i][col] == t)
                {
                    for(i--; i > row; i--) board[i][col] = t;
                    break;
                }
                if(board[i][col] == '-') break;
            }        
        }
        if(col != 0 && (board[row][col-1] != t && board[row][col-1] != '-'))//left
        {
            for(int i = col - 1; i >= 0; i-- )
            {
                if(board[row][i] == t) 
                {
                    for(i++; i < col; i++) board[row][i] = t;
                    break;
                }
                if(board[row][i] == '-') break;
            }
        }
        if(col != 7 && (board[row][col+1] != t && board[row][col+1] != '-'))//right
        {
            for(int i = col + 1; i < 8; i++ )
            {
                if(board[row][i] == t) 
                {
                    for(i--; i > col; i--) board[row][i] = t;
                    break;
                }
                if(board[row][i] == '-') break;
            }
        }
        if((row != 0 && col !=0 ) && (board[row-1][col-1] != t && board[row-1][col-1] != '-'))//up left
        {
            for(int i = 1; i <= Math.min(row, col); i++ )
            {
                if(board[row-i][col-i] == t) 
                {
                    for(i--; i > 0; i--) board[row-i][col-i] = t;
                    break;
                }
                if(board[row-i][col-i] == '-') break;
            }
        }
        if((row != 0 && col !=7 ) && (board[row-1][col+1] != t && board[row-1][col+1] != '-'))//up right
        {
            for(int i = 1; i <= Math.min(row, 7 - col); i++ )
            {
                if(board[row-i][col+i] == t) 
                {
                    for(i--; i > 0; i--) board[row-i][col+i] = t;
                    break;
                }
                if(board[row-i][col+i] == '-') break;
            }
        }
        if((row != 7 && col !=0 ) && (board[row+1][col-1] != t && board[row+1][col-1] != '-'))//down left
        {
            for(int i = 1; i <= Math.min(7 - row, col); i++ )
            {
                if(board[row+i][col-i] == t) 
                {
                    for(i--; i > 0; i--) board[row+i][col-i]=t;
                    break;
                }
                if(board[row+i][col-i] == '-') break;
            }
        }
        if((row != 7 && col !=7 ) && (board[row+1][col+1] != t && board[row+1][col+1] != '-'))//down right
        {
            for(int i = 1; i <= (7 - Math.max(row, col)); i++ )
            {
                if(board[row+i][col+i] == t) 
                {
                    for(i--; i > 0; i--) board[row+i][col+i] = t;
                    break;
                }
                if(board[row+i][col+i] == '-') break;
            }
        }
    }
    
    //place method
    static boolean place(char let, char num, char t)
    {
        int col = let;
        int row = num;
        
        //convert 'let' into useable int col
        if(col >= 65 && col <= 72) 
        {
            col = col - 65;
        }else if(col >= 97 && col <= 104) 
        {
            col = col - 97;
        }else 
        {
            System.out.println("invalid input");
            return false;
        }
        // convert 'num' into useable int row
        if(row >= 48 && row <= 55)
        {
            row = row - 48;
        }else
        {
            System.out.println("Invalid input");
            return false;
        }

        //make sure valid placement location
        if(!canPlace(row, col, t)) 
        {
            System.out.println("Can't Place in that Spot");
            return false;
        }

        //flip pieces
        if(t == BLACK)
        {
            boardState[row][col] = BLACK;
            flip(row, col, BLACK, boardState); //##CHANGED: REFLECTS CHANGES TO FLIP METHOD
        }else
        {
            boardState[row][col] = WHITE;
            flip(row, col, WHITE, boardState); //##CHANGED: REFLECTS CHANGES TO FLIP METHOD
        }
        return true;

    }

    //check if valid location
    static boolean canPlace(int row, int col, char t)
    {
        if(boardState[row][col] != '-') return false; //make sure spot is empty

        boolean can = false;
        if(row != 0 && (boardState[row-1][col] != t && boardState[row-1][col] != '-'))//check above
        {
            for(int i = row - 1; i >= 0; i-- )
            {
                if(boardState[i][col] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[i][col] == '-') break;

            }
        }
        if(row != 7 && (boardState[row+1][col] != t && boardState[row+1][col] != '-'))//below
        {
            for(int i = row + 1; i < 8; i++ )
            {
                if(boardState[i][col] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[i][col] == '-') break;
            }        
        }
        if(col != 0 && (boardState[row][col-1] != t && boardState[row][col-1] != '-'))//left
        {
            for(int i = col - 1; i >= 0; i-- )
            {
                if(boardState[row][i] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[row][i] == '-') break;
            }
        }
        if(col != 7 && (boardState[row][col+1] != t && boardState[row][col+1] != '-'))//right
        {
            for(int i = col + 1; i < 8; i++ )
            {
                if(boardState[row][i] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[row][i] == '-') break;
            }
        }
        if((row != 0 && col !=0 ) && (boardState[row-1][col-1] != t && boardState[row-1][col-1] != '-'))//up left
        {
            for(int i = 1; i <= Math.min(row, col); i++ )
            {
                if(boardState[row-i][col-i] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[row-i][col-i] == '-') break;
            }
        }
        if((row != 0 && col !=7 ) && (boardState[row-1][col+1] != t && boardState[row-1][col+1] != '-'))//up right
        {
            for(int i = 1; i <= Math.min(row, 7 - col); i++ )
            {
                if(boardState[row-i][col+i] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[row-i][col+i] == '-') break;
            }
        }
        if((row != 7 && col !=0 ) && (boardState[row+1][col-1] != t && boardState[row+1][col-1] != '-'))//down left
        {
            for(int i = 1; i <= Math.min(7 - row, col); i++ )
            {
                if(boardState[row+i][col-i] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[row+i][col-i] == '-') break;
            }
        }
        if((row != 7 && col !=7 ) && (boardState[row+1][col+1] != t && boardState[row+1][col+1] != '-'))//down right
        {
            for(int i = 1; i <= (7 - Math.max(row, col)); i++ )
            {
                if(boardState[row+i][col+i] == t)
                { 
                    can = true;
                    break;
                }
                if(boardState[row+i][col+i] == '-') break;
            }
        }
        return can;
    }

    //return true if forfeit turn, false elsewise.
    static boolean checkForfeit(char[][] board, char t)
    {
        boolean hold = true;
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                
                if(canPlace(i, j, t)) 
                {
                    board[i][j] = VALID;
                    hold = false; //if can place at any point don't forfeit
                }
            }
        }
        return hold; // can't place forfeit
    }

    //returns array with all valid and non valid moves. 
    static char[][] checkValidMoves(char[][] board, char t)
    {
        char[][] validty = new char[8][8];
        boolean hold = true;
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(canPlace(i, j, t)) 
                {
                    hold = false;
                    validty[i][j] = 'v';
                }else validty[i][j] = 'n';
            }
        }
        return validty;
    }

    //get determine winner, then display message. 
    static void gameOver(int turnCount)
    {
        int black = 0, white = 0;
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(boardState[i][j] == BLACK) black++;
                else if(boardState[i][j] == WHITE) white++;
            }
        }
        if(black > white) System.out.println("BLACK WON IN " + turnCount + " TURNS.");
        else if(white > black) System.out.println("WHITE WON IN " + turnCount + " TURNS.");
        else if(black == white) System.out.println("MATCH ENDED IN A DRAW IN " + turnCount + " TURNS.");
        else System.out.println("NOBODY WON, AND I DON'T KNOW HOW");
    }


    static void gameLoop(String state)
    {
        //variables
        int turnCounter = 0;
        char let = ' ', num = ' ', curPlayer = BLACK, AI = ' ';
        String in = "";
        boolean prune = true;
        boolean debug = false;

        char[][] display = copyBoard(boardState);

        if(state == "1") //check if playing against AI
        {
            while(AI == ' ')
            {
                System.out.println("choose your color.. b/w");
                in = getInput();
                if(in.equalsIgnoreCase("b")) AI = WHITE;
                else if(in.equalsIgnoreCase("w")) AI = BLACK;
                else System.out.println("Invalid option..");
            }
        }

        while (true)
        {
            
            display = copyBoard(boardState);

            if(checkForfeit(display, curPlayer)) //check if turn forfeit
            {
                display = copyBoard(boardState);
                if(curPlayer == BLACK) curPlayer = WHITE;
                else curPlayer = BLACK;
                if(checkForfeit(display, curPlayer)) //check if game over
                {
                    gameOver(turnCounter);
                    return;
                }
            }
            
            displayGame(display);

            //message to let players know who's turn it is
            if(curPlayer == BLACK && curPlayer != AI)
            {
                System.out.println("BLACK TURN: INPUT COMMAND..");
            }else if(curPlayer == WHITE && curPlayer != AI)
            {
                System.out.println("WHITE TURN: INPUT COMMAND..");
            }

            if (curPlayer != AI)//get input if player is not AI
            {
                in = getInput();
                if (in.equalsIgnoreCase("quit"))
                    return;
                if(in.contains("debug:on")) debug = true;
                if(in.contains("debug:off")) debug = false;
                if(in.contains("pruning:on")) prune = true;
                if(in.contains("pruning:off")) prune = false;
                if (in.matches("[a-zA-Z][0-7].*")) { //CHANGED: ORIGINAL USED + IN THE REGEX, MINOR MISTAKE THAT FORCES YOU TO ADD ANY 3RD CHARACTER TO YOU INPUT COMMAND. + MEANS ONE OR MORE, * IS 0 OR MORE AND WAS WHAT I MEANT TO USE.
                    let = in.toCharArray()[0];
                    num = in.toCharArray()[1];
                    if (place(let, num, curPlayer)) {
                        turnCounter++;
                        if (curPlayer == BLACK)
                            curPlayer = WHITE;
                        else
                            curPlayer = BLACK;
                    }
                } else
                    System.out.println("Invalid Position..");
            }else//AI Input handled here
            {
                in = aiGetMove(copyBoard(boardState), AI, prune, debug);
                let = in.toCharArray()[0];
                num = in.toCharArray()[1];
                place(let, num, curPlayer);
                turnCounter++;
                if(curPlayer == BLACK)
                    curPlayer = WHITE;
                else
                    curPlayer = BLACK;
                
            }

            

        }
    }

    static String aiGetMove(char[][] board, char t, boolean pruning, boolean debug)
    {
        int[] track = {0};
        int minScore = 9999, maxScore = -9999;
        int alpha = -1000, beta = 1000;
        char[][] temp = checkValidMoves(board, t); //get valid moves
        String command = "";
        //List<String> moves = new ArrayList<String>();
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(temp[i][j] == 'v')
                {
                    track[0]++;
                    int col = 'A' + j, row = '0' + i;
                    char let = (char)col, num = (char)row;
                    if(debug)System.out.println("Node 0: Check pos " + let +":"+ num ); //##ADDED: DEBUG ABILITY
                    int tempscore = miniMax(copyBoard(board), t, t, i, j, maxDepth-1, alpha, beta, pruning, debug, track); //start minmaxing through all valid moves
                    if(debug)System.out.println("temp:"+tempscore); //##ADDED: DEBUG ABILITY
                    if(tempscore > maxScore)
                    {
                        maxScore = tempscore;
                        command = ""+let+num; 
                        if(debug)System.out.println("New Best Choice: " +command); //##ADDED: DEBUG ABILITY
                    }
                    if(pruning)
                    {
                        if(alpha < tempscore) alpha = tempscore;//if maximizer and found higher min, update alpha
                        else if(beta <= maxScore) 
                        {
                            if(debug)System.out.println("Beta Prune");//##ADDED: DEBUG ABILITY
                            break;//if maximizer, and found higher max than beta, stop
                        }
                        
                    }
                }
            }
        }
        System.out.println("AI chose: "+command+"  AI looked through "+track[0]+" states.");
        return command; //return command of best score
    }


    static char[][] copyBoard(char[][] origin) //copy board without reference
    {
        char[][] arr = new char[8][8];
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                arr[i][j] = origin[i][j];
            }
        }
        return arr;
    }

    static int hueristic(char[][] board, char ai) //hueristic is just AIscore - player score
    {
        char opp = ' ';
        if(ai == BLACK) opp = WHITE;
        else opp = BLACK;

        int aiScore = getScore(board, ai);
        int oppScore = getScore(board, opp);

        return (aiScore - oppScore);

    }

    static int miniMax(char[][] board, char ai, char t, int row, int col, int depth, int alpha, int beta, boolean pruning, boolean debug, int[] track)
    {  
        int maxScore = -9999;
        int minScore = 9999;

        char let = (char)(col + 65), num = (char)(row + 48);

        flip(row, col, t, board); //##CHANGED: ORIGINALLY INSERTED POSITION BUT DIDN'T FLIP TILES PROPERLY RUINING THE HUERISTIC CHECK.
        if(depth == 0) //return hurestic for AI if at last depth
        {
            if(debug)System.out.println("hueristic Score: " + hueristic(board, ai));
            return hueristic(board, ai);
        }

        

        char nxtPlayer = ' ';
        if(t == BLACK) nxtPlayer = WHITE;
        else nxtPlayer = BLACK;

        if(checkForfeit(copyBoard(board), nxtPlayer)) //check forfeit and game over possibilty. 
        {
            if(checkForfeit(copyBoard(board), t))
            {
                return hueristic(board, ai) * 10; //if end game, either best value or worst value
            }else nxtPlayer = t;
        }

        char[][] temp = checkValidMoves(board, nxtPlayer);
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(temp[i][j] == 'v')
                {
                    track[0]++;
                    //maxscore holds best, min holds lowest
                    if(debug)System.out.println("Node Level "+ (maxDepth-depth)+": Check Pos " +(char)(65+j)+":"+(char)(48+i));//ADDED: DEBUG ABILITY
                    int tempscore = miniMax(copyBoard(board), ai, nxtPlayer, i, j, depth-1, alpha, beta, pruning, debug, track);//##CHANGED: ORIGINAL PASSES MINSCORE AND MAXSCORE AS ALPHA AND BETA, A SIMPLE BUT MASSIVE MISTAKE ON MY PART. 
                    if(tempscore > maxScore) maxScore = tempscore;  
                    if(tempscore < minScore) minScore = tempscore;
                    if(pruning)
                    {
                        if(alpha >= minScore && nxtPlayer != ai) 
                        {
                            if(debug)System.out.println("Alpha Prune");//##ADDED: DEBUG ABILITY
                            return alpha;//if minimizer and lower than alpha stop
                        }
                        else if(alpha < tempscore && nxtPlayer == ai) alpha = tempscore;//if maximizer and found higher min, update alpha
                        else if(beta <= maxScore && nxtPlayer == ai) 
                        {
                            if(debug)System.out.println("Beta Prune");//##ADDED: DEBUG ABILITY
                            return beta;//if maximizer, and found higher max than beta, stop
                        }
                        if(beta > tempscore && nxtPlayer != ai) beta = tempscore;//if minimizer and found lower than beta, update beta. 
                    }            
                }
                    
            }
        }



        if(ai == nxtPlayer) //return max or min depending on if maximizer or minimizer
        {
            return maxScore;
        }
        else return minScore;

    }

    static int getScore(char[][] board,char t)// get score for wanted player. 
    {
        int points = 0;
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++){
                if(board[i][j] == t) points++;
            }
        }

        return points;
    }

    public static void main(String Args[])
    {
        String state = "-1";
        while(true)
        {
            gameState(state);
            state = getInput();
        }
    }
}
