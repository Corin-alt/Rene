package fr.corentin.rene.modules.games.tictactoe.event;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.games.tictactoe.TicTacToeInstance;
import fr.corentin.rene.modules.games.tictactoe.TicTacToeManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TicTacToeButtonListener extends AEventListener {

    private final TicTacToeManager ticTacToeManager;

    public TicTacToeButtonListener(TicTacToeManager ticTacToeManager) {
        this.ticTacToeManager = ticTacToeManager;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();

        User user = event.getUser();
        Button button = event.getButton();
        Channel channel = event.getChannel();

        if (!channel.getName().contains("tic-tac-toe")) {
            return;
        }

        TicTacToeInstance ticTacToeInstance = ticTacToeManager.getTicTacToeInstances().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(channel.getId()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (ticTacToeInstance == null) {
            return;
        }

        if (!user.getId().equals(ticTacToeInstance.getCurrentPlayer().getId())) {
            return;
        }

        ticTacToeInstance.editAndUpdateGameButtons(button);
    }
}
