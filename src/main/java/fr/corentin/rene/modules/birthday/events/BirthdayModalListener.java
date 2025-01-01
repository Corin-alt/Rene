package fr.corentin.rene.modules.birthday.events;

import fr.corentin.rene.Rene;
import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.birthday.service.BirthdayDatabaseService;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BirthdayModalListener extends AEventListener {

    private final BirthdayDatabaseService birthdayDatabaseService;

    public BirthdayModalListener(BirthdayDatabaseService birthdayDatabaseService) {
        this.birthdayDatabaseService = birthdayDatabaseService;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("birthday_modal")) {
            String userId = event.getUser().getId();
            String dateString = event.getValue("date").getAsString();

            Date date;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                Rene.getInstance().getLogger().warn("Failed to parse birthday date : {}", e.getMessage());
                event.reply("Ton anniversaire n'a pas été enregistré, fournie une date correcte !").setEphemeral(true).queue();
                return;
            }

            birthdayDatabaseService.insertBirthday(userId, date.getTime());

            event.reply("Ton anniversaire a été enregistré !").setEphemeral(true).queue();
        }
    }
}
