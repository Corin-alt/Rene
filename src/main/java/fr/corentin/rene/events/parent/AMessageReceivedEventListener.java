package fr.corentin.rene.events.parent;

import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AMessageReceivedEventListener extends AEventListener {

    private final Set<String> authorizedChannelIds;

    public AMessageReceivedEventListener(Collection<Channels> channels) {
        this.authorizedChannelIds = new HashSet<>();
        for (Channels channel : channels) {
            authorizedChannelIds.add(channel.getChannelID());
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!authorizedChannelIds.contains(event.getChannel().getId())) return;

        execute(event);
    }

    public abstract void execute(MessageReceivedEvent event);
}
