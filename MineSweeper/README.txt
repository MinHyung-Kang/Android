README
--------------------------------------------------------------------------------------
MineSweeper (AIT-Budapest 2015 Mobile Development Assignment 1)
Min Hyung (Daniel) KANG
--------------------------------------------------------------------------------------
This is a simple implementation of game Minesweeper with following features : 
	- Gameboard can be specified from 5 x 5 to 15 x 15 (changable in settings)
	- Rectangular shaped gameboards all possible
	- User may click on a cell to try it out. If it is a bomb, game is lost.
	  If it is not a bomb, the cell shows a number of bombs in its 3*3 square neighborhood.
	- If a cell's count is 0, it expands in every direction until it finds a nonzero cell.
	- User may place a flag on a spot he/she thinks a mine exists by pressing on location 
	  for long time (0.5 sec)
	- User may also remove the preexisting flag by long-pressing on the location
	- If user places a flag on a cell without a mine, game is lost.


Refer to the screenshots in this folder for more information.
--------------------------------------------------------------------------------------