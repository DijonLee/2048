/**
 * @author Tyler Carberry
 * 2048
 * The main code of the game 2048
 */

import java.util.*;
public class Game implements java.io.Serializable
{
	private static final long serialVersionUID = 3356339029021499348L;

	// The main board the game is played on
	private Grid board;
	
	// Stores the previous boards and scores
	private Stack history;
	
	// The chance of a 2 appearing
	public final double CHANCE_OF_2 = .90;
	
	private int score = 0;
	private int turnNumber = 0;
	private boolean quitGame = false;
	
	// Used to start the time limit on the first move
	// instead of when the game is created
	private boolean newGame = true;
	
	// The time the game was started
	private Date d1;
	
	// Limited number of moves
	// -1 = unlimited
	private int movesRemaining = -1;
	
	// Limited number of undos
	// -1 = unlimited
	private int undosRemaining  = -1;
	
	// The time limit in seconds before the game automatically quits
	// The timer starts immediately after the first move
	private double timeLeft = -1;
	
	private boolean survivalMode = false;
	
	// If true, any tile less than the max tile can spawn
	// Ex. If the highest piece is 32 then a 2,4,8, or 16 can appear
	// All possible tiles have an equal chance of appearing
	private boolean dynamicTileSpawning = false;
	
	
	/**
	 * Creates a default game with the size 4x4
	 */
	public Game()
	{
		this(4,4);
	}
	
	
	/**
	 * @param rows The number of rows in the game
	 * @param cols The number of columns in the game
	 */
	public Game(int rows, int cols)
	{
		// The main board the game is played on
		board = new Grid(rows,cols);
		
		// Keeps track of the turn number
		turnNumber = 1;
		
		// Store the move history
		history = new Stack();
		
		// Adds 2 pieces to the board
		addRandomPiece();
		addRandomPiece();
	}
	
	/**
	 * Moves the entire board in the given direction
	 * @param direction Called using a final variable in the location class
	*/
	public void act(int direction)
	{
		// Don't move if the game is already lost or quit
		if(lost())
			return;
		
		// If this is the game's first move, keep track of
		// the starting time and activate the time limit
		if(newGame)
			madeFirstMove();
		
		// Used to determine if any pieces moved
		Grid lastBoard = board.clone();
				
		// If moving up or left start at location 0,0 and move right and down
		// If moving right or down start at the bottom right and move left and up
		List<Location> locations = board.getLocationsInTraverseOrder(direction);
		
		// Move each piece in the direction
		for(Location loc : locations)
			move(loc, direction);
		
		// If no pieces moved then it was not a valid move
		if(! board.equals(lastBoard))
		{
			turnNumber++;
			addRandomPiece();
			history.push(lastBoard, score);
			movesRemaining--;
		}
		
	}
	
	/** 
	 * Move a single piece all of the way in a given direction
	 * Will combine with a piece of the same value
	 * @param from The location of he piece to move
	 * @param direction Called using a final variable in the location class
	 */
	private void move(Location from, int direction)
	{
		// Do not move X spaces or 0 spaces
		if(board.get(from) == -1 || board.get(from) == 0) return;
				
		Location to = from.getAdjacent(direction);
		while(board.isValid(to))
		{
			// If the new position is empty, move
			if(board.isEmpty(to))
			{
				board.move(from, to);
				from = to.clone();
				to = to.getAdjacent(direction);
			}
			
			// If the new position has a piece
			else
			{
				// If they have the same value, combine
				if(board.get(from) == board.get(to))
					add(from, to);
				return;
			}
		}
	}
		
		
	
	/**
	 * Adds piece "from" into piece "to", 4 4 -> 0 8
	 * Precondition: from and to are valid locations with equal values
	 * @param from The piece to move
	 * @param to The destination of the piece
	*/
	private void add(Location from, Location to)
	{
		if(survivalMode && board.get(from) >= 8)
			timeLeft += board.get(from) / 4;
		
		
		score += board.get(to) * 2;
		board.set(to, board.get(to) * 2);
		board.set(from, 0);
	}
	
	/** 
	 * Stores the starting game time and activates the time limit after
	 * the first move instead of when the game is created
	 */
	private void madeFirstMove()
	{
		d1 = new Date();
		if(timeLeft > 0)
			activateTimeLimit();
		
		newGame = false;
	}
	
	
	/**
	 * Undo the game 1 turn 
	 * Uses a stack to store previous moves
	 */
	public void undo()
	{
		if(turnNumber > 1 && undosRemaining != 0)
		{
			// Undo the score, board, and turn #
			score = history.popScore();
			board = history.popBoard();
			turnNumber--;
			
			// Use up one of the undos allowed
			if(undosRemaining > 0)
				undosRemaining--;
			
			// Print the number of undos remaining
			if(undosRemaining >= 0)
				System.out.println("Undos remaining: " + undosRemaining);
		}
	}
	
	/**
	 * Shuffle the board
	 */
	public void shuffle()
	{
		// If this is the game's first move, keep track of
		// the starting time and activate the time limit
		if(newGame)
			madeFirstMove();
		
		// Adds every piece > 0 to a linked list
		LinkedList<Integer> pieces = new LinkedList<Integer>();
		int num;
		
		for(int row = 0; row < board.getNumRows(); row++)
			for(int col = 0; col < board.getNumCols(); col++)
			{
				num = board.get(new Location(row, col));
				if(num > 0)
				{
					pieces.add(num);
					
					// Remove the piece from the board
					// This is used instead of board.clear() to prevent
					// the X's from disappearing in corner mode
					board.set(new Location(row,col), 0);
				}
			}
		
		List<Location> empty;
		
		// Adds every piece to a random empty location
		for(int piece : pieces)
		{
			empty = board.getEmptyLocations();
			board.set(empty.get((int) (Math.random() * empty.size())), piece);
		}
		
		turnNumber++;
	}
	
	/**
	 * Remove all 2's and 4's from the board
	 */
	public void removeLowTiles()
	{
		for(int row = 0; row < board.getNumRows(); row++)
			for(int col = 0; col < board.getNumCols(); col++)
			{
				int tile = board.get(new Location(row,col));
				if(tile <= 4 && tile > 0)
					board.set(new Location(row,col), 0);
			}
		
		// There are always at least 2 pieces on the board
		if(board.getFilledLocations().size() < 2)
			addRandomPiece();
		if(board.getFilledLocations().size() < 2)
			addRandomPiece();
		
	}
	
	/**
	 * Remove the piece from the given location
	 * @param loc The location to remove
	 */
	public void removeTile(Location loc)
	{
		board.set(loc, 0);
	}
	
	/**
	 * Stop the game automatically after a time limit
	 * @param seconds The time limit in seconds
	 */
	public void setTimeLimit(double seconds)
	{
		if(seconds > 0)
			timeLeft = seconds;
	}
	
	/**
	 * Starts the time limit
	 * Is called after the first move
	 * Precondition
	 * */
	private void activateTimeLimit()
	{	
		// How often to update the time left
		// Smaller = update more often
		final double UPDATESPEED = 0.1;
		
		
		// Create a new thread to quit the game
		final Thread T = new Thread() {
			public void run()
			{
				while(timeLeft > 0)
				{
					try
					{
						// Pause the thread for x milliseconds
						// The game continues to run
						Thread.sleep((long) (UPDATESPEED * 1000.0));
					}
					catch (Exception e)
					{
						System.err.println(e);
						System.err.println(Thread.currentThread().getStackTrace());
					}

					timeLeft -= UPDATESPEED;
					
					// Round the time to the second decimal place
					// The number of 0's = number of decimal places
					timeLeft = (double)Math.round(timeLeft * 100) / 100;
				}
				
				// After the time limit is up, quit the game
				System.out.println("Time Limit Reached");
				quitGame = true;   
				
			}
		}; // end thread

		T.start();
	}
	
	/**
	 * @return the time limit in seconds
	 * This is NOT the amount of time currently left in the game, 
	 * it is the total time limit.
	 */
	public double getTimeLimit()
	{
		return timeLeft;
	}
	
	/**
	 * Places immovable X's in the corners of the board
	 * This will clear any existing pieces in the corners of the board
	 */
	public void cornerMode()
	{
		int previousValue;
		
		previousValue = board.set(new Location(0,0), -1);
		if(previousValue != 0)
			addRandomPiece(previousValue);
			
		previousValue = board.set(new Location(0,board.getNumCols() - 1), -1);
		if(previousValue != 0)
			addRandomPiece(previousValue);
		
		previousValue = board.set(new Location(board.getNumRows() - 1,0), -1);
		if(previousValue != 0)
			addRandomPiece(previousValue);
		
		previousValue = board.set(new Location(board.getNumRows() - 1 ,board.getNumCols() - 1), -1);
		if(previousValue != 0)
			addRandomPiece(previousValue);
	}
	
	/**
	 * Places an X on the board that can move but not combine 
	 */
	public void XMode()
	{
		List<Location> empty = board.getEmptyLocations();

		if(empty.isEmpty())
			System.err.println("Can not start XMode. The board is filled");
		else
		{
			int randomLoc = (int) (Math.random() * empty.size());
			board.set(empty.get(randomLoc), -2);
		}
	}

	
	/**
	 *  The game increases the time limit when tiles >= 8 combine
	 */
	public void survivalMode()
	{
		survivalMode = true;
		
		// If no time limit is in effect, set it to 30 seconds
		if(timeLeft <= 0)
			timeLeft = 30;
	}
	
	public void dynamicTileSpawning(boolean enabled)
	{
		dynamicTileSpawning = enabled;
	}
	
	
	
	/**
	 *  Limit the number of undos
	 * -1 = unlimited
	 * @param limit The new limit of undos
	 * This overrides the previous limit, does not add to it
	 */
	public void setUndoLimit(int limit)
	{
		undosRemaining = limit;
	}
	
	/**
	 * @return The number of undos left
	 * -1 = unlimited
	 */
	public int getUndosRemaining()
	{
		return undosRemaining;
	}
	
	/**
	 * Limit the number of moves
	 * -1 = unlimited
	 * @param limit The new limit of moves
	 * This overrides the previous limit, does not add to it
	 */
	public void setMoveLimit(int limit)
	{
		movesRemaining = limit;
	}
	
	/**
	 * @return The number of moves left
	 * -1 = unlimited
	 */
	public int getMovesRemaining()
	{
		return movesRemaining;
	}
	
	/**
	 * Randomly adds a new piece to an empty space
	 * 90% add 2, 10% add 4
	 * CHANCE_OF_2 is a final variable declared at the top
	 * 
	 * If dynamicTileSpawing is true,
	 * any tile less than the max tile can spawn
	 * Ex. If the highest piece is 32 then a 2,4,8, or 16 can appear
	 * All possible tiles have an equal chance of appearing
	 */
	public void addRandomPiece()
	{
		// See method header for description of dynamicTileSpawning
		if(dynamicTileSpawning)
		{
			// All powers of 2 less that the highest tile
			ArrayList<Integer> possibleTiles = new ArrayList<Integer>();
			possibleTiles.add(2);
			possibleTiles.add(4);
			
			// The highest tile on the board
			int highest = highestPiece();
			
			// Add each possible value to possibleTiles
			for(int t = 8; t < highest; t *= 2)
				possibleTiles.add(t);
			
			int tile = possibleTiles.get((int) (Math.random() * possibleTiles.size()));
			addRandomPiece(tile);
			
		}
		else
		{	
			if(Math.random() < CHANCE_OF_2)
				addRandomPiece(2);
			else
				addRandomPiece(4);
		}
	}
	
	private void addRandomPiece(int tile)
	{
		// A list of the empty spaces on the board
		List<Location> empty = board.getEmptyLocations();

		// If there are no empty pieces on the board don't do anything
		if(empty.isEmpty())
			return;
		
		int randomLoc = (int) (Math.random() * empty.size());
		
		board.set(empty.get(randomLoc), tile);
	}

	
	
	/**
	 * @return Whether or not the game is won
	 * A game is won if there is a 2048 tile or greater
	 */
	public boolean won()
	{
		return won(2048);
	}
	
	
	/**
	 * @param winningTile The target tile
	 * @return If a tile is >= winningTile
	 */
	public boolean won(int winningTile)
	{
		Location loc;
		for(int col = 0; col < board.getNumCols(); col++)
		{
			for(int row = 0; row < board.getNumRows(); row++)
			{
				loc = new Location(row, col);
				if(board.get(loc) >= winningTile)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @return If the game is lost
	 */
	public boolean lost()
	{
		// If the game is quit then the game is lost
		if(quitGame || movesRemaining == 0)
			return true;
		
		// If the board is not filled then the game is lost
		if(!board.getEmptyLocations().isEmpty())
			return false;
		
		int current = -1;
		int next;
		
		// Check if two of the same number are next to each
		// other in a row.
		for(int row = 0; row < board.getNumRows(); row++)
		{
			for(int col = 0; col < board.getNumCols(); col++)
			{
				next = current;
				current = board.get(new Location(row,col));
				
				if(current == next)
					return false;
			}
			current = -1;
		}
		
		// Check if two of the same number are next to each
		// other in a column.
		for(int col = 0; col < board.getNumCols(); col++)
		{
			for(int row = 0; row < board.getNumRows(); row++)
			{
				next = current;
				current = board.get(new Location(row,col));
				
				if(current == next)
					return false;
			}
			current = -1;
		}
		return true;
	}
	
	/**
	 * @return the number of seconds the game was played for
	 */
	public double timePlayed()
	{
		Date d2 = new Date();
		double seconds = ((d2.getTime() - d1.getTime()) / 1000.0);
		return seconds;
	}
	
	/**
	 *  Quit the game
	 */
	public void quit()
	{
		quitGame = true;
	}
	
	
	/**
	 * @return The highest piece on the board
	 */
	public int highestPiece()
	{
		int highest = 0;
		for(int col = 0; col < board.getNumCols(); col++)
		{
			for(int row = 0; row < board.getNumRows(); row++)
			{
				if(board.get(new Location(row, col)) > highest)
					highest = board.get(new Location(row, col));
			}
		}
		return highest;
	}
	
	/**
	 * @param otherGame The other game to check
	 * @return If the games are equal
	 * Games are equal if they have the same board and score.
	 * Even if their history is different.
	 */
	public boolean equals(Game otherGame)
	{
		return board.equals(otherGame.getGrid()) && score == otherGame.getScore();
	}
	
	/**
	 * Used to avoid creating aliases
	 * @return A clone of the game
	 */
	public Game clone()
	{
		Game game = new Game();
		game.setGrid(board.clone());
		game.setScore(score);
		game.setHistory(history.clone());
		game.setTurn(turnNumber);
		return game;
	}
	

	/**
	 * @param direction Called using the final variables in the location class
	 * @return If the game can move in the given direction
	 */
	public boolean canMove(int direction)
	{
		Game nextMove = clone();
		nextMove.act(direction);
		return !(nextMove.equals(this));
	}
	
	/**
	 * @return The score of the game
	 */
	public int getScore()
	{
		return score;
	}
	
	/**
	 * @return The current turn number of the game
	 */
	public int getTurns()
	{
		return turnNumber;
	}
	
	/**
	 * @return The grid of the game
	 */
	public Grid getGrid()
	{
		return board;
	}
	
	private void setTurn(int newTurn)
	{
		turnNumber = newTurn;
	}
	
	private void setGrid(Grid newGrid)
	{
		board = newGrid.clone();
	}
	
	private void setScore(int newScore)
	{
		score = newScore;
	}
	
	private void setHistory(Stack newHistory)
	{
		history = newHistory.clone();
	}
	
	/** @return a string of the game in the form:
	---------------------------------------------
	||  Turn #8  Score: 20  Moves Left: 3
	---------------------------------------------
	| 8  |    | 2  |    |
	| 4  |    |    |    |
	| 2  |    |    | 2  |
	|    |    |    |    |		*/
	public String toString()
	{
		String output = "---------------------------------------------\n";
		output += "||  Turn #" + turnNumber + "  Score: " + score + "\n";
		output += "||  Moves Left:";
		
		if(movesRemaining >= 0)
			output += movesRemaining;
		else
			output += "�";
		
		output += " Undos Left:";
		
		if(undosRemaining >= 0)
			output += undosRemaining;
		else
			output += "�";
		
		output += " Time Left:";
		
		if(timeLeft >= 0)
			output += timeLeft;
		else
			output += "�";
		
		
		output += "\n---------------------------------------------\n";
		output += board.toString();
		
		return output;
	}
}