package codecup2018;

public class Util {

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
