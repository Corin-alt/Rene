package fr.corentin.rene.events;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.corentin.rene.Rene;
import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EightBallMessageListener extends AEventListener {

    private final Rene rene;
    private final CommandManager commandManager;
    private static final List<String> possibleAnswers = new ArrayList<>();
    private static final Random random = new Random();

    static {
        try {
            String filePath = Paths.get("files", "eight.json").toString();
            Gson gson = new Gson();
            Reader reader = new FileReader(filePath);
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray jsonArray = jsonObject.get("possible_answers").getAsJsonArray();

            jsonArray.forEach(element -> possibleAnswers.add(element.getAsString()));

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EightBallMessageListener() {
        this.commandManager = CommandManager.getInstance();
        this.rene = Rene.getInstance();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw();

        if (!content.contains(":8ball:") && !content.contains(Emoji.fromUnicode("U+1F3B1").getFormatted())) return;

        if (!event.getChannel().getId().equals(Channels.TCHAT_WITH_RENE.getChannelID())) return;

        String randomAnswer = possibleAnswers.get(random.nextInt(possibleAnswers.size()));

        event.getChannel().sendMessage(randomAnswer).queue();
    }
}