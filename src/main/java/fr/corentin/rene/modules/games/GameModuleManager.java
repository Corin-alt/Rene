package fr.corentin.rene.modules.games;

import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.games.tictactoe.CommandTicTacToe;
import fr.corentin.rene.modules.games.tictactoe.TicTacToeManager;

public class GameModuleManager extends AModule {

    private final TicTacToeManager ticTacToeManager;

    public GameModuleManager() {
        ticTacToeManager = new TicTacToeManager();
    }

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerSlashCommand(new CommandTicTacToe(ticTacToeManager));
    }

    @Override
    public void registerListeners() {
        EventManager.getInstance().registerListeners("GameModule", ticTacToeManager.registerListeners());
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return GameModuleConfiguration.class;
    }
}