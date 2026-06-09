package fr.corentin.rene.modules.games;

import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.games.mastermind.MastermindCommand;
import fr.corentin.rene.modules.games.mastermind.MastermindDatabaseService;
import fr.corentin.rene.modules.games.mastermind.MastermindGameManager;
import fr.corentin.rene.modules.games.mastermind.MastermindInteractionListener;
import fr.corentin.rene.modules.games.sudoku.SudokuCommand;
import fr.corentin.rene.modules.games.sudoku.SudokuDatabaseService;
import fr.corentin.rene.modules.games.sudoku.SudokuGameManager;
import fr.corentin.rene.modules.games.sudoku.SudokuInteractionListener;
import fr.corentin.rene.modules.games.sutom.SutomCommand;
import fr.corentin.rene.modules.games.sutom.SutomDatabaseService;
import fr.corentin.rene.modules.games.sutom.SutomGameManager;
import fr.corentin.rene.modules.games.sutom.SutomInteractionListener;

public class GameModuleManager extends AModule {
    private final SudokuGameManager sudokuGameManager;
    private final SudokuDatabaseService sudokuDatabaseService;
    private final MastermindGameManager mastermindGameManager;
    private final MastermindDatabaseService mastermindDatabaseService;
    private final SutomGameManager sutomGameManager;
    private final SutomDatabaseService sutomDatabaseService;

    public GameModuleManager() {
        this.sudokuGameManager = new SudokuGameManager();
        this.sudokuDatabaseService = new SudokuDatabaseService();
        this.mastermindGameManager = new MastermindGameManager();
        this.mastermindDatabaseService = new MastermindDatabaseService();
        this.sutomGameManager = new SutomGameManager();
        this.sutomDatabaseService = new SutomDatabaseService();
    }

    @Override
    public void registerCommands() {
        //CommandManager.getInstance().registerSlashCommand(new SudokuCommand(sudokuGameManager));
        //CommandManager.getInstance().registerSlashCommand(new MastermindCommand(mastermindGameManager, mastermindDatabaseService));
        // CommandManager.getInstance().registerSlashCommand(new SutomCommand(sutomGameManager, sutomDatabaseService));
        //CommandManager.getInstance().registerSlashCommand(new ClassementCommand());
    }

    @Override
    public void registerListeners() {
        EventManager.getInstance().registerListener("GameModule",
                new SudokuInteractionListener(sudokuGameManager, sudokuDatabaseService));
        EventManager.getInstance().registerListener("GameModule",
                new MastermindInteractionListener(mastermindGameManager, mastermindDatabaseService));
        EventManager.getInstance().registerListener("GameModule",
                new SutomInteractionListener(sutomGameManager, sutomDatabaseService));
        EventManager.getInstance().registerListener("GameModule",
                new ClassementInteractionListener(sudokuDatabaseService, mastermindDatabaseService, sutomDatabaseService));
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return GameModuleConfiguration.class;
    }
}
