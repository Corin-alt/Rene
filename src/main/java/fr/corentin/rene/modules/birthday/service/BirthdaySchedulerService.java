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
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BirthdaySchedulerService {
    private static final LocalTime SCHEDULE_TIME = LocalTime.of(10, 10); // 10:10 AM

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

    /**
     * Calculate a person's age based on their birth date
     * @param birthDateMillis The birth date in milliseconds since epoch
     * @return The person's age in years
     */
    private int calculateAge(long birthDateMillis) {
        LocalDate birthDate = Instant.ofEpochMilli(birthDateMillis)
                .atZone(ZoneId.of("Europe/Paris"))
                .toLocalDate();
        LocalDate currentDate = LocalDate.now(ZoneId.of("Europe/Paris"));

        return Period.between(birthDate, currentDate).getYears();
    }

    public void checkAndSendBirthdayMessages() {
        Map<String, Long> userBirthdays = dbManager.getTodayBirthdaysWithDates();
        Rene.getInstance().getLogger().info("Found " + userBirthdays.size() + " birthdays today");

        TextChannel channel = rene.getJda().getTextChannelById(BIRTHDAY_CHANNEL_ID);

        if (channel == null) {
            return;
        }

        if (userBirthdays.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("🥳  Alerte Anniversaire  !");
        embedBuilder.setColor(0xF4900C);
        embedBuilder.setImage("https://i.ibb.co/wNnrn2w/Anniversaire-1-an.jpg");
        embedBuilder.setFooter("Rene | " + now.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .localizedBy(Locale.FRANCE)), rene.getJda().getSelfUser().getAvatarUrl());

        StringBuilder st = new StringBuilder();
        st.append("Aujourd'hui est un jour spécial pour :\n");

        for (Map.Entry<String, Long> entry : userBirthdays.entrySet()) {
            String userId = entry.getKey();
            Long birthDateMillis = entry.getValue();
            int age = calculateAge(birthDateMillis);

            st.append("\n- <@").append(userId).append("> (").append(age).append(" ans)");
        }
        embedBuilder.setDescription(st);

        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}