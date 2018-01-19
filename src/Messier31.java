import java.io.BufferedReader;
import java.util.List;
import java.util.Arrays;
import java.io.PrintStream;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.util.Random;
public class Messier31 {
public static void main(String[]args)throws IOException{
Player.TIMING=true;
IterativeDFSPlayer.DEBUG_FINAL_VALUE=true;
Player p=getPlayer();
p.play(new BufferedReader(new InputStreamReader(System.in)),System.out);
}
public static Player getPlayer(){
return new IterativeDFSPlayer("ID_IEV_BSM1_LD4700",new IncrementalExpectedValue(),new BucketSortMaxMovesOneHole(),new ProportionalController(4700,ProportionalController.LINEAR_DECAY));
}
}
abstract class Board {
public static final byte BLOCKED=16;
public static final byte FREE=0;
public static final int[]KEY_POSITION_NUMBERS=new int[64*32];
public static final long[]HASH_POSITION_NUMBERS=new long[64*32];
static{
Random rand=new Random(611382272);
for(int i=0;i<64*32;i++){
KEY_POSITION_NUMBERS[i]=rand.nextInt();
HASH_POSITION_NUMBERS[i]=rand.nextLong();
}
}
public abstract byte get(byte pos);
public abstract void block(byte pos);
public abstract void applyMove(int move);
public abstract void undoMove(int move);
public abstract boolean haveIUsed(byte value);
public abstract boolean hasOppUsed(byte value);
public abstract boolean isFree(byte pos);
public abstract int getNFreeSpots();
public abstract int getHoleValue(byte pos);
public abstract int getFreeSpotsAround(byte pos);
public abstract byte[]getFreeSpots();
public abstract boolean isGameOver();
public abstract boolean isGameInEndGame();
public abstract int getTranspositionTableKey();
public abstract long getHash();
public int getFinalScore(){
if(!isGameOver()){
System.err.println("getFinalScore called when game is not over.");
return 0;
}
return getHoleValue(getFreeSpots()[0]);
}
public static final int MOVE_POS_MASK=(1<<6)-1;
public static final int MOVE_VAL_MASK=((1<<5)-1)<<6;
public static final int MOVE_EVAL_MASK=((1<<21)-1)<<11;
public static final int MOVE_NOT_EVAL_MASK=~MOVE_EVAL_MASK;
public static final int MIN_EVAL=-750001;
public static final int MAX_EVAL=750001;
public static final int MIN_EVAL_MOVE=buildMove((byte)0,(byte)0,MIN_EVAL);
public static final int MAX_EVAL_MOVE=buildMove((byte)0,(byte)0,MAX_EVAL);
public static final int ILLEGAL_MOVE=buildMove((byte)63,(byte)0,0);
public static final byte getMovePos(int move){
return(byte)(move&MOVE_POS_MASK);
}
public static final byte getMoveVal(int move){
return(byte)(((move&MOVE_VAL_MASK)>>6)-15);
}
public static final int getMoveEval(int move){
return move>>11;
}
public static final boolean equalMoves(int move1,int move2){
return(move1&MOVE_NOT_EVAL_MASK)==(move2&MOVE_NOT_EVAL_MASK);
}
public static final int setMovePos(int move,byte pos){
return(move&~MOVE_POS_MASK)|pos;
}
public static final int setMoveVal(int move,byte val){
return(move&~MOVE_VAL_MASK)|(val+15<<6);
}
public static final int setMoveEval(int move,int eval){
return(move&MOVE_NOT_EVAL_MASK)|(eval<<11);
}
public static final int negateEval(int move){
return(move&MOVE_NOT_EVAL_MASK)|((-(move>>11)<<11)&MOVE_EVAL_MASK);
}
public static final int clearEval(int move){
return move&MOVE_NOT_EVAL_MASK;
}
public static final int buildMove(byte pos,byte val,int eval){
return(eval<<11)|(val+15<<6)|pos;
}
public final boolean isLegalMove(int move){
byte pos=getMovePos(move);
byte val=getMoveVal(move);
return isValidPos(pos)&&isFree(getMovePos(move))&&((val>0&&!haveIUsed(val))||(val<0&&!hasOppUsed((byte)-val)));
}
public static final byte getPos(byte a,byte b){
return(byte)(8*a+b);
}
public static final boolean isValidPos(int pos){
return(pos&0b111)+(pos>>>3)<8;
}
public static final byte[]getCoordinates(byte pos){
return new byte[]{(byte)(pos/8),(byte)(pos %8)};
}
public static final byte parsePos(String location){
return(byte)(8*(location.charAt(0)-'A')+location.charAt(1)-'1');
}
public static final String posToString(byte pos){
return Character.toString((char)('A'+pos/8))+Character.toString((char)('1'+pos %8));
}
public static final String coordinatesToString(byte a,byte b){
return Character.toString((char)('A'+a))+Character.toString((char)('1'+b));
}
public static final int parseMove(String move){
return buildMove((byte)(8*(move.charAt(0)-'A')+move.charAt(1)-'1'),(byte)Integer.parseInt(move.substring(3)),0);
}
public static final String moveToString(int move){
return posToString(getMovePos(move))+'='+getMoveVal(move);
}
public static final String movesToString(int[]moves){
StringBuilder sb=new StringBuilder();
sb.append('[');
for(int i=0;i<moves.length;i++){
if(i>0){
sb.append(",");
}
sb.append(moveToString(moves[i]));
}
return sb.append(']').toString();
}
public static final void print(Board board){
for(byte h=0;h<8;h++){
System.err.printf("%"+(2*(7-h)+1)+"s","");
for(byte i=0;i<=h;i++){
if(i>0){
System.err.print(' ');
}
byte value=board.get(getPos((byte)(h-i),i));
System.err.print(value==Board.BLOCKED?"  X":String.format("%3d",value));
}
System.err.printf("%"+(2*(7-h)+1)+"s%n","");
}
System.err.print("nFree:"+board.getNFreeSpots());
System.err.println();
}
}
class BitBoard extends Board {
private static final long BOARD=0b0000000100000011000001110000111100011111001111110111111111111111L;
private static final long EXCLUDE_ONE=~0b1L;
private static final long EXCLUDE_EIGHT=~0b10000000L;
private static final long EXCLUDE_NINE=~0b100000000L;
private static final long[]NEIGHBOURS=new long[64];
static{
for(byte a=0;a<8;a++){
for(byte pos=(byte)(8*a);pos<7*a+8;pos++){
long posMask=posMask(pos);
NEIGHBOURS[pos]
=(((posMask<<1)&EXCLUDE_NINE)
|((posMask>>>1)&EXCLUDE_EIGHT)
|((posMask<<7)&EXCLUDE_EIGHT)
|((posMask>>>7)&EXCLUDE_ONE)
|(posMask<<8)
|(posMask>>>8))
&BOARD;
}
}
}
private long free=0;
private long myTiles=0;
private long oppTiles=0;
private long myValues=0;
private long oppValues=0;
private short myUsed=0;
private short oppUsed=0;
public int freeEdgeCount=0;
private int key;
private long hash;
public BitBoard(){
free=BOARD;
freeEdgeCount=(3*2+18*4+15*6)/2;
initializeTranspositionTableValues();
}
public BitBoard(Board board){
int myValIndex=0;
int oppValIndex=0;
for(byte a=0;a<8;a++){
for(byte pos=(byte)(8*a);pos<7*a+8;pos++){
byte val=board.get(pos);
long posMask=posMask(pos);
if(val==FREE){
free|=posMask;
freeEdgeCount+=getFreeSpotsAround(pos);
}else if(val==BLOCKED){
}else if(val>0){
myTiles|=posMask;
myValues|=((long)val)<<(4*myValIndex);
myUsed|=(1<<val);
myValIndex++;
}else if(val<0){
oppTiles|=posMask;
oppValues|=((long)-val)<<(4*oppValIndex);
oppUsed|=(1<<-val);
oppValIndex++;
}
}
}
initializeTranspositionTableValues();
}
private static long posMask(byte pos){
return 1L<<pos;
}
@Override
public byte get(byte pos){
long posMask=posMask(pos);
if((free&posMask)!=0){
return FREE;
}
if((myTiles&posMask)!=0){
return getValue(posMask,myTiles,myValues);
}
if((oppTiles&posMask)!=0){
return(byte)-getValue(posMask,oppTiles,oppValues);
}
return BLOCKED;
}
@Override
public boolean isFree(byte pos){
return(free&posMask(pos))!=0;
}
@Override
public int getNFreeSpots(){
return Long.bitCount(free);
}
@Override
public byte[]getFreeSpots(){
byte[]result=new byte[getNFreeSpots()];
long tempFree=free;
for(int i=0;tempFree!=0;i++){
int pos=Long.numberOfTrailingZeros(tempFree);
result[i]=(byte)pos;
tempFree&=(tempFree-1);
}
return result;
}
@Override
public int getFreeSpotsAround(byte pos){
return Long.bitCount(free&NEIGHBOURS[pos]);
}
@Override
public int getHoleValue(byte pos){
long neighbours=NEIGHBOURS[pos];
long myNeighbours=myTiles&neighbours;
long oppNeighbours=oppTiles&neighbours;
int total=0;
while(myNeighbours!=0){
long posMask=Long.lowestOneBit(myNeighbours);
total+=getValue(posMask,myTiles,myValues);
myNeighbours^=posMask;
}
while(oppNeighbours!=0){
long posMask=Long.lowestOneBit(oppNeighbours);
total-=getValue(posMask,oppTiles,oppValues);
oppNeighbours^=posMask;
}
return total;
}
@Override
public void block(byte pos){
free&=~posMask(pos);
freeEdgeCount-=getFreeSpotsAround(pos);
int index=32*pos+FREE+15;
int newIndex=index-FREE+BLOCKED;
key^=KEY_POSITION_NUMBERS[index]^KEY_POSITION_NUMBERS[newIndex];
hash^=HASH_POSITION_NUMBERS[index]^HASH_POSITION_NUMBERS[newIndex];
}
@Override
public void applyMove(int move){
byte pos=getMovePos(move);
long posMask=posMask(pos);
byte value=getMoveVal(move);
free&=~posMask;
freeEdgeCount-=getFreeSpotsAround(pos);
if(value>0){
myTiles|=posMask;
myUsed|=(1<<value);
myValues=insertValue(posMask,myTiles,myValues,value);
}else if(value<0){
oppTiles|=posMask;
oppUsed|=(1<<-value);
oppValues=insertValue(posMask,oppTiles,oppValues,-value);
}
int index=32*pos+FREE+15;
int newIndex=index-FREE+value;
key^=KEY_POSITION_NUMBERS[index]^KEY_POSITION_NUMBERS[newIndex];
hash^=HASH_POSITION_NUMBERS[index]^HASH_POSITION_NUMBERS[newIndex];
}
@Override
public void undoMove(int move){
byte pos=getMovePos(move);
long posMask=posMask(pos);
byte value=getMoveVal(move);
free|=posMask;
freeEdgeCount+=getFreeSpotsAround(pos);
if(value>0){
myTiles&=~posMask;
myUsed&=~(1<<value);
myValues=removeValue(posMask,myTiles,myValues);
}else if(value<0){
oppTiles&=~posMask;
oppUsed&=~(1<<-value);
oppValues=removeValue(posMask,oppTiles,oppValues);
}
int index=32*pos+value+15;
int newIndex=index-value+FREE;
key^=KEY_POSITION_NUMBERS[index]^KEY_POSITION_NUMBERS[newIndex];
hash^=HASH_POSITION_NUMBERS[index]^HASH_POSITION_NUMBERS[newIndex];
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
return(myUsed&(1<<value))!=0;
}
@Override
public boolean hasOppUsed(byte value){
return(oppUsed&(1<<value))!=0;
}
@Override
public boolean isGameOver(){
return getNFreeSpots()==1;
}
@Override
public boolean isGameInEndGame(){
return freeEdgeCount==0;
}
private void initializeTranspositionTableValues(){
key=0;
hash=0;
for(byte a=0;a<8;a++){
for(byte pos=(byte)(8*a);pos<7*a+8;pos++){
int index=32*pos+get(pos)+15;
key^=KEY_POSITION_NUMBERS[index];
hash^=HASH_POSITION_NUMBERS[index];
}
}
}
@Override
public int getTranspositionTableKey(){
return key;
}
@Override
public long getHash(){
return hash;
}
}
abstract class Player {
public static boolean TIMING=false;
public static boolean DEBUG=false;
protected Board board;
protected final String name;
protected int turn;
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
long start=0;
if(TIMING){
start=System.currentTimeMillis();
}
initialize();
for(int i=0;i<5;i++){
byte pos=Board.parsePos(in.readLine());
block(pos);
}
if(TIMING){
System.err.printf("Initialization took %d ms.%n",System.currentTimeMillis()-start);
}
for(String input=in.readLine();!(input==null||"Quit".equals(input));input=in.readLine()){
if(TIMING){
start=System.currentTimeMillis();
}
if(!"Start".equals(input)){
processMove(Board.parseMove(input),false);
}
int move=move();
if(TIMING){
System.err.printf("Move %d took %d ms.%n",turn-1,System.currentTimeMillis()-start);
}
out.println(Board.moveToString(move));
}
}
public void initialize(){
initialize(new BitBoard());
turn=1;
}
public void initialize(Board currentBoard){
board=currentBoard;
turn=Math.max(1,32-currentBoard.getNFreeSpots());
}
public void block(byte pos){
board.block(pos);
}
public void processMove(int move,boolean mine){
board.applyMove(mine?move:Board.setMoveVal(move,(byte)-Board.getMoveVal(move)));
turn++;
}
public int move(){
int move=selectMove();
processMove(move,true);
return move;
}
protected abstract int selectMove();
}
abstract class StandardPlayer extends Player {
protected final Evaluator evaluator;
protected final MoveGenerator generator;
public StandardPlayer(String name,Evaluator evaluator,MoveGenerator generator){
super(name);
this.evaluator=evaluator;
this.generator=generator;
}
@Override
public void initialize(Board currentBoard){
super.initialize(currentBoard);
evaluator.initialize(currentBoard);
}
@Override
public void block(byte pos){
super.block(pos);
evaluator.block(pos);
}
@Override
public void processMove(int move,boolean mine){
int m=(mine?move:Board.setMoveVal(move,(byte)-Board.getMoveVal(move)));
board.applyMove(m);
evaluator.applyMove(m);
turn++;
}
}
abstract class TimedPlayer extends StandardPlayer {
protected final TimeController controller;
public TimedPlayer(String name,Evaluator evaluator,MoveGenerator generator,TimeController controller){
super(name,evaluator,generator);
this.controller=controller;
}
@Override
public void initialize(Board currentBoard){
super.initialize(currentBoard);
controller.reset();
}
@Override
protected int selectMove(){
controller.startMove();
int time=controller.getMillisecondsForMove(turn);
if(TIMING){
System.err.printf("%d ms for this move.%n",time);
}
int move=selectMove(time);
controller.endMove();
return move;
}
protected abstract int selectMove(int millisecondsToMove);
}
class SimpleMaxPlayer extends StandardPlayer {
public SimpleMaxPlayer(String name,Evaluator evaluator,MoveGenerator generator){
super(name,evaluator,generator);
}
@Override
protected int selectMove(){
int bestMove=0;
double bestValue=Double.NEGATIVE_INFINITY;
int[]moves=generator.generateMoves(board,true);
for(int move:moves){
board.applyMove(move);
double value=evaluator.evaluate(board);
board.undoMove(move);
if(DEBUG){
System.err.println(getName()+":Move "+Board.moveToString(move)+" has value "+value);
}
if(value>bestValue){
bestValue=value;
bestMove=move;
}
}
return bestMove;
}
}
class IterativeDFSPlayer extends TimedPlayer {
public static boolean DEBUG_FINAL_VALUE=false;
private static final boolean DEBUG_AB=false;
private static final boolean DEBUG_BETA=false;
private static final int DEBUG_TURN=-1;
private final static int INITIAL_WINDOW_SIZE=5000;
private final static double WINDOW_FACTOR=1.75;
private final Evaluator endgameEvaluator=new MedianFree();
private final Player endgamePlayer=new SimpleMaxPlayer("Expy",new ExpectedValue(),new AllMoves());
private final static int TABLE_SIZE_POWER=20;
private final static int TABLE_SIZE=1<<TABLE_SIZE_POWER;
private final static int TABLE_KEY_MASK=TABLE_SIZE-1;
private final TranspositionEntry[]transpositionTable=new TranspositionEntry[TABLE_SIZE];
private int prevScore=0;
private long turnStartTime;
private long nsToMove;
private int maxDepth;
private int depthToTurnBase;
private final int[][]killerMoves;
public IterativeDFSPlayer(String name,Evaluator evaluator,MoveGenerator generator,TimeController controller){
super(name,evaluator,generator,controller);
killerMoves=new int[30][2];
for(int[]killers:killerMoves){
Arrays.fill(killers,Board.ILLEGAL_MOVE);
}
}
@Override
public void initialize(Board currentBoard){
super.initialize(currentBoard);
prevScore=evaluator.evaluate(board);
turn=1;
Arrays.fill(transpositionTable,null);
for(int[]killers:killerMoves){
Arrays.fill(killers,Board.ILLEGAL_MOVE);
}
}
@Override
protected int selectMove(int millisecondsToMove){
turnStartTime=System.nanoTime();
timeUp=false;
callsToCheck=100;
if(board.isGameInEndGame()||millisecondsToMove<=0){
endgamePlayer.initialize(board);
return endgamePlayer.selectMove();
}
nsToMove=1000000*(long)millisecondsToMove;
maxDepth=0;
int movesLeft=30-turn;
int bestMove=Board.ILLEGAL_MOVE;
do{
int move=searchForBestMove();
if(move!=Board.ILLEGAL_MOVE){
bestMove=move;
}
maxDepth+=2;
}while(maxDepth<=movesLeft&&!timeIsUp());
int eval=Board.getMoveEval(bestMove);
if(DEBUG_AB||DEBUG_FINAL_VALUE||turn==DEBUG_TURN){
System.err.println("Turn "+turn+" final:"+eval);
}
return bestMove;
}
private int searchForBestMove(){
depthToTurnBase=turn+maxDepth;
int window=INITIAL_WINDOW_SIZE;
int alpha=prevScore-window/2,beta=prevScore+window/2;
int move,eval;
boolean failLow=false;
boolean failHigh=false;
while(true){
if(DEBUG_AB||DEBUG_FINAL_VALUE||turn==DEBUG_TURN){
System.err.printf("Searching[%d,%d]",alpha,beta);
}
move=negamax((byte)1,(byte)(maxDepth+1),alpha,beta);
if(move==Board.ILLEGAL_MOVE){
return move;
}
eval=Board.getMoveEval(move);
if(DEBUG_AB||DEBUG_FINAL_VALUE||turn==DEBUG_TURN){
System.err.printf("=>%d%n",eval);
}
if(eval<=alpha){
failLow=true;
alpha=eval-window;
}else if(eval>=beta){
failHigh=true;
beta=eval+window;
}else{
break;
}
if(maxDepth>0&&timeIsUp()){
return Board.ILLEGAL_MOVE;
}
if(failLow&&failHigh){
System.err.println("Search is unstable on turn "+turn+" with depth "+maxDepth);
move=negamax((byte)1,(byte)(maxDepth+1),Board.MIN_EVAL,Board.MAX_EVAL);
eval=Board.getMoveEval(move);
break;
}
window*=WINDOW_FACTOR;
}
if(DEBUG_AB||DEBUG_FINAL_VALUE||turn==DEBUG_TURN){
System.err.println("Turn "+turn+" depth "+maxDepth+" value:"+eval);
}
prevScore=eval;
return move;
}
private int negamax(byte player,byte depth,int alpha,int beta){
if(depth==0||board.isGameOver()){
return Board.buildMove((byte)0,(byte)0,player*evaluator.evaluate(board));
}
if(board.isGameInEndGame()){
return Board.buildMove((byte)0,(byte)0,player*(endgameEvaluator.evaluate(board)+evaluator.evaluate(board)/100));
}
if(maxDepth>0&&timeIsUp()){
return Board.ILLEGAL_MOVE;
}
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sRunning negamax with %d plies left,interval=[%d,%d]and board state:%n",getName(),"",depth,alpha,beta);
Board.print(board);
}
int bestMove=Board.MIN_EVAL_MOVE;
int bestEval=Board.MIN_EVAL;
int myAlpha=alpha;
int myBeta=beta;
TranspositionEntry entry=transpositionTable[board.getTranspositionTableKey()&TABLE_KEY_MASK];
boolean tableMatch=entry!=null&&entry.hash==board.getHash()&&board.isLegalMove(entry.bestMove);
if(tableMatch){
int entryEval=Board.getMoveEval(entry.bestMove);
if(entry.depthSearched>=depth){
switch(entry.type){
case TranspositionEntry.EXACT:
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sExact transposition table result:%d%n",getName(),"",entryEval);
}
return entry.bestMove;
case TranspositionEntry.LOWER_BOUND:
if(entryEval>=beta){
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sLower bound transposition table result:%d%n",getName(),"",entryEval);
}
return entry.bestMove;
}
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sInitializing alpha from transposition table result:%d%n",getName(),"",entryEval);
}
myAlpha=Math.max(myAlpha,entryEval);
break;
case TranspositionEntry.UPPER_BOUND:
if(entryEval<=alpha){
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sUpper bound transposition table result:%d%n",getName(),"",entryEval);
}
return entry.bestMove;
}
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sInitializing beta from transposition table result:%d%n",getName(),"",entryEval);
}
myBeta=Math.min(myBeta,entryEval);
break;
}
}
if(depth==1){
bestMove=entry.bestMove;
bestEval=entryEval;
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sInitializing best move from transposition table:%s=>%d%n",getName(),"",Board.moveToString(bestMove),bestEval);
}
if(bestEval>myAlpha){
myAlpha=bestEval;
}
}else{
bestMove=evaluateMove(entry.bestMove,player,depth,myAlpha,myBeta," stored");
if(bestMove==Board.ILLEGAL_MOVE){
return bestMove;
}
bestEval=Board.getMoveEval(bestMove);
if(bestEval>myAlpha){
myAlpha=bestEval;
}
}
}
if(myAlpha<myBeta){
for(int move:killerMoves[depthToTurnBase-depth]){
if(board.isLegalMove(move)&&!(tableMatch&&Board.equalMoves(move,entry.bestMove))){
move=evaluateMove(move,player,depth,myAlpha,myBeta," killer");
if(move==Board.ILLEGAL_MOVE){
return move;
}
if(move>bestMove){
int eval=Board.getMoveEval(move);
if(eval>bestEval){
bestMove=move;
bestEval=eval;
if(eval>myAlpha){
myAlpha=eval;
if(myBeta<=myAlpha){
if(DEBUG_AB||turn==DEBUG_TURN||DEBUG_BETA){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sBeta-cutoff from killer move %s%n",getName(),"",Board.moveToString(bestMove));
}
updateKillerMoves(depth,bestMove);
break;
}
}
}
}
}
}
}else{
if(DEBUG_AB||turn==DEBUG_TURN||DEBUG_BETA){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sBeta-cutoff from stored move %s%n",getName(),"",Board.moveToString(bestMove));
}
updateKillerMoves(depth,bestMove);
}
if(myAlpha<myBeta){
int[]moves=generator.generateMoves(board,player>0);
for(int move:moves){
if((tableMatch&&Board.equalMoves(move,entry.bestMove))||matchesKiller(move,depth)){
continue;
}
move=evaluateMove(move,player,depth,myAlpha,myBeta,"");
if(move==Board.ILLEGAL_MOVE){
return move;
}
if(move>bestMove){
int eval=Board.getMoveEval(move);
if(eval>bestEval){
bestMove=move;
bestEval=eval;
if(eval>myAlpha){
myAlpha=eval;
if(myBeta<=myAlpha){
if(DEBUG_AB||turn==DEBUG_TURN||DEBUG_BETA){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sBeta cut-off from move %s%n",getName(),"",Board.moveToString(bestMove));
}
updateKillerMoves(depth,bestMove);
break;
}
}
}
}
}
}
if(entry==null){
entry=new TranspositionEntry();
transpositionTable[board.getTranspositionTableKey()&TABLE_KEY_MASK]=entry;
}
if(entry.turn<turn||entry.depthSearched<depth){
entry.hash=board.getHash();
entry.bestMove=bestMove;
entry.depthSearched=depth;
entry.type=(bestEval>alpha?(bestEval<beta?TranspositionEntry.EXACT:TranspositionEntry.LOWER_BOUND):TranspositionEntry.UPPER_BOUND);
entry.turn=(byte)turn;
}else if(tableMatch&&entry.depthSearched==depth){
entry.bestMove=bestMove;
entry.type=(bestEval>alpha?(bestEval<beta?TranspositionEntry.EXACT:TranspositionEntry.LOWER_BOUND):TranspositionEntry.UPPER_BOUND);
}
return bestMove;
}
private int evaluateMove(int move,byte player,byte depth,int alpha,int beta,String type){
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sEvaluating%s move %s",getName(),"",type,Board.moveToString(move));
if(depth>1){
System.err.println();
}
}
board.applyMove(move);
evaluator.applyMove(move);
int result=Board.setMoveEval(move,-Board.getMoveEval(negamax((byte)-player,(byte)(depth-1),-beta,-alpha)));
board.undoMove(move);
evaluator.undoMove(move);
if(DEBUG_AB||turn==DEBUG_TURN){
if(depth>1){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sGot back a score of %d%n",getName(),"",Board.getMoveEval(result));
}else{
System.err.printf("=>%d%n",Board.getMoveEval(result));
}
}
return result;
}
private void updateKillerMoves(byte depth,int move){
int[]killers=killerMoves[depthToTurnBase-depth];
if(Board.equalMoves(killers[0],move)){
return;
}
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf("%s:%"+(2*(maxDepth-depth+1)+1)+"sUpdating killer moves with new move %s. Old:%s",getName(),"",Board.moveToString(move),killersToString());
}
killers[1]=killers[0];
killers[0]=move;
if(DEBUG_AB||turn==DEBUG_TURN){
System.err.printf(" New:%s%n",killersToString());
}
}
private String killersToString(){
StringBuilder sb=new StringBuilder();
for(int i=0;i<killerMoves.length;i++){
sb.append(' ').append(Integer.toString(i)).append(":[");
for(int j=0;j<killerMoves[0].length;j++){
if(j>0){
sb.append(',');
}
sb.append(Board.moveToString(killerMoves[i][j]));
}
sb.append(']');
}
return sb.toString();
}
private boolean matchesKiller(int move,byte depth){
int[]killers=killerMoves[depthToTurnBase-depth];
return Board.equalMoves(move,killers[0])||Board.equalMoves(move,killers[1]);
}
private boolean timeUp;
private int callsToCheck;
private boolean timeIsUp(){
if(callsToCheck==0){
callsToCheck=100;
timeUp=System.nanoTime()-turnStartTime>=nsToMove;
}else{
callsToCheck--;
}
return timeUp;
}
private class TranspositionEntry{
static final byte EXACT=3,LOWER_BOUND=4,UPPER_BOUND=5;
long hash;
int bestMove;
byte depthSearched;
byte type;
byte turn;
}
}
interface Evaluator {
public abstract int evaluate(Board board);
public void initialize(Board board);
public void block(byte pos);
public void applyMove(int move);
public void undoMove(int move);
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
for(byte pos=(byte)(8*a);pos<7*a+8;pos++){
if(board.isFree(pos)){
totalExpectedHoleValue+=board.getHoleValue(pos)+board.getFreeSpotsAround(pos)*expectedFree;
nHoles++;
}
}
}
return(int)(10000*totalExpectedHoleValue/nHoles);
}
@Override
public void initialize(Board board){
}
@Override
public void block(byte pos){
}
@Override
public void applyMove(int move){
}
@Override
public void undoMove(int move){
}
}
class MedianFree implements Evaluator {
@Override
public int evaluate(Board board){
int movesLeft=0;
for(byte v=1;v<=15;v++){
if(!board.haveIUsed(v)){
movesLeft++;
}
}
byte[]free=board.getFreeSpots();
for(int i=0;i<free.length;i++){
free[i]=(byte)board.getHoleValue(free[i]);
}
Arrays.sort(free);
return 10000*free[movesLeft];
}
@Override
public void initialize(Board board){
}
@Override
public void block(byte pos){
}
@Override
public void applyMove(int move){
}
@Override
public void undoMove(int move){
}
}
class IncrementalExpectedValue implements Evaluator {
private Board board;
private int nFree=0;
private int nUnused=0;
private int totalUnused=0;
private int totalHoleValue=0;
private int totalFreeDegree=0;
@Override
public void initialize(Board board){
this.board=board;
nFree=0;
nUnused=0;
totalUnused=0;
totalHoleValue=0;
totalFreeDegree=0;
for(byte a=0;a<8;a++){
for(byte pos=(byte)(8*a);pos<7*a+8;pos++){
byte v=board.get(pos);
if(v==Board.FREE){
nFree++;
totalHoleValue+=board.getHoleValue(pos);
totalFreeDegree+=board.getFreeSpotsAround(pos);
}
}
}
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
}
@Override
public void block(byte pos){
int holeValue=board.getHoleValue(pos);
int freeDegree=board.getFreeSpotsAround(pos);
nFree--;
totalHoleValue-=holeValue;
totalFreeDegree-=2*freeDegree;
}
@Override
public void applyMove(int move){
byte pos=Board.getMovePos(move);
byte val=Board.getMoveVal(move);
int holeValue=board.getHoleValue(pos);
int freeDegree=board.getFreeSpotsAround(pos);
nFree--;
totalHoleValue=totalHoleValue-holeValue+freeDegree*val;
totalFreeDegree-=2*freeDegree;
nUnused--;
totalUnused-=val;
}
@Override
public void undoMove(int move){
byte pos=Board.getMovePos(move);
byte val=Board.getMoveVal(move);
int holeValue=board.getHoleValue(pos);
int freeDegree=board.getFreeSpotsAround(pos);
nFree++;
totalHoleValue=totalHoleValue+holeValue-freeDegree*val;
totalFreeDegree+=2*freeDegree;
nUnused++;
totalUnused+=val;
}
@Override
public int evaluate(Board board){
if(board!=this.board){
throw new InternalError();
}
double expectedFree=(nUnused==0?0:totalUnused/(double)nUnused);
double totalExpectedHoleValue=totalHoleValue+expectedFree*totalFreeDegree;
return(int)(10000*totalExpectedHoleValue/nFree);
}
}
interface MoveGenerator {
public abstract int[]generateMoves(Board board,boolean player1);
}
class AllMoves implements MoveGenerator {
@Override
public int[]generateMoves(Board board,boolean player1){
byte[]free=board.getFreeSpots();
List<Byte>freeValues=getFreeValues(board,player1);
int[]moves=new int[free.length*freeValues.size()];
int i=0;
for(byte pos:free){
for(byte v:freeValues){
moves[i]=Board.buildMove(pos,v,0);
i++;
}
}
return moves;
}
private List<Byte>getFreeValues(Board board,boolean player1){
List<Byte>freeValues=new ArrayList<>();
for(byte v=1;v<=15;v++){
if((player1&&board.haveIUsed(v))||(!player1&&board.hasOppUsed(v))){
continue;
}
freeValues.add(player1?v:(byte)-v);
}
return freeValues;
}
}
class BucketSortMaxMovesOneHole implements MoveGenerator {
private final int[][]freeSorted=new int[7][31];
@Override
public int[]generateMoves(final Board board,boolean player1){
byte max=getMaxValueLeft(board,player1);
byte min=getMinValueLeft(board,player1);
byte[]free=board.getFreeSpots();
byte[]index=new byte[7];
for(int i=0;i<free.length;i++){
byte pos=free[i];
int freeAround=board.getFreeSpotsAround(pos);
freeSorted[freeAround][index[freeAround]++]=Board.buildMove(pos,max,0);
}
int[]moves=new int[free.length-(index[0]>1?index[0]-1:0)];
int movesIndex=0;
for(int i=6;i>0;i--){
System.arraycopy(freeSorted[i],0,moves,movesIndex,index[i]);
movesIndex+=index[i];
}
if(index[0]>1){
int worstHoleValue=76;
int worstHole=0;
for(int i=0;i<index[0];i++){
int hole=freeSorted[0][i];
int holeValue=board.getHoleValue(Board.getMovePos(hole));
if(!player1){
holeValue*=-1;
}
if(holeValue<worstHoleValue){
worstHoleValue=holeValue;
worstHole=hole;
}
}
moves[movesIndex]=Board.setMoveVal(worstHole,min);
}else if(index[0]==1){
moves[movesIndex]=Board.setMoveVal(freeSorted[0][0],min);
}
return moves;
}
private byte getMaxValueLeft(Board board,boolean player1){
for(byte v=15;v>0;v--){
if(player1){
if(!board.haveIUsed(v)){
return v;
}
}else{
if(!board.hasOppUsed(v)){
return(byte)-v;
}
}
}
System.err.println("ERROR:No value left for active player.");
Board.print(board);
for(int i=15;i>=1;i--){
System.err.printf("%6d",i);
}
System.err.println();
for(byte i=15;i>=1;i--){
System.err.printf("%6b",player1?board.haveIUsed(i):board.hasOppUsed(i));
}
System.err.println();
throw new IllegalArgumentException();
}
private byte getMinValueLeft(Board board,boolean player1){
for(byte v=1;v<=15;v++){
if(player1){
if(!board.haveIUsed(v)){
return v;
}
}else{
if(!board.hasOppUsed(v)){
return(byte)-v;
}
}
}
throw new IllegalArgumentException();
}
}
abstract class TimeController {
protected final int totalTimeMilliseconds;
protected int timeRemainingMilliseconds;
protected long moveStartTime;
public TimeController(int totalTimeMilliseconds){
this.totalTimeMilliseconds=totalTimeMilliseconds;
timeRemainingMilliseconds=totalTimeMilliseconds;
}
public void reset(){
timeRemainingMilliseconds=totalTimeMilliseconds;
}
public void startMove(){
moveStartTime=System.nanoTime();
}
public void endMove(){
int duration=(int)Math.ceil((System.nanoTime()-moveStartTime)/1000000.0);
timeRemainingMilliseconds-=duration;
if(Player.TIMING){
System.err.println("Move took "+duration+" ms.");
}
}
public abstract int getMillisecondsForMove(int turn);
protected int getTurnsRemaining(int turn){
return 15-(turn-1)/2;
}
}
class ProportionalController extends TimeController {
public static final double[]LINEAR_DECAY=new double[]{
0.164444444444444,
0.1796875,
0.198176291793313,
0.221127116502401,
0.250486696950032,
0.28961038961039,
0.344911639244363,
0.430697674418605,
0.588235294117647,
0.428571428571429,
0.5,
0.4,
0.5,
0.666666666666667,
1
};
private final double[]fractionOfRemaining;
public ProportionalController(int totalTimeMilliseconds){
super(totalTimeMilliseconds);
fractionOfRemaining=new double[]{
0.275,0.275,0.325,0.350,0.420,0.480,0.560,0.440,0.170,0.165,0.2,0.25,0.333,0.5,1
};
}
public ProportionalController(int totalTimeMilliseconds,double[]fractionOfRemaining){
super(totalTimeMilliseconds);
this.fractionOfRemaining=fractionOfRemaining;
}
@Override
public int getMillisecondsForMove(int turn){
return(int)Math.round(timeRemainingMilliseconds*fractionOfRemaining[(turn-1)/2]);
}
}
