package codecup2018.data;

import java.util.List;

public interface Board {

    public static final byte BLOCKED = 120;
    public static final byte FREE = 0;

    public byte get(byte a, byte b);
    
    public void block(byte a, byte b);

    public boolean isFree(byte a, byte b);
    
    public int getNFreeSpots();

    public int getHoleValue(byte a, byte b);

    public int getFreeSpotsAround(byte a, byte b);
    
    public List<byte[]> getFreeSpots();

    public void applyMove(byte[] move);

    public void undoMove(byte[] move);

    public boolean haveIUsed(byte value);

    public boolean hasOppUsed(byte value);

    public boolean isGameOver();
}
