package fr.corentin.rene.modules.birthday.service;

import fr.corentin.rene.Rene;
import fr.corentin.rene.modules.birthday.BirthdayModuleManager;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BirthdaySchedulerService {
    private static final LocalTime SCHEDULE_TIME = LocalTime.of(10, 10); // 10:00 AM

    private static final String BIRTHDAY_CHANNEL_ID = Channels.TAVERNE.getChannelID();
    private final BirthdayDatabaseService dbManager;
    private final Rene rene;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public BirthdaySchedulerService(BirthdayModuleManager birthdayModuleManager) {
        this.rene = Rene.getInstance();
        this.dbManager = birthdayModuleManager.getBirthdayDatabaseService();

        scheduleDailyTask();
    }

    private void scheduleDailyTask() {
        long initialDelay = calculateInitialDelay();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(this::checkAndSendBirthdayMessages, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private long calculateInitialDelay() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        ZonedDateTime nextRun = now.withHour(SCHEDULE_TIME.getHour()).withMinute(SCHEDULE_TIME.getMinute()).withSecond(0);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(now, nextRun).toMillis();
    }

    public void checkAndSendBirthdayMessages() {
        List<String> userIds = dbManager.getTodayBirthdays();
        Rene.getInstance().getLogger().info(String.valueOf(userIds.size()));

        TextChannel channel = rene.getJda().getTextChannelById(BIRTHDAY_CHANNEL_ID);

        if (channel == null) {
            return;
        }

        if (userIds.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("🥳  Alerte Anniversaire  !");
        embedBuilder.setColor(0xF4900C);
        embedBuilder.setImage("https://i.ibb.co/wNnrn2w/Anniversaire-1-an.jpg");
        embedBuilder.setFooter("Rene | " + now.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).localizedBy(Locale.FRANCE)), rene.getJda().getSelfUser().getAvatarUrl());

        StringBuilder st = new StringBuilder();
        st.append("Aujourd'hui est un jour spécial pour :\n");

        for (String userId : userIds) {
            st.append("\n- <@").append(userId).append(">");
        }
        embedBuilder.setDescription(st);

        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
