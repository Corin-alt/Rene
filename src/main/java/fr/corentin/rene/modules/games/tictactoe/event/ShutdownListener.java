package fr.corentin.rene.modules.games.tictactoe.event;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.games.tictactoe.TicTacToeManager;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import org.jetbrains.annotations.NotNull;

public class ShutdownListener extends AEventListener {

    private final TicTacToeManager ticTacToeManager;

    public ShutdownListener(TicTacToeManager ticTacToeManager) {
        this.ticTacToeManager = ticTacToeManager;
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        ticTacToeManager.shutdown();
    }
}
