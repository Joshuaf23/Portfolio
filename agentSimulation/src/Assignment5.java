/*
 * Agent based simulation that shows the spread of muscle wilt (MW) in a body over 10 years using a 15 x 15 array of cells.
 * This program also includes the Youthazeen (YZ) drug, which makes cells immune to MW and also spreads between cells.
 * If a healthy (NA or not afflicted) cell is next to a MW cell in a cardinal direction, the healthy cell has a 50% chance of becoming a MW cell.
 * If a NA cell is next to a YZ cell, there is a 60% chance that the YZ begins tendriling to the NA cell, giving it a 60% immunity to MW cells and becoming fully immune once the year is over.
 * This program displays the array of cells at years 1, 5, and 10 as well as the amount of NA, MW, and YZ cells in that year.
 * At the end of the 10 year period, the program displays the average MW cells and the variance of the MW cells.
 */
public class Assignment5 {
    public static void main(String[] args) {
        Cell[][] cellArray = new Cell[15][15]; //15x15 cell array
        //for loop to place cells in the array
        for (int y = 0; y < 15; y++){
            for (int x = 0; x < 15; x++){
                cellArray[x][y] = new Cell(x, y);
            }//end of for
        }//end of for
        int i = 0;
        //while loop to place 23 random MW cells in the array
        while (i < 23) {
            int x = (int) (Math.random()*15);
            int y = (int) (Math.random()*15);
            if (cellArray[x][y].getStatus() == 0) {
                cellArray[x][y].setStatus(1);
            } else {
                i--;
            } //end of if
            i++;
        }//end of while

        i = 0;
        //while loop to place 23 random YZ cells in the array
        while (i < 23) {
            int x = (int) (Math.random()*15);
            int y = (int) (Math.random()*15);
            if (cellArray[x][y].getStatus() == 0) {
                cellArray[x][y].setStatus(2);
            } else {
                i--;
            } //end of if
            i++;
        }//end of while

        double averageMW = 0; //average MW cells
        double varianceMW = 0; //variance MW cells
        int totalMW = 0; //total MW cells
        int sqrMW = 0; //square of the total MW cells
        //for loop that runs 10 times to simulate the 10 years
        for (int year = 1; year <= 10; year++) {
            Cell[] adjacentCells = new Cell[4]; //array to hold cells adjacent to a cell
            int naCount = 0; //current count of NA cells
            int mwCount = 0; //current count of MW cells
            int yzCount = 0; //current count of YZ cells
            //for loop to count the type of cells in array
            for (int y = 0; y < 15; y++){
                for (int x = 0; x < 15; x++){
                    Cell currCell = cellArray[x][y];
                    switch (currCell.getStatus()) {
                        case 0:
                            naCount++;
                            break;
                        case 1:
                            mwCount++;
                            break;
                        case 2:
                            yzCount++;
                            break;
                    }//end of switch
                }//end of for
            }//end of for
            totalMW += mwCount; //add MW count to total
            sqrMW += Math.pow(mwCount, 2); //add square of MW count to square of total
            //if statement to print out the cell array at years 1, 5, and 10
            if (year == 1 || year == 5 || year == 10) {
                System.out.println("Year " + year);
                System.out.println("=================================================================================");
                printArray(cellArray);
                System.out.printf("NA: %d\tMW: %d\tYZ: %d\n", naCount, mwCount, yzCount);
                System.out.println("=================================================================================");
            }//end of if
            //for loop to collect cells adjacent to each cell and change the status of the cell as needed
            for (int y = 0; y < 15; y++){
                for (int x = 0; x < 15; x++){
                    if (x - 1 > 0) {
                        adjacentCells[0] = cellArray[x-1][y];
                    }//end of if
                    if (x + 1 < 15) {
                        adjacentCells[1] = cellArray[x+1][y];
                    }//end of if
                    if (y - 1 > 0) {
                        adjacentCells[2] = cellArray[x][y-1];
                    }//end of if
                    if (y + 1 < 15) {
                        adjacentCells [3] = cellArray[x][y+1];
                    }//end of if
                    cellArray[x][y].changeStatus(adjacentCells); //function to change status of the cell
                }//end of for
            }//end of for
            
            //changes tendriled YZ cells into complete YZ cells
            for (int y = 0; y < 15; y++){
                for (int x = 0; x < 15; x++){
                    Cell currCell = cellArray[x][y];
                    if (currCell.getStatus() == 3) {
                        currCell.setStatus(2);
                    }//end of if
                }//end of for
            }//end of for
        }//end of for
        averageMW = totalMW/10.0; //calculates average
        varianceMW = (sqrMW/10.0) - Math.pow(averageMW, 2); //calculates variance
        //prints out average and variance
        System.out.printf("Average MW cells: %f\n", averageMW);
        System.out.printf("Variance MW cells: %f\n", varianceMW);
        System.out.println("Done!");
    } //end of main

    //function to print out the cell array
    public static void printArray(Cell[][] cellArray){
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 15; x++){
                Cell currCell = cellArray[x][y];
                switch (currCell.getStatus()) {
                    case 0:
                        System.out.print("\u001B[0m" + "NA  ");
                        break;
                    case 1:
                        System.out.print("\u001B[31m" + "MW  ");
                        break;
                    case 2:
                        System.out.print("\u001B[32m" + "YZ  ");
                        break;
                }//end of if
            }//end of for
            System.out.print("\u001B[0m" + "\n");
        }//end of for
    }//end of printArray
}//end of Assignment5

//class that represents a cell
class Cell {
    int myX; //x location in cell array
    int myY; //y location in cell array
    int status; //0 for unaffected, 1 for MW, 2 for YZ, 3 for tendriled YZ

    //constructor for Cell
    public Cell(int myX, int myY) {
        setMyX(myX);
        setMyY(myY);
        setStatus(0);
    }//end of Cell constructor

    //getter for myX
    public int getMyX() {
        return myX;
    }//end of getMyX

    //setter for myX
    public void setMyX(int myX) {
        this.myX = myX;
    }//end of setMyX

    //end of getMyY
    public int getMyY() {
        return myY;
    }//end of getMyY

    //setter for myY
    public void setMyY(int myY) {
        this.myY = myY;
    }//end of setMyY

    //getter for status
    public int getStatus() {
        return status;
    }//end of getStatus

    //setter for status
    public void setStatus(int status) {
        this.status = status;
    }//end of setStatus

    //function to change the status based on adjacent cells
    public void changeStatus(Cell[] adjacentCells){
        double x; //used for holding a random number
        //for loop that goes through each cell put into the array and changes status of current cell based on the other cell's status
        for (Cell cell : adjacentCells) {
            if (cell != null) {
                //if adjacent cell has MW, has a 50% chance to spread to this cell if this cell isn't already affected
                //if adjacent cell has YZ, has a 60% chance to spread to tendril to this cell
                //if MW tries to tendril onto a cell that is tendriled onto be a YZ cell, only has a 40% chance to tendril to this cell
                if (cell.getStatus() == 1 && this.status == 0) {
                    x = Math.random();
                    if (x < 0.5) {
                        setStatus(1);
                    }//end of if
                } else if (cell.getStatus() == 2 && this.status == 0) {
                    x = Math.random();
                    if (x < 0.6) {
                        setStatus(3);
                    }
                } else if (cell.getStatus() == 1 && this.status == 3){
                    x = Math.random();
                    if (x < 0.4) {
                        setStatus(1);
                    }
                }
            }//end of if
        }//end of for
    }//end of changeStatus
}//end of Cell