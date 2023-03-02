import java.util.*;
import java.io.*;

public class Crossword{

    //Class Variables
    //static vars
    private DictInterface D;
    private StringBuilder Alphabet;

    //non static vars
    private int dimension;
    private char[][] board;
    private StringBuilder[] colStr;
    private StringBuilder[] rowStr;

    //score variable
    private HashMap<Character, Integer> ScoreMap;


    /** Constructor for Crossword
  	 * @param a dictionary file and a crossword template file
  	 *
  	 */
  public Crossword(String dicFile, String CrosFile) throws IOException{

    //read in dictionary file and assign to D variable
    D = new MyDictionary();
    try(Scanner s = new Scanner(new File(dicFile))){
      while(s.hasNext()){
        D.add(s.nextLine());
      }
    }catch(IOException e){
      e.printStackTrace();
    }

    //instantiate static Alphabet Strigbuilder used in solution finding
    this.Alphabet = new StringBuilder();
    for(char i = 'a'; i<='z'; i++){
      this.Alphabet.append(i);
    }

    //read in score file
    this.ScoreMap = new HashMap<Character, Integer>();
    try(Scanner g = new Scanner(new File("letterpoints.txt"))){
      while(g.hasNext()){
        String cur = g.nextLine();
        char l = cur.charAt(0);
        char ln = cur.charAt(2);
        int n = Character.getNumericValue(ln);
        l = Character.toLowerCase(l);
        ScoreMap.put(l,n);
      }
    }catch(IOException e){
      e.printStackTrace();
    }

    //read in crossword file and instantiate required variables to solve
    try(Scanner infile = new Scanner(new File(CrosFile))){
      //first int in file provides the crossword dimensions define constructor values based on it.
      int n = infile.nextInt();
      this.dimension = n;
      this.board = new char[n][n];
      //fill colStr and rowStr arrays with empty stringbuilders to start.
      this.colStr = new StringBuilder[n];
      for(int i=0; i<this.colStr.length; i++) this.colStr[i]= new StringBuilder("");
      this.rowStr = new StringBuilder[n];
      for(int i=0; i<this.rowStr.length; i++) this.rowStr[i] = new StringBuilder("");
      //move to crossword template and generate our board data.
      infile.nextLine();
      int count=0;
      while(infile.hasNext()){
        String cur = infile.nextLine();
        char[] curline = cur.toCharArray();
        for(int i=0; i<curline.length; i++) this.board[count][i]=curline[i];
        count++;
      }
    }
    catch(IOException e){
      e.printStackTrace();
    }



  }
  /** Begins the backtracking search process from the top left of board
	 * @param none
	 * @return crossword solution if exists.
	 */
  public void solve(){
    solve(0,0);
  }
  /** Recursive backtracking search process
	 * @param s current row and column of the crossword
	 * @return crossword solution if exists
	 */
  private void solve(int row, int col){
    //is the current cell a special case? if so we dont need to enumerate the alphabet
    if(this.board[row][col] != '+') applyPreset(row,col);
    //must be an open space, begin alphabetic enumeration and evaluation
    else{
      for(int i=0; i<Alphabet.length(); i++){
        char Letter = Alphabet.charAt(i);
        //is currsol valid? if so append to row and col SB
        if(isValid(row,col,Letter)){
          this.rowStr[row].append(Letter);
          this.colStr[col].append(Letter);
          //was this the last cell on the board? if so we have found our solution, terminate backtracking and present solution
          if(row==this.dimension-1 && col==this.dimension-1){
            presentSolution();
          }
          //not finished, check to see if its a column end and increment accordingly
          else{
            if(col==dimension-1) solve(row+1,0);
            else solve(row,col+1);
          }
          //if we got here we must delete last letter since no word was added
          this.rowStr[row].deleteCharAt(this.rowStr[row].length()-1);
          this.colStr[col].deleteCharAt(this.colStr[col].length()-1);

        }

      }
    }

  }
  /** helper function for solve to handle cases of presets in the Cword template
	 * @param crossword row and col where preset was found
	 * @return handles said case without disrupting search process
	 */
  private void applyPreset(int row, int col){
    //preset is either a predefined letter or block, isValid is responsible for handeling but we know alphabetic enumeration is not required
    char Preset = this.board[row][col];
    if(isValid(row,col,Preset)){
      this.rowStr[row].append(Preset);
      this.colStr[col].append(Preset);
      //was this the last cell on the board? if so we have found our solution, terminate backtracking and present solution
      if(row== this.dimension-1 && col==this.dimension-1){
        presentSolution();
      }
      //not finished, check to see if its a column end and increment accordingly
      else{
        if(col==dimension-1) solve(row+1, 0);
        else solve(row,col+1);
      }
      //if we got here we must delete last letter since no word was added
      this.rowStr[row].deleteCharAt(this.rowStr[row].length()-1);
      this.colStr[col].deleteCharAt(this.colStr[col].length()-1);
    }
  }
  /** Presents the solution to the crossword if found by solve
	 * @param none
	 * @return none
	 */
  private void presentSolution(){
    //print out solved board and terminate program to prevent infinite loop.
    int score = 0;
    for(int i=0; i<this.rowStr.length; i++){

      for(int h=0; h<this.rowStr[i].length(); h++){
        if(this.rowStr[i].charAt(h)!='-'){
        int n = this.ScoreMap.get(this.rowStr[i].charAt(h));
        score+=n;
      }
      }
      System.out.println(this.rowStr[i]);
    }
    System.out.println("Score: " + score);

    System.exit(0);
  }
  /** validate the current letter with respect to the provided cell
  * this function is responsible for handeling all cases regarding blocked cells
  * it ensures when a blocked cell is found the appropriate substrings surrounding it
  * are still valid by using the dictionary interface.
	 * @param row and col along with the letter candidate
	 * @return boolean if curr letter is valid
	 */
  private boolean isValid(int row, int col, char let){
    //declare two placeholder int variables for row and col if a block is found
    //declare two int variables for row and col to track # of blocks in given row or col
    int rowCross = -1, colCross = -1, numRowCross=0, numColCross=0;
    for(int i = this.rowStr[row].length()-1; i>=0; i--){
      //test if block found and is it the first block
      if(this.rowStr[row].charAt(i)=='-' && rowCross==-1){
        rowCross = i;
        numRowCross++;
      }
      //block found but not the first, enumerate count
      else if(this.rowStr[row].charAt(i)=='-') numRowCross++;
    }
    //test if block found and is it the first block
    for(int i = this.colStr[col].length()-1; i>=0; i--){
      if(this.colStr[col].charAt(i)=='-' && colCross==-1){
        colCross=i;
        numColCross++;
      }
      //block found but not the first, enumerate count
      else if(this.colStr[col].charAt(i)=='-') numColCross++;
    }
    //after checking for blocks it is safe to add the letter since a shade will be accounted for
    this.rowStr[row].append(let);
    this.colStr[col].append(let);

    //get our dictionary score for both row and col
    int rowCrossDic, colCrossDic;
    if(let=='-'){
      numRowCross++;
      numColCross++;
      //if we are at a shade, we need to score based on prev letter instead of current since curr is a shade
      rowCrossDic = D.searchPrefix(this.rowStr[row], rowCross+1, this.rowStr[row].length()-2);
      colCrossDic = D.searchPrefix(this.colStr[col], colCross+1, this.colStr[col].length()-2);
    }

    //curr letter is not a shade so we get prefix score including curr letter.
    else{
      rowCrossDic = D.searchPrefix(this.rowStr[row], rowCross+1, this.rowStr[row].length()-1);
      colCrossDic = D.searchPrefix(this.colStr[col], colCross+1, this.colStr[col].length()-1);
    }
    //get substring if shade or get whole string if not and instantiate booleans for row and col
    //rowpass and colpass are initially set to true and various tests will negate if needed.
    String rowString = this.rowStr[row].substring(rowCross+1, this.rowStr[row].length());
    String colString = this.colStr[col].substring(colCross+1, this.colStr[col].length());
    boolean rowPass = true, colPass= true;
    for(int i=0; i<rowString.length(); i++){
      if(rowString.charAt(i) != '-'){
        rowPass = false;
        break;
      }
    }
    for(int i=0; i<colString.length(); i++){
      if(colString.charAt(i) != '-'){
        colPass = false;
        break;
      }
    }
    //if rowpass and/or colpass are still true our curr lett is valid since we need a word and not a prefix
    if(rowPass) rowCrossDic=2;
    if(colPass) colCrossDic=2;
    //if our number curr let is at a shaded point its valid since we need a word and not a prefix
    if(numRowCross == this.rowStr[row].length()) rowCrossDic=2;
    if(numColCross == this.colStr[col].length()) colCrossDic=2;
    //delete the last letter
    this.rowStr[row].deleteCharAt(this.rowStr[row].length()-1);
    this.colStr[col].deleteCharAt(this.colStr[col].length()-1);
    //if our curr letter is not a valid word or a valid prefix its not valid
    if(rowCrossDic==0 || colCrossDic==0) return false;
    //if curr letter is a valid prefix but not a word and we are at the end of a row or column the letter is not valid
    else if((rowCrossDic==1 && col==this.dimension-1) || (colCrossDic==1 && row==this.dimension-1))
      return false;
    //if curr letter is a valid prefix but its at a shade, its not valid since we need a word
    else if(let=='-' && (rowCrossDic==1 || colCrossDic==1)) return false;
    //if our row and col scores have gotten here then the input is valid, return true.
    return true;

  }
  /** Driver funciton for crossword, requires the user to input file requirements
	 * and proceeds to create and solve the crossword solution based on cmd promp.
   * @param a dict file and cword template file
	 * @return a solution to crossword if it exists.
	 */
  public static void main(String [] args) throws IOException{
    //validate files provided, terminate if not.
    if(args.length < 1){
    System.out.println("Must enter 2 files, dictionary and crossword.");
    System.exit(0);
  }
  //create crossword and generate solution.
  Crossword game = new Crossword(args[0],args[1]);
  game.solve();
  }


}
