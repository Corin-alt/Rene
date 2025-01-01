package fr.corentin.rene.modules.games.tictactoe;

import fr.corentin.rene.utils.UserRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicTacToeInstance {
    private static final int GRID_SIZE = 3;
    private static final int GAME_DURATION_MINUTES = 3;

    private final ScheduledExecutorService gameTimer = Executors.newScheduledThreadPool(1);
    private final Guild guild;
    private final User player1;
    private final User player2;
    private User currentPlayer;

    private Role createRoleForGame;
    private final Map<User, PlayerSymbol> playerSymbols = new HashMap<>(2);
    private final String[][] grid = new String[GRID_SIZE][GRID_SIZE];
    private TextChannel channel;
    private Message gameMessage;
    private Message turnMessage;

    public TicTacToeInstance(Guild guild, User player1, User player2) {
        this.guild = guild;
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
        this.playerSymbols.put(player1, PlayerSymbol.X);
        this.playerSymbols.put(player2, PlayerSymbol.O);
    }

    public void initialisation() {
        String gameAndRoleName = "tic-tac-toe-" + this.player1.getName() + "-" + this.player2.getName();

        this.channel = guild.createTextChannel(gameAndRoleName).complete();

        this.createRoleForGame = guild.createRole().setName(gameAndRoleName).complete();

        EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_ADD_REACTION);
        EnumSet<Permission> deny = EnumSet.complementOf(allow);

        this.channel.upsertPermissionOverride(createRoleForGame).setAllowed(allow).setDenied(deny).complete();

        this.guild.addRoleToMember(this.player1, this.createRoleForGame).complete();
        this.guild.addRoleToMember(this.player2, this.createRoleForGame).complete();

        Role staff = this.guild.getRoleById(UserRole.STAFF.getId());

        EnumSet<Permission> allowForStaff = EnumSet.of(Permission.VIEW_CHANNEL);
        EnumSet<Permission> denyForStaff = EnumSet.complementOf(allow);
        assert staff != null;

        this.channel.upsertPermissionOverride(staff).setAllowed(allowForStaff).setDenied(denyForStaff).complete();

        initGrid();
    }

    private void initGrid() {
        String message = String.format("- Une partie de morpion entre %s (❌) et %s (⭕) vient de commencer.\n" +
                        "- :information_source: Pour information la partie dure %d minutes," +
                        " si au bout de ce temps personne n'a gagné ou que vous êtes AFK celle-ci sera automatiquement arrêtée.",
                this.player1.getAsMention(), this.player2.getAsMention(), GAME_DURATION_MINUTES);
        this.channel.sendMessage(message).queue();

        List<ActionRow> actionRows = new ArrayList<>();
        List<ButtonImpl> buttons = new ArrayList<>();

        for (String[] row : this.grid) {
            Arrays.fill(row, " ");
        }

        for (int i = 1; i < 10; i++) {
            buttons.add(new ButtonImpl(this.channel.getId() + "_" + i, "", ButtonStyle.SECONDARY, false, Emoji.fromFormatted("U+2754")));
        }

        actionRows.add(ActionRow.of(buttons.subList(0, 3)));
        actionRows.add(ActionRow.of(buttons.subList(3, 6)));
        actionRows.add(ActionRow.of(buttons.subList(6, 9)));

        this.channel.sendMessageComponents(actionRows).queue(message2 -> this.gameMessage = message2);

        this.channel.sendMessage("C'est au tour de **" + currentPlayer.getAsMention() + "** de jouer !").queue(message3 -> this.turnMessage = message3);
    }

    public void startGame() {
        this.gameTimer.schedule(this::endGameIfNotWon, GAME_DURATION_MINUTES, TimeUnit.MINUTES);
    }

    public void editAndUpdateGameButtons(Button button) {
        PlayerSymbol playerSymbol = playerSymbols.get(currentPlayer);

        List<ActionRow> modifiedComponents = gameMessage.getActionRows().stream().map(actionRow ->
                ActionRow.of(actionRow.getComponents().stream().map(component -> {
                    if (component instanceof Button buttonToUpdate && buttonToUpdate.getId().equals(button.getId())) {
                        return (buttonToUpdate).withDisabled(true).withEmoji(playerSymbol.toEmoji());
                    }
                    return component;
                }).toList())
        ).toList();

        this.gameMessage.editMessageComponents(modifiedComponents).queue(updatedMessage -> {
            this.gameMessage = updatedMessage;
            updateGridState(button);
            computeGameState();
        });
    }

    private void updateGridState(Button button) {
        int buttonId = Integer.parseInt(button.getId().split("_")[1]);

        int row = (buttonId - 1) / GRID_SIZE;
        int col = (buttonId - 1) % GRID_SIZE;

        this.grid[row][col] = playerSymbols.get(currentPlayer).getTextEmoji();
    }

    public void computeGameState() {
        if (checkWin()) {
            this.channel.sendMessageFormat("Félicitation %s, tu as gagné la partie !", currentPlayer.getAsMention())
                    .queue(message -> gameTimer.schedule(this::stopGame, 20, TimeUnit.SECONDS));
        } else {
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
            sendTurnMessage(currentPlayer);
        }
    }

    public void sendTurnMessage(User user) {
        this.turnMessage.editMessage("C'est au tour de **" + user.getAsMention() + "** de jouer !").queue();
    }

    private void endGameIfNotWon() {
        this.channel.sendMessage("Le temps est écoulé ! La partie est terminée sans vainqueur.").queue();
        this.stopGame();
    }

    public void stopGame() {
        if (this.channel != null) {
            this.channel.delete().queueAfter(20, TimeUnit.SECONDS);
        }

        this.guild.removeRoleFromMember(this.player1, this.createRoleForGame).complete();
        this.guild.removeRoleFromMember(this.player2, this.createRoleForGame).complete();

        this.gameTimer.shutdown();
    }

    public boolean checkWin() {
        String symbolTextEmoji = playerSymbols.get(currentPlayer).getTextEmoji();
        if (symbolTextEmoji == null || symbolTextEmoji.isEmpty()) {
            return false;
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            if (!this.grid[i][0].isEmpty() && this.grid[i][0].equals(this.grid[i][1]) && this.grid[i][1].equals(this.grid[i][2]) && this.grid[i][0].equals(symbolTextEmoji)) {
                return true;
            }

            if (!this.grid[0][i].isEmpty() && this.grid[0][i].equals(this.grid[1][i]) && this.grid[1][i].equals(this.grid[2][i]) && this.grid[0][i].equals(symbolTextEmoji)) {
                return true;
            }
        }

        if (!this.grid[0][0].isEmpty() && this.grid[0][0].equals(this.grid[1][1]) &&
                this.grid[1][1].equals(this.grid[2][2]) && this.grid[0][0].equals(symbolTextEmoji)) {
            return true;
        }

        return !this.grid[0][2].isEmpty() && this.grid[0][2].equals(this.grid[1][1]) &&
                this.grid[1][1].equals(this.grid[2][0]) && this.grid[0][2].equals(symbolTextEmoji);
    }

    public User getCurrentPlayer() {
        return currentPlayer;
    }

    public TextChannel getChannel() {
        return channel;
    }
}
