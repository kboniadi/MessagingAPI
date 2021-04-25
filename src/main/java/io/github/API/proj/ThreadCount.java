package io.github.API.proj;

public enum ThreadCount {
    ONE(1),
    TWO(2),
    FOUR(4),
    SIX(6),
    EIGHT(8),
    T_16(16),
    T_32(32),
    T_64(64),
    SYS_DEP(Runtime.getRuntime().availableProcessors()),
    NO_CAP(Integer.MAX_VALUE);

    private final int size;

    /**
     * Constructor
     * @param size value of specific enum
     * @author Kord Boniadi
     */
    ThreadCount(int size) {
        this.size = size;
    }

    /**
     * Gets the actual value of the enum
     * @return (int) value
     */
    public int toInt() {
        return this.size;
    }

    /**
     * String representation of value of the enum.
     * @return (String) representation
     * @author Kord Boniadi
     */
    @Override
    public String toString() {
        return String.valueOf(this.size);
    }
}
