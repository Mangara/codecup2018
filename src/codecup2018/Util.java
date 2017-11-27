package codecup2018;

import codecup2018.data.Board;

public class Util {

    public static String coordinatesToString(byte a, byte b) {
        return Character.toString((char) ('A' + a)) + Character.toString((char) ('1' + b));
    }

    public static byte[] parseMove(String move) {
        return new byte[]{(byte) (move.charAt(0) - 'A'), (byte) (move.charAt(1) - '1'), (byte) (Integer.parseInt(move.substring(3)))};
    }

    public static byte[] getCoordinates(String location) {
        return new byte[]{(byte) (location.charAt(0) - 'A'), (byte) (location.charAt(1) - '1')};
    }
    
    public static void print(Board board) {
        for (byte h = 0; h < 8; h++) {
            System.err.print(spaces(7 - h));
            for (byte i = 0; i <= h; i++) {
                if (i > 0) {
                    System.err.print(' ');
                }
                
                byte value = board.get((byte) (h - i), i);

                System.err.print(value == Board.BLOCKED ? "  X" : String.format("%3d", value));
            }
            System.err.println(spaces(7 - h));
        }
        System.err.println("nFree: " + board.getNFreeSpots());
    }

    private static String spaces(int n) {
        switch (n) {
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
