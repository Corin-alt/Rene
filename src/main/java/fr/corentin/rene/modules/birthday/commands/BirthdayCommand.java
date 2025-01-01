package fr.corentin.rene.modules.birthday.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class BirthdayCommand extends AInteractionCommand {

    public BirthdayCommand() {
        super("anniversaire", "Ajoute ton anniversaire à la liste", Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        TextInput date = TextInput.create("date", "Date", TextInputStyle.SHORT)
                .setPlaceholder("10/10/2010")
                .setRequired(true)
                .setRequiredRange(10, 10)
                .build();

        Modal modal = Modal.create("birthday_modal", "Anniversaire")
                .addComponents(ActionRow.of(date))
                .build();

        event.replyModal(modal).queue();
        return true;
    }
}
