package V2.util;


public enum Opcode {
    // Strategy types
    SINGLE_WORKER(1),
    JOB_LEADER(2),
    IDK_WHAT_TO_CALL_THIS(3),

    // Client types
    PRIMARY(4),
    SECONDARY(5),

    // Responses
    RESULT(6),
    ERROR(7);

    private final int code;

    Opcode(int code) {
        this.code = code;
    }

    public static Opcode fromCode(int code) {
        for (Opcode op : values()) {
            if (op.getCode() == code) return op;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

}
