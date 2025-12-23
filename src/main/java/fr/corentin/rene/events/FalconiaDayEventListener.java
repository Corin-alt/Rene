package fr.corentin.rene.events;

import fr.corentin.rene.Rene;
import fr.corentin.rene.events.parent.AMessageReceivedEventListener;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FalconiaDayEventListener extends AMessageReceivedEventListener {

    private static final String EMOJI_MSG_CLASSIC = Emoji.fromUnicode("U+1F973").getFormatted();
    public static final String MESSAGE_CLASSIC
            = EMOJI_MSG_CLASSIC + " Aujourd'hui c'est **Falconia Day** ! " +
            "Joyeux Falconia Day à tous ! " + EMOJI_MSG_CLASSIC;


    private static final String EMOJI_MSG_VARIANT = Emoji.fromUnicode("U+1F621").getFormatted();
    public static final String MESSAGE_VARIANT
            = EMOJI_MSG_VARIANT + " Aujourd'hui ce n'est **PAS Falconia Day** ! " +
            "Pas de joyeux **Falconia Day** à qui que ce soit ! " + EMOJI_MSG_VARIANT;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public FalconiaDayEventListener() {
        super(Collections.singleton(Channels.TAVERNE));
        TextChannel channel = Rene.getInstance().getJda()
                .getTextChannelById(Channels.TAVERNE.getChannelID());

        assert channel != null;

        channel.sendMessage(getMessage()).queue();
        scheduleNextMessage(channel);
    }

    @Override
    public void execute(MessageReceivedEvent event) {}

    private void scheduleNextMessage(TextChannel channel) {
        LocalDateTime triggerTime = generateNextFalconiaDay();
        long delay = Duration.between(LocalDateTime.now(), triggerTime).toMillis();

        System.out.println("[INFO] Next Falconia Day scheduled for : " + triggerTime.format(formatter));

        scheduler.schedule(() -> {
            String message = getMessage();
            System.out.println("[INFO] Sending message: " + (message.contains("PAS") ? "PAS Falconia Day"
                    : "Falconia Day"));
            channel.sendMessage(message).queue();
            scheduleNextMessage(channel);
        }, delay, TimeUnit.MILLISECONDS);
    }

    private LocalDateTime generateNextFalconiaDay() {
        int daysToAdd = random.nextInt(5);
        int hour = 6 + random.nextInt(8);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);

        LocalDate date = LocalDate.now().plusDays(daysToAdd);
        return LocalDateTime.of(date, LocalTime.of(hour, minute, second));
    }

    private String getMessage(){
        int rand = new Random().nextInt(10) + 1;
        return rand != 1 ? MESSAGE_CLASSIC : MESSAGE_VARIANT;
    }
}