package fr.corentin.rene.modules.games.tictactoe;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Objects;

public class CommandTicTacToe extends AInteractionCommand {

    private final TicTacToeManager ticTacToeManager;

    public CommandTicTacToe(TicTacToeManager ticTacToeManager) {
        super("tic-tac-toe", "Permet de jouer au Morpion avec un autre membre.", Permission.ALL, List.of(
                new OptionData(OptionType.USER, "user", "Avec qui veux-tu jouer ?", true)
        ));
        this.ticTacToeManager = ticTacToeManager;
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Guild guild = event.getGuild();
        User player1 = event.getUser();
        User player2 = Objects.requireNonNull(event.getOption("user")).getAsUser();

        if (player2.isBot()) {
            event.getHook().sendMessage(player1.getAsMention() + ", tu ne peux pas jouer contre un BOT !").queue();
            return false;
        }

        if (player1.getId().equals(player2.getId())) {
            event.getHook().sendMessage(player1.getAsMention() + ", tu ne peux pas jouer contre toi même !").queue();
            return false;
        }

        startNewGame(guild, player1, player2);
        event.getHook().sendMessage(player1.getAsMention() + " Lancement de la partie !").queue();

        return true;
    }

    public void startNewGame(Guild guild, User player1, User player2) {
        TicTacToeInstance gameInstance = new TicTacToeInstance(guild, player1, player2);

        ticTacToeManager.addTicTacToeInstance(gameInstance);
    }
}
