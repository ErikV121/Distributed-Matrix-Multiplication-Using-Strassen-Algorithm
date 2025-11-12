package V2;

import V2.util.Opcode;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Strategy {
    private final Opcode strategyType;

    public Strategy(Opcode strategyType) {
        this.strategyType = strategyType;
    }

    public void singleWorkerStrategy() {

    }

    public Opcode getStrategyType() {
        return strategyType;
    }
}
