package fr.corentin.rene.modules.moderation.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GiveawayCommand extends AInteractionCommand {
    public static final String OPTIONS = "options";

    public GiveawayCommand() {
        super("giveaway", "Tire une personne au hasard parmi les réactions", Permission.ADMIN, List.of(
                new OptionData(OptionType.STRING, "messageid", "L'ID du message", true),
                new OptionData(OptionType.STRING, "emoji", "L'emoji", true)
        ));
        System.out.println("-");
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        event.deferReply().queue();

        String messageID = Objects.requireNonNull(event.getOption("messageid")).getAsString();
        String emojiStr = Objects.requireNonNull(event.getOption("emoji")).getAsString();

        MessageChannel channel = event.getMessageChannel();

        Emoji emoji = parseEmoji(emojiStr);

        channel.retrieveReactionUsersById(messageID, emoji).queue(users -> {
            List<User> validUsers = users.stream()
                    .filter(user -> !user.isBot())
                    .toList();

            if (validUsers.isEmpty()) {
                event.getHook().editOriginal("❌ No users have reacted with this emoji.").queue();
                return;
            }

            User winner = validUsers.get(new Random().nextInt(validUsers.size()));

            channel.retrieveMessageById(messageID).queue(message -> {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🎉 Giveaway - Résultat")
                        .setDescription("**Gagnant :** " + winner.getAsMention())
                        .addField("Participants", String.valueOf(validUsers.size()), true)
                        .setColor(Color.GREEN)
                        .setThumbnail(winner.getEffectiveAvatarUrl());

                event.getHook().editOriginalEmbeds(embed.build()).queue();
            });
        }, error -> {
            event.getHook().editOriginal("❌ Error: " +
                    "Unable to retrieve reactions. Please check the message ID and emoji.").queue();
        });

        return true;
    }


    private Emoji parseEmoji(String emojiStr) {
        if (emojiStr.matches("<a?:\\w+:\\d+>")) {
            String[] parts = emojiStr.replaceAll("[<>]", "").split(":");
            boolean animated = parts[0].equals("a");
            String name = parts[1];
            long id = Long.parseLong(parts[2]);
            return Emoji.fromCustom(name, id, animated);
        }
        return Emoji.fromUnicode(emojiStr);
    }
}