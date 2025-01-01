package fr.corentin.rene.modules.games.tictactoe.event;

import fr.corentin.rene.events.parent.AEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends AEventListener {

    public ReadyListener() {

    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        jda.getGuilds().forEach(guild ->
                guild.getChannels().stream()
                        .filter(channel -> channel.getName().contains("tic-tac-toe"))
                        .forEach(channel -> channel.delete().queue())
        );
    }
}
