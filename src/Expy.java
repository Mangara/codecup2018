import java.io.BufferedReader;
import java.util.List;
import java.io.PrintStream;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStreamReader;
public class Expy {
public static void main(String[]args)throws IOException{
new SimpleMaxPlayer("Expy",new ExpectedValue(),new AllMoves()).play(new BufferedReader(new InputStreamReader(System.in)),System.out);
}
}
class Board {
public static final int ME=0,OPP=1;
public static final byte BLOCKED=120,FREE=0;
private final byte[][]grid=new byte[8][8];
private final boolean[]myUsed=new boolean[15];
private final boolean[]oppUsed=new boolean[15];
private int nFree=36;
public byte get(byte a,byte b){
return grid[a][b];
}
public int getHoleValue(byte a,byte b){
int total=0;
if(a>0){
total+=value(grid[a-1][b]);
}
if(a<7-b){
total+=value(grid[a+1][b]);
}
if(b>0){
total+=value(grid[a][b-1]);
}
if(b<7-a){
total+=value(grid[a][b+1]);
}
if(a>0&&b<8-a){
total+=value(grid[a-1][b+1]);
}
if(a<8-b&&b>0){
total+=value(grid[a+1][b-1]);
}
return total;
}
private int value(byte val){
return(val==BLOCKED?0:val);
}
public int getFreeSpotsAround(byte a,byte b){
int total=0;
if(a>0&&grid[a-1][b]==FREE){
total++;
}
if(a<7-b&&grid[a+1][b]==FREE){
total++;
}
if(b>0&&grid[a][b-1]==FREE){
total++;
}
if(b<7-a&&grid[a][b+1]==FREE){
total++;
}
if(a>0&&b<8-a&&grid[a-1][b+1]==FREE){
total++;
}
if(a<8-b&&b>0&&grid[a+1][b-1]==FREE){
total++;
}
return total;
}
public void set(byte a,byte b,byte value){
grid[a][b]=value;
nFree--;
if(value>0&&value!=BLOCKED){
myUsed[value-1]=true;
}else if(value<0){
oppUsed[-value-1]=true;
}
}
public void set(String location,byte value){
byte[]loc=getCoordinates(location);
set(loc[0],loc[1],value);
}
public void applyMove(byte[]move){
grid[move[0]][move[1]]=move[2];
nFree--;
if(move[2]>0){
myUsed[move[2]-1]=true;
}else{
oppUsed[-move[2]-1]=true;
}
}
public void undoMove(byte[]move){
grid[move[0]][move[1]]=FREE;
nFree++;
if(move[2]>0){
myUsed[move[2]-1]=false;
}else{
oppUsed[-move[2]-1]=false;
}
}
public boolean haveIUsed(byte value){
return myUsed[value-1];
}
public boolean hasOppUsed(byte value){
return oppUsed[value-1];
}
public boolean isGameOver(){
return nFree==1;
}
public static byte[]getCoordinates(String location){
return new byte[]{(byte)(location.charAt(0)-'A'),(byte)(location.charAt(1)-'1')};
}
public static String coordinatesToString(byte a,byte b){
return Character.toString((char)('A'+a))+Character.toString((char)('1'+b));
}
public void print(){
for(int h=0;h<8;h++){
System.err.print(spaces(7-h));
for(int i=0;i<=h;i++){
if(i>0){
System.err.print(' ');
}
System.err.print(grid[h-i][i]==BLOCKED?"  X":String.format("%3d",grid[h-i][i]));
}
System.err.println(spaces(7-h));
}
}
private String spaces(int n){
switch(n){
case 0:
return "";
case 1:
return "  ";
case 2:
return "    ";
case 3:
return "      ";
case 4:
return "        ";
case 5:
return "          ";
case 6:
return "            ";
case 7:
return "              ";
default:
throw new IllegalArgumentException();
}
}
}
abstract class Player {
protected final static boolean DEBUG=false;
protected Board board=new Board();
protected final List<Integer>myNumbers=new ArrayList<>();
protected final List<Integer>hisNumbers=new ArrayList<>();
protected final String name;
public Player(String name){
this.name=name;
}
public String getName(){
return name;
}
public void play(BufferedReader in,PrintStream out)throws IOException{
initialize();
for(int i=0;i<5;i++){
String loc=in.readLine();
if(DEBUG){
System.err.println(name+":Blocking "+loc);
}
block(loc);
}
for(String input=in.readLine();!(input==null||"Quit".equals(input));input=in.readLine()){
if(DEBUG){
System.err.println(name+":Input:"+input);
}
if(!"Start".equals(input)){
processMove(input);
}
if(DEBUG){
board.print();
}
String move=move();
if(DEBUG){
System.err.println(name+":Output:"+move);
}
out.println(move);
}
}
public void initialize(){
board=new Board();
for(int i=1;i<=15;i++){
myNumbers.add(i);
hisNumbers.add(i);
}
}
public void block(String loc){
board.set(loc,Board.BLOCKED);
}
public void processMove(String move){
String loc=move.substring(0,2);
byte val=(byte)Integer.parseInt(move.substring(3));
hisNumbers.remove((Integer)(int)val);
board.set(loc,(byte)-val);
}
public String move(){
byte[]move=selectMove();
board.set(move[0],move[1],move[2]);
myNumbers.remove((Integer)(int)move[2]);
return Board.coordinatesToString(move[0],move[1])+"="+move[2];
}
protected abstract byte[]selectMove();
}
class SimpleMaxPlayer extends Player {
private final Evaluator evaluator;
private final MoveGenerator generator;
public SimpleMaxPlayer(String name,Evaluator evaluator,MoveGenerator generator){
super(name);
this.evaluator=evaluator;
this.generator=generator;
}
@Override
protected byte[]selectMove(){
byte[]bestMove=null;
double bestValue=Double.NEGATIVE_INFINITY;
List<byte[]>moves=generator.generateMoves(board,true);
for(byte[]move:moves){
board.applyMove(move);
double value=evaluator.evaluate(board);
board.undoMove(move);
if(value>bestValue){
bestValue=value;
bestMove=move;
}
}
return bestMove;
}
}
interface Evaluator {
public abstract int evaluate(Board board);
}
class ExpectedValue implements Evaluator {
@Override
public int evaluate(Board board){
int totalUnused=0;
int nUnused=0;
for(byte i=1;i<=15;i++){
if(!board.haveIUsed(i)){
totalUnused+=i;
nUnused++;
}
if(!board.hasOppUsed(i)){
totalUnused-=i;
nUnused++;
}
}
double expectedFree=(nUnused==0?0:totalUnused/(double)nUnused);
double totalExpectedHoleValue=0;
int nHoles=0;
for(byte a=0;a<8;a++){
for(byte b=0;b<8-a;b++){
if(board.get(a,b)==Board.FREE){
totalExpectedHoleValue+=board.getHoleValue(a,b)+board.getFreeSpotsAround(a,b)*expectedFree;
nHoles++;
}
}
}
return(int)(10000*totalExpectedHoleValue/nHoles);
}
}
interface MoveGenerator {
public abstract List<byte[]>generateMoves(Board board,boolean player1);
}
class AllMoves implements MoveGenerator {
@Override
public List<byte[]>generateMoves(Board board,boolean player1){
List<byte[]>moves=new ArrayList<>();
for(byte a=0;a<8;a++){
for(byte b=0;b<8-a;b++){
if(board.get(a,b)!=Board.FREE){
continue;
}
for(byte v=1;v<=15;v++){
if((player1&&board.haveIUsed(v))||(!player1&&board.hasOppUsed(v))){
continue;
}
moves.add(new byte[]{a,b,(player1?v:(byte)-v)});
}
}
}
return moves;
}
}
