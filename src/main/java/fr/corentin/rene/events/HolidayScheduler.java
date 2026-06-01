package fr.corentin.rene.events;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.corentin.rene.Rene;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HolidayScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HolidayScheduler.class);
    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String CONFIG_FILENAME = "holidays.json";

    public HolidayScheduler() {
        TextChannel channel = Rene.getInstance().getJda()
                .getTextChannelById(Channels.TAVERNE.getChannelID());

        if (channel == null) {
            logger.warn("Could not find TAVERNE channel for holiday scheduler");
            return;
        }

        List<Holiday> holidays = loadHolidays();
        ZonedDateTime now = ZonedDateTime.now(ZONE_PARIS);
        int scheduled = 0;

        LocalDate today = now.toLocalDate();

        for (Holiday holiday : holidays) {
            if (holiday.date.isBefore(today)) {
                continue;
            }

            if (holiday.date.isEqual(today)) {
                logger.info("Today is a holiday, sending now: {}", holiday.date);
                channel.sendMessage(holiday.message).queue();
                scheduled++;
                continue;
            }

            int hour = 6 + random.nextInt(3);
            int minute = random.nextInt(60);
            int second = random.nextInt(60);

            ZonedDateTime triggerTime = ZonedDateTime.of(holiday.date, LocalTime.of(hour, minute, second), ZONE_PARIS);
            long delay = Duration.between(now, triggerTime).toMillis();
            String message = holiday.message;

            scheduler.schedule(() -> {
                logger.info("Sending holiday message for {}", holiday.date);
                channel.sendMessage(message).queue();
            }, delay, TimeUnit.MILLISECONDS);

            logger.info("Holiday scheduled for {} : {}", triggerTime.format(formatter), holiday.date);
            scheduled++;
        }

        logger.info("{} holiday messages scheduled", scheduled);
    }

    private List<Holiday> loadHolidays() {
        List<Holiday> holidays = new ArrayList<>();
        File configFile = new File(Rene.getInstance().getDataFolder(), CONFIG_FILENAME);

        if (!configFile.exists()) {
            logger.warn("Holiday config file not found: {}", configFile.getPath());
            return holidays;
        }

        try (Reader reader = Files.newBufferedReader(configFile.toPath())) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            JsonArray array = root.getAsJsonArray("holidays");

            if (array == null) return holidays;

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                String dateStr = obj.get("date").getAsString();
                String message = obj.get("message").getAsString();
                LocalDate date = LocalDate.parse(dateStr);
                holidays.add(new Holiday(date, message));
            }

            logger.info("Loaded {} holidays from config", holidays.size());
        } catch (Exception e) {
            logger.error("Failed to load holiday config", e);
        }

        return holidays;
    }

    private static class Holiday {
        final LocalDate date;
        final String message;

        Holiday(LocalDate date, String message) {
            this.date = date;
            this.message = message;
        }
    }
}
