package fr.corentin.rene.events;

import fr.corentin.rene.events.parent.AMessageReceivedEventListener;
import fr.corentin.rene.utils.Channels;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;

public class AngryMessageReceivedEventListener extends AMessageReceivedEventListener {

    public AngryMessageReceivedEventListener() {
        super(Collections.singleton(Channels.GRR));
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        event.getMessage().addReaction(Emoji.fromUnicode("U+1F621")).queue();
    }
}

