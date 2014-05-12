// Tyler Carberry
import java.util.*;
public class Grid
{
	private int[][] board;
	private final int ROWS;
	private final int COLS;
	
	// Constructor
	public Grid (int rows, int columns)
	{
		ROWS = rows;
		COLS = columns;
		board = new int[rows][columns];
	}
	
	public Grid(int[][] grid)
	{
		board = grid;
		ROWS = grid.length;
		COLS = grid[0].length;
	}
	
	
	// Returns the variable at the position
	public int get(Location loc)
	{
		return board[loc.getRow()][loc.getCol()];
	}
	
	// Sets the object and returns the previous value
	public int set(Location loc, int newObject)
	{
		int temp = get(loc);
		board[loc.getRow()][loc.getCol()] = newObject;
		return temp;
	}
	
	public boolean equals(Grid g)
	{
		//System.out.println("Entering Grid .equals");
		//System.out.println("Returning " + Arrays.deepEquals(board, g.getArray()));
		return (Arrays.deepEquals(board, g.getArray()));
	}
	
	private void setArray(int[][] newBoard)
	{
		 board = newBoard;
	}
	
	public int[][] getArray()
	{
		return board;
	}
	
	public int getNumRows()
	{
		return ROWS;
	}
	
	public int getNumCols()
	{
		return COLS;
	}
	
	public boolean isEmpty(Location loc)
	{
		return (get(loc) == 0);
	}
	
	public void move(Location from, Location to)
	{
		set(to, get(from));
		set(from, 0);
	}
	
	// Returns the empty locations
	public LinkedList<Location> getEmptyLocations()
	{
		LinkedList<Location> empty = new LinkedList<Location>();
		for(int row = 0; row < ROWS; row++)
			for(int col = 0; col < COLS; col++)
				if(board[row][col] == 0)
					empty.add(new Location(row,col));
		
		return empty;
	}
	
	// Returns the filled locations
	public LinkedList<Location> getFilledLocations()
	{
		LinkedList<Location> filled = new LinkedList<Location>();
		for(int row = 0; row < ROWS; row++)
			for(int col = 0; col < COLS; col++)
				if(board[row][col] != 0)
					filled.add(new Location(row,col));
			
		return filled;
	}
		
	// Returns the empty locations
	public void printGrid()
	{
		Location loc;
		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				loc = new Location (row, col);
				
				// Even Spacing
				// | 4  | 16 | 256|1028|
				if(get(loc) == 0)
					System.out.print("|    ");
				else
					if(get(loc) >= 1000)
						System.out.print("|" + get(loc));
					else
						if(get(loc) >= 100)
							System.out.print("| " + get(loc));
						else
							if(get(loc) >= 10)
								System.out.print("| " + get(loc) + " ");
							else
								System.out.print("| " + get(loc) + "  ");
			}
			System.out.println("|");
		}
			
	}
		
	
	// Clones the array
	// Used because the .clone() method creates an alias for 2d integer arrays
	public Grid clone()
	{
		//System.out.println("Entering Grid clone");
		int[][] temp = new int[board.length][board[0].length];
		
		for(int row = 0; row < board.length; row++)
			for(int col = 0; col < board[0].length; col++)
				temp[row][col] = board[row][col];
		
		Grid toReturn = new Grid(temp);
		return toReturn;	
	}
	
	
	// Returns if the location is a valid position in the grid
	public boolean isValid(Location loc)
	{
		return (loc.getRow() >= 0 && loc.getRow() < ROWS && loc.getCol() >= 0 && loc.getCol() < COLS);
	}

	// WORKS BUT NEED TO CHANGE
	public String toString()
	{
		String output = "";
		

		Location loc;
		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				loc = new Location (row, col);
				
				// Even Spacing
				// | 4  | 16 | 256|1028|
				if(get(loc) == 0)
					output += "|    ";
				else
					if(get(loc) >= 1000)
						output += "|" + get(loc);
					else
						if(get(loc) >= 100)
							output += "| " + get(loc);
						else
							if(get(loc) >= 10)
								output += "| " + get(loc) + " ";
							else
								output += "| " + get(loc) + "  ";
			}
			output+= "|\n";
		}
		
		
		
		
		return output;
	}
}
