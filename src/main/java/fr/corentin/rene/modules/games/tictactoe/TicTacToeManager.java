package fr.corentin.rene.modules.games.tictactoe;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.games.parent.AGameManager;
import fr.corentin.rene.modules.games.tictactoe.event.ReadyListener;
import fr.corentin.rene.modules.games.tictactoe.event.ShutdownListener;
import fr.corentin.rene.modules.games.tictactoe.event.TicTacToeButtonListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeManager extends AGameManager {
    private final Map<String, TicTacToeInstance> ticTacToeInstances;
    private final ExecutorService gamesExecutor;

    public TicTacToeManager() {
        this.ticTacToeInstances = new HashMap<>();
        this.gamesExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public List<AEventListener> registerListeners() {
        return Arrays.asList(
                new TicTacToeButtonListener(this),
                new ShutdownListener(this),
                new ReadyListener()
        );
    }

    public void addTicTacToeInstance(TicTacToeInstance ticTacToeInstance) {
        ticTacToeInstance.initialisation();
        ticTacToeInstances.put(ticTacToeInstance.getChannel().getId(), ticTacToeInstance);

        gamesExecutor.submit(ticTacToeInstance::startGame);
    }

    public void shutdown() {
        ticTacToeInstances.forEach((s, ticTacToeInstance) -> ticTacToeInstance.stopGame());
        gamesExecutor.shutdown();
    }

    public ExecutorService getGamesExecutor() {
        return gamesExecutor;
    }

    public Map<String, TicTacToeInstance> getTicTacToeInstances() {
        return ticTacToeInstances;
    }
}
