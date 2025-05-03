package fr.corentin.rene.events.parent;

import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AMessageReceivedEventListener extends AEventListener {

    private final List<Channels> authorizedChannel = new ArrayList<>();

    public AMessageReceivedEventListener(Collection<Channels> channels) {
        this.authorizedChannel.addAll(channels);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!canExecute(event.getChannel().getId())) return;

        execute(event);
    }

    public abstract void execute(MessageReceivedEvent event);

    private boolean canExecute(String id) {
        return this.authorizedChannel.stream().anyMatch(channels -> channels.getChannelID().equals(id));
    }
}
