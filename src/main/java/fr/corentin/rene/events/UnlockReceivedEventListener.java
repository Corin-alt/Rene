package fr.corentin.rene.events;

import fr.corentin.rene.events.parent.AMessageReceivedEventListener;
import fr.corentin.rene.utils.Channels;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;

public class UnlockReceivedEventListener extends AMessageReceivedEventListener {

    private final String unlock;

    public UnlockReceivedEventListener() {
        super(Collections.singleton(Channels.TAVERNE));
        Dotenv dotenv = Dotenv.load();
        this.unlock = dotenv.get("UNLOCK").toLowerCase();
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().getContentRaw().toLowerCase().contains(this.unlock)) return;

        event.getChannel().sendMessage("Félicitations ! Vous avez rétabli la stabilité du serveur.").queue();
    }
}

