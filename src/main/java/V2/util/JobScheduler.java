package V2.util;

public class JobScheduler {
    private static final int SMALL_SIZE = 50;
    private static final int SMALL_AMOUNT = 4;
    private static final int MEDIUM_SIZE = 500;
    private static final int MEDIUM_AMOUNT = 10;

    public static Opcode getStrategy(int size, int amount) {
        if (size <= SMALL_SIZE && amount <= SMALL_AMOUNT) {
            return Opcode.SINGLE_WORKER;
        } else if (size <= MEDIUM_SIZE && amount <= MEDIUM_AMOUNT) {
            return Opcode.JOB_LEADER;
        } else {
            return Opcode.IDK_WHAT_TO_CALL_THIS;
        }
    }
}
