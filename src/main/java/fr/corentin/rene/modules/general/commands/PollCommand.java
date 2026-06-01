package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Locale;

import static fr.corentin.rene.utils.EmojiUtils.getEmojiForOption;
import static fr.corentin.rene.utils.EmojiUtils.getYesNoEmojis;

public class PollCommand extends AInteractionCommand {
    private static final String OPTIONS = "options";

    public PollCommand() {
        super("sondage", "Créer un nouveau sondage", Permission.ALL, Arrays.asList(
                new OptionData(OptionType.STRING, "question", "La question du sondage", true),
                new OptionData(OptionType.STRING, OPTIONS, "Réponses possibles au sondage, séparées par des virgules", false)
        ));
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        String question = event.getOption("question").getAsString();
        String optionsStr = event.getOption(OPTIONS) != null ? event.getOption(OPTIONS).getAsString() : null;

        String[] options = (optionsStr != null && !optionsStr.trim().isEmpty())
                ? optionsStr.split(",") : null;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(question);
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setFooter("Sondage crée par " + event.getUser().getEffectiveName() +
                        " | " +
                        event.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).localizedBy(Locale.FRANCE)),
                event.getUser().getEffectiveAvatarUrl());

        StringBuilder optionsDescription = new StringBuilder();

        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                Emoji emoji = getEmojiForOption(i);
                optionsDescription.append(emoji.getFormatted()).append(" ").append(options[i].trim()).append("\n\n");
            }
        } else {
            Emoji[] emojis = getYesNoEmojis();
            optionsDescription.append(emojis[0].getFormatted()).append(" Oui\n\n").append(emojis[1].getFormatted()).append(" Non");
        }

        embedBuilder.setDescription(optionsDescription.toString());

        event.replyEmbeds(embedBuilder.build()).queue(hook -> hook.retrieveOriginal().queue(message -> {
            if (options != null) {
                for (int i = 0; i < options.length; i++) {
                    message.addReaction(getEmojiForOption(i)).queue();
                }
            } else {
                Emoji[] emojis = getYesNoEmojis();
                message.addReaction(emojis[0]).queue();
                message.addReaction(emojis[1]).queue();
            }
        }));
        return true;
    }
}
