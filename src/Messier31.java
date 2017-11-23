import java.io.BufferedReader;
import java.util.List;
import java.util.Arrays;
import java.io.PrintStream;
import java.util.Collections;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStreamReader;
public class Messier31 {
public static void main(String[]args)throws IOException{
Player p=new AspirationPlayer("As_MF_MFM_10",new MedianFree(),new MostFreeMax(),10);
p.play(new BufferedReader(new InputStreamReader(System.in)),System.out);
}
}
class Util {
public static String coordinatesToString(byte a,byte b){
return Character.toString((char)('A'+a))+Character.toString((char)('1'+b));
}
public static byte[]parseMove(String move){
return new byte[]{(byte)(move.charAt(0)-'A'),(byte)(move.charAt(1)-'1'),(byte)(move.charAt(3)-'1')};
}
public static byte[]getCoordinates(String location){
return new byte[]{(byte)(location.charAt(0)-'A'),(byte)(location.charAt(1)-'1')};
}
public static void print(Board board){
for(byte h=0;h<8;h++){
System.err.print(spaces(7-h));
for(byte i=0;i<=h;i++){
if(i>0){
System.err.print(' ');
}
byte value=board.get((byte)(h-i),i);
System.err.print(value==Board.BLOCKED?"  X":String.format("%3d",value));
}
System.err.println(spaces(7-h));
}
System.err.println("nFree:"+board.getFreeSpots());
}
private static String spaces(int n){
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
interface Board {
public static final byte BLOCKED=120;
public static final byte FREE=0;
public byte get(byte a,byte b);
public void block(byte a,byte b);
public boolean isFree(byte a,byte b);
public int getFreeSpots();
public int getHoleValue(byte a,byte b);
public int getFreeSpotsAround(byte a,byte b);
public void applyMove(byte[]move);
public void undoMove(byte[]move);
public boolean haveIUsed(byte value);
public boolean hasOppUsed(byte value);
public boolean isGameOver();
}
class BitBoard implements Board {
private static final long BOARD=0b0000000100000011000001110000111100011111001111110111111111111111L;
private long free=BOARD;
private long myTiles=0;
private long oppTiles=0;
private long myValues=0;
private long oppValues=0;
private short myUsed=0;
private short oppUsed=0;
public BitBoard(){
}
public BitBoard(Board board){
int myValIndex=0;
int oppValIndex=0;
for(byte a=0;a<8;a++){
for(byte b=0;b<8-a;b++){
byte val=board.get(a,b);
int pos=getPos(a,b);
if(val==FREE){
}else if(val==BLOCKED){
free-=(1L<<pos);
}else if(val>0){
free-=(1L<<pos);
myTiles|=(1L<<pos);
myValues|=((long)val)<<(4*myValIndex);
myUsed|=(1<<val);
myValIndex++;
}else if(val<0){
free-=(1L<<pos);
oppTiles|=(1L<<pos);
oppValues|=((long)-val)<<(4*oppValIndex);
oppUsed|=(1<<-val);
oppValIndex++;
}
}
}
}
private int getPos(byte a,byte b){
return 8*a+b;
}
@Override
public byte get(byte a,byte b){
long posMask=1L<<getPos(a,b);
if((free&posMask)>0){
return FREE;
}
if((myTiles&posMask)>0){
return getValue(posMask,myTiles,myValues);
}
if((oppTiles&posMask)>0){
return(byte)-getValue(posMask,oppTiles,oppValues);
}
return BLOCKED;
}
public boolean isFree(byte a,byte b){
return(free&(1L<<getPos(a,b)))>0;
}
@Override
public int getFreeSpots(){
return Long.bitCount(free);
}
private static final long EXCLUDE_ONE=~0b1L;
private static final long EXCLUDE_EIGHT=~0b10000000L;
private static final long EXCLUDE_NINE=~0b100000000L;
private long neighbours(int pos){
long posMask=1L<<pos;
return(((posMask<<1)&EXCLUDE_NINE)
|((posMask>>>1)&EXCLUDE_EIGHT)
|((posMask<<7)&EXCLUDE_EIGHT)
|((posMask>>>7)&EXCLUDE_ONE)
|(posMask<<8)
|(posMask>>>8))
&BOARD;
}
@Override
public int getFreeSpotsAround(byte a,byte b){
return Long.bitCount(free&neighbours(getPos(a,b)));
}
@Override
public int getHoleValue(byte a,byte b){
long neighbours=neighbours(getPos(a,b));
long myNeighbours=myTiles&neighbours;
long oppNeighbours=oppTiles&neighbours;
int total=0;
while(myNeighbours>0){
long posMask=Long.lowestOneBit(myNeighbours);
total+=getValue(posMask,myTiles,myValues);
myNeighbours^=posMask;
}
while(oppNeighbours>0){
long posMask=Long.lowestOneBit(oppNeighbours);
total-=getValue(posMask,oppTiles,oppValues);
oppNeighbours^=posMask;
}
return total;
}
public void block(byte a,byte b){
free&=~(1L<<getPos(a,b));
}
@Override
public void applyMove(byte[]move){
long posMask=(1L<<getPos(move[0],move[1]));
free&=~posMask;
byte value=move[2];
if(value>0){
myTiles|=posMask;
myUsed|=(1<<value);
myValues=insertValue(posMask,myTiles,myValues,value);
}else if(value<0){
oppTiles|=posMask;
oppUsed|=(1<<-value);
oppValues=insertValue(posMask,oppTiles,oppValues,-value);
}
}
@Override
public void undoMove(byte[]move){
long posMask=(1L<<getPos(move[0],move[1]));
free|=posMask;
byte value=move[2];
if(value>0){
myTiles&=~posMask;
myUsed&=~(1<<value);
myValues=removeValue(posMask,myTiles,myValues);
}else if(value<0){
oppTiles&=~posMask;
oppUsed&=~(1<<-value);
oppValues=removeValue(posMask,oppTiles,oppValues);
}
}
private int getValueIndex(long posMask,long tiles){
return 4*Long.bitCount(tiles&(posMask-1));
}
private byte getValue(long posMask,long tiles,long values){
return(byte)((values>>>getValueIndex(posMask,tiles))&0b1111);
}
private long insertValue(long posMask,long tiles,long values,int value){
int index=getValueIndex(posMask,tiles);
return(values&((1L<<index)-1))
|((values<<4)&~((1L<<(index+4))-1))
|((long)value)<<index;
}
private long removeValue(long posMask,long tiles,long values){
int index=getValueIndex(posMask,tiles);
long preIndexMask=(1L<<index)-1;
return(values&preIndexMask)
|((values>>4)&~preIndexMask);
}
@Override
public boolean haveIUsed(byte value){
return(myUsed&(1<<value))>0;
}
@Override
public boolean hasOppUsed(byte value){
return(oppUsed&(1<<value))>0;
}
@Override
public boolean isGameOver(){
return getFreeSpots()==1;
}
}
abstract class Player {
protected final static boolean DEBUG=false;
protected Board board;
protected final String name;
public Player(String name){
this.name=name;
}
public String getName(){
return name;
}
public Board getBoard(){
return board;
}
public void play(BufferedReader in,PrintStream out)throws IOException{
initialize();
for(int i=0;i<5;i++){
byte[]loc=Util.getCoordinates(in.readLine());
block(loc[0],loc[1]);
}
for(String input=in.readLine();!(input==null||"Quit".equals(input));input=in.readLine()){
if(!"Start".equals(input)){
processMove(Util.parseMove(input),false);
}
byte[]move=move();
out.println(Util.coordinatesToString(move[0],move[1])+"="+move[2]);
}
}
public void initialize(){
initialize(new BitBoard());
}
public void initialize(Board currentBoard){
board=currentBoard;
}
public void block(byte a,byte b){
board.block(a,b);
}
public void processMove(byte[]move,boolean mine){
board.applyMove(new byte[]{move[0],move[1],(mine?move[2]:(byte)-move[2])});
}
public byte[]move(){
byte[]move=selectMove();
processMove(move,true);
return move;
}
protected abstract byte[]selectMove();
}
class AspirationPlayer extends Player {
private static final boolean DEBUG_AB=false;
private static final byte[]FAIL_HIGH=new byte[0];
private static final byte[]FAIL_LOW=new byte[0];
public static int WINDOW_SIZE=10000;
private final Evaluator evaluator;
private final MoveGenerator generator;
private int depth;
private int prevScore=0;
public AspirationPlayer(String name,Evaluator evaluator,MoveGenerator generator,int depth){
super(name);
this.evaluator=evaluator;
this.generator=generator;
this.depth=depth;
}
public int getDepth(){
return depth;
}
public void setDepth(int depth){
this.depth=depth;
}
@Override
public void initialize(Board currentBoard){
super.initialize(currentBoard);
prevScore=0;
}
@Override
protected byte[]selectMove(){
byte[]move=topLevelSearch(prevScore-WINDOW_SIZE,prevScore+WINDOW_SIZE);
if(move==FAIL_HIGH){
return topLevelSearch(prevScore+WINDOW_SIZE,Integer.MAX_VALUE);
}else if(move==FAIL_LOW){
return topLevelSearch(Integer.MIN_VALUE+1,prevScore-WINDOW_SIZE);
}else{
return move;
}
}
private byte[]topLevelSearch(int alpha,int beta){
if(DEBUG_AB){
System.err.printf(getName()+":Starting search with interval=[%d,%d]%n",alpha,beta);
}
int bestValue=Integer.MIN_VALUE+1;
byte[]bestMove=null;
List<byte[]>moves=generator.generateMoves(board,true);
for(byte[]move:moves){
if(DEBUG_AB){
System.err.println(getName()+":Evaluating my move "+Arrays.toString(move));
}
board.applyMove(move);
int value=-negamax(-1,depth,-beta,-alpha);
board.undoMove(move);
if(DEBUG_AB){
System.err.println(getName()+":Value of my move "+Arrays.toString(move)+" is "+value);
}
if(value>bestValue){
bestValue=value;
bestMove=move;
if(value>alpha){
alpha=value;
if(beta<=alpha){
return FAIL_HIGH;
}
}
}
}
if(bestValue<alpha){
return FAIL_LOW;
}else{
prevScore=bestValue;
return bestMove;
}
}
private int negamax(int player,int depth,int alpha,int beta){
if(DEBUG_AB){
System.err.printf("%s: Running negamax with%d turns left,interval=[%d,%d]and board state:%n",getName(),depth,alpha,beta);
Util.print(board);
}
if(depth==0||board.isGameOver()){
return player*evaluator.evaluate(board);
}
int bestValue=Integer.MIN_VALUE+1;
List<byte[]>moves=generator.generateMoves(board,player>0);
for(byte[]move:moves){
if(DEBUG_AB){
System.err.printf("%s:  Evaluating move%s%n",getName(),Arrays.toString(move));
}
board.applyMove(move);
int value=-negamax(-player,depth-1,-beta,-alpha);
board.undoMove(move);
if(DEBUG_AB){
System.err.printf("%s:  Got back a score of%d%n",getName(),value);
}
if(value>bestValue){
bestValue=value;
if(value>alpha){
alpha=value;
if(beta<=alpha){
break;
}
}
}
}
return bestValue;
}
}
interface Evaluator {
public abstract int evaluate(Board board);
}
class MedianFree implements Evaluator {
@Override
public int evaluate(Board board){
List<Integer>holeValues=new ArrayList<>();
for(byte a=0;a<8;a++){
for(byte b=0;b<8-a;b++){
if(board.isFree(a,b)){
holeValues.add(board.getHoleValue(a,b));
}
}
}
Collections.sort(holeValues);
return holeValues.get((holeValues.size()-1)/2);
}
}
interface MoveGenerator {
public abstract List<byte[]>generateMoves(Board board,boolean player1);
}
class MostFreeMax implements MoveGenerator {
@Override
public List<byte[]>generateMoves(Board board,boolean player1){
List<byte[]>moves=new ArrayList<>();
byte v=getMaxValueLeft(board,player1);
int mostFree=0;
for(byte a=0;a<8;a++){
for(byte b=0;b<8-a;b++){
if(!board.isFree(a,b)){
continue;
}
int free=board.getFreeSpotsAround(a,b);
if(free<mostFree){
continue;
}else if(free>mostFree){
mostFree=free;
moves.clear();
}
moves.add(new byte[]{a,b,(player1?v:(byte)-v)});
}
}
return moves;
}
private byte getMaxValueLeft(Board board,boolean player1){
for(byte v=15;v>0;v--){
if((player1&&!board.haveIUsed(v))||(!player1&&!board.hasOppUsed(v))){
return v;
}
}
throw new IllegalArgumentException();
}
}
