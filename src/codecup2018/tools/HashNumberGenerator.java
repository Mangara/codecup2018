package codecup2018.tools;

import java.util.Random;

public class HashNumberGenerator {

    public static void main(String[] args) {
        new HashNumberGenerator();
    }

    public HashNumberGenerator() {
        findIndependentIntegers(64);
    }

    int[] integers;
    long[] longs;

    private void findIndependentIntegers(int n) {
        for (int seed = 611382270;; seed++) {
            Random rand = new Random(seed);
            integers = new int[n];
            longs = new long[n];

            for (int i = 0; i < n; i++) {
                integers[i] = rand.nextInt();
                longs[i] = rand.nextLong();
            }

            if (noZero1() && noZero2() && noZero3() && noZero4() && noZero5() && noZero6()
                    && noZeroL1() && noZeroL2() && noZeroL3() && noZeroL4() && noZeroL5() && noZeroL6()) {
                System.out.println(seed);
            }
        }
    }

    private boolean noZero1() {
        for (int integer : integers) {
            if (integer == 0) {
                System.out.println("Zero1");
                return false;
            }
        }

        return true;
    }

    private boolean noZero2() {
        for (int i = 0; i < integers.length; i++) {
            for (int j = i + 1; j < integers.length; j++) {
                if ((integers[i] ^ integers[j]) == 0) {
                    System.out.println("Zero2");
                    return false;
                }
            }
        }

        return true;
    }

    private boolean noZero3() {
        for (int i = 0; i < integers.length; i++) {
            for (int j = i + 1; j < integers.length; j++) {
                for (int k = j + 1; k < integers.length; k++) {
                    if ((integers[i] ^ integers[j] ^ integers[k]) == 0) {
                        System.out.println("Zero3");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean noZero4() {
        for (int i = 0; i < integers.length; i++) {
            for (int j = i + 1; j < integers.length; j++) {
                for (int k = j + 1; k < integers.length; k++) {
                    for (int l = k + 1; l < integers.length; l++) {
                        if ((integers[i] ^ integers[j] ^ integers[k] ^ integers[l]) == 0) {
                            System.out.println("Zero4");
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean noZero5() {
        for (int i = 0; i < integers.length; i++) {
            for (int j = i + 1; j < integers.length; j++) {
                for (int k = j + 1; k < integers.length; k++) {
                    for (int l = k + 1; l < integers.length; l++) {
                        for (int m = l + 1; m < integers.length; m++) {
                            if ((integers[i] ^ integers[j] ^ integers[k] ^ integers[l] ^ integers[m]) == 0) {
                                System.out.println("Zero5");
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean noZero6() {
        for (int i = 0; i < integers.length; i++) {
            for (int j = i + 1; j < integers.length; j++) {
                for (int k = j + 1; k < integers.length; k++) {
                    for (int l = k + 1; l < integers.length; l++) {
                        for (int m = l + 1; m < integers.length; m++) {
                            for (int n = m + 1; n < integers.length; n++) {
                                if ((integers[i] ^ integers[j] ^ integers[k] ^ integers[l] ^ integers[m] ^ integers[n]) == 0) {
                                    System.out.println("Zero6");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
    
    private boolean noZeroL1() {
        for (long integer : longs) {
            if (integer == 0) {
                System.out.println("ZeroL1");
                return false;
            }
        }

        return true;
    }

    private boolean noZeroL2() {
        for (int i = 0; i < longs.length; i++) {
            for (int j = i + 1; j < longs.length; j++) {
                if ((longs[i] ^ longs[j]) == 0) {
                    System.out.println("ZeroL2");
                    return false;
                }
            }
        }

        return true;
    }

    private boolean noZeroL3() {
        for (int i = 0; i < longs.length; i++) {
            for (int j = i + 1; j < longs.length; j++) {
                for (int k = j + 1; k < longs.length; k++) {
                    if ((longs[i] ^ longs[j] ^ longs[k]) == 0) {
                        System.out.println("ZeroL3");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean noZeroL4() {
        for (int i = 0; i < longs.length; i++) {
            for (int j = i + 1; j < longs.length; j++) {
                for (int k = j + 1; k < longs.length; k++) {
                    for (int l = k + 1; l < longs.length; l++) {
                        if ((longs[i] ^ longs[j] ^ longs[k] ^ longs[l]) == 0) {
                            System.out.println("ZeroL4");
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean noZeroL5() {
        for (int i = 0; i < longs.length; i++) {
            for (int j = i + 1; j < longs.length; j++) {
                for (int k = j + 1; k < longs.length; k++) {
                    for (int l = k + 1; l < longs.length; l++) {
                        for (int m = l + 1; m < longs.length; m++) {
                            if ((longs[i] ^ longs[j] ^ longs[k] ^ longs[l] ^ longs[m]) == 0) {
                                System.out.println("ZeroL5");
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean noZeroL6() {
        for (int i = 0; i < longs.length; i++) {
            for (int j = i + 1; j < longs.length; j++) {
                for (int k = j + 1; k < longs.length; k++) {
                    for (int l = k + 1; l < longs.length; l++) {
                        for (int m = l + 1; m < longs.length; m++) {
                            for (int n = m + 1; n < longs.length; n++) {
                                if ((longs[i] ^ longs[j] ^ longs[k] ^ longs[l] ^ longs[m] ^ longs[n]) == 0) {
                                    System.out.println("ZeroL6");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

}
