package fr.corentin.rene.events;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.corentin.rene.Rene;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

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

    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String CONFIG_FILENAME = "holidays.json";

    public HolidayScheduler() {
        TextChannel channel = Rene.getInstance().getJda()
                .getTextChannelById(Channels.TAVERNE.getChannelID());

        if (channel == null) {
            System.out.println("[WARN] Could not find TAVERNE channel for holiday scheduler");
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
                System.out.println("[INFO] Today is a holiday, sending now: " + holiday.date);
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
                System.out.println("[INFO] Sending holiday message for " + holiday.date);
                channel.sendMessage(message).queue();
            }, delay, TimeUnit.MILLISECONDS);

            System.out.println("[INFO] Holiday scheduled for " + triggerTime.format(formatter) + " : " + holiday.date);
            scheduled++;
        }

        System.out.println("[INFO] " + scheduled + " holiday messages scheduled");
    }

    private List<Holiday> loadHolidays() {
        List<Holiday> holidays = new ArrayList<>();
        File configFile = new File(Rene.getInstance().getDataFolder(), CONFIG_FILENAME);

        if (!configFile.exists()) {
            System.out.println("[WARN] Holiday config file not found: " + configFile.getPath());
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

            System.out.println("[INFO] Loaded " + holidays.size() + " holidays from config");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to load holiday config: " + e.getMessage());
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
