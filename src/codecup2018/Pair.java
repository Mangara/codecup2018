package codecup2018;

/**
 * A generic pair of objects.
 * <p>
 * The order of the two objects matters. They can have different types, and even
 * if they don't, a pair (a, b) is different from a pair (b, a). This is
 * reflected in the {@code equals} and {@code hashCode} methods.
 *
 * @param <T1> The type of the first object
 * @param <T2> The type of the second object
 *
 */
public class Pair<T1, T2> {

    private T1 first;
    private T2 second;

    /**
     * Creates a new pair from the given objects.
     *
     * @param first
     * @param second
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first element of this pair.
     *
     * @return the first element
     */
    public T1 getFirst() {
        return first;
    }

    /**
     * Sets the first element of this pair.
     *
     * @param first the new first element.
     */
    public void setFirst(T1 first) {
        this.first = first;
    }

    /**
     * Gets the second element of this pair.
     *
     * @return the second element
     */
    public T2 getSecond() {
        return second;
    }

    /**
     * Sets the second element of this pair.
     *
     * @param second the new second element.
     */
    public void setSecond(T2 second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<T1, T2> other = (Pair<T1, T2>) obj;
        if (this.first != other.first && (this.first == null || !this.first.equals(other.first))) {
            return false;
        }
        if (this.second != other.second && (this.second == null || !this.second.equals(other.second))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 41 * hash + (this.second != null ? this.second.hashCode() : 0);
        return hash;
    }
}
