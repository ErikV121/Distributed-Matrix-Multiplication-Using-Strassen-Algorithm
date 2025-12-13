package V2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorkQueue {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Queue<MatrixPair> pendingWork = new ConcurrentLinkedDeque<>();

    private final Queue<long[][]> completedResults = new ConcurrentLinkedDeque<>();

    private final CompletableFuture<long[][]> finalResultFuture = new CompletableFuture<>();

    private int activeJobCount = 0;

    public WorkQueue(ArrayList<long[][]> allMatrices) {
        addPendingWork(allMatrices);
    }

    private synchronized void addPendingWork(ArrayList<long[][]> matrices) {
        for (int i = 0; i < matrices.size(); i += 2) {
            if (i + 1 < matrices.size()) {
                MatrixPair pair = new MatrixPair(matrices.get(i), matrices.get(i + 1));
                pendingWork.add(pair);
            } else {
                completedResults.add(matrices.get(i));
            }
        }
    }

    public synchronized MatrixPair poll() {
        MatrixPair pair = pendingWork.poll();
        if (pair != null) {
            activeJobCount++;
        }
        return pair;
    }

    public boolean hasWork() {
        return !pendingWork.isEmpty();
    }

    public synchronized void addResult(int workId, long[][] result) {
        activeJobCount--;
        completedResults.add(result);

//TODO disables printing matrices and causes massive latency
//        for (MatrixPair q : pendingWork) {
//            StrassenAlgorithmUtil.printMatrix(q.matrixA());
//            StrassenAlgorithmUtil.printMatrix(q.matrixB());
//        }

        if (completedResults.size() >= 2) {
            long[][] m1 = completedResults.poll();
            long[][] m2 = completedResults.poll();

            pendingWork.add(new MatrixPair(m1, m2));
            LOGGER.info("WorkQueue: Created new Round 2 job from results.");
        }

        if (pendingWork.isEmpty() && activeJobCount == 0 && completedResults.size() == 1) {
            LOGGER.info("WorkQueue: all completed");
            finalResultFuture.complete(completedResults.peek());
        }
    }

    public CompletableFuture<long[][]> getFinalResultFuture() {
        return finalResultFuture;
    }
}