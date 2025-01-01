package fr.corentin.rene.modules.suggestion.commands;

import fr.corentin.rene.Rene;
import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import fr.corentin.rene.modules.suggestion.SuggestionModuleManager;
import fr.corentin.rene.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class SuggestionCommand extends AInteractionCommand {

    private final SuggestionModuleManager suggestionModuleManager;

    public SuggestionCommand(SuggestionModuleManager suggestionModuleManager) {
        super("suggestion", "Ajoute une suggestion dans le channel associé", Permission.ALL, List.of(
                new OptionData(OptionType.STRING, "titre", "Titre de la suggestion", true),
                new OptionData(OptionType.STRING, "contenu", "Contenu de la suggestion", true)));
        this.suggestionModuleManager = suggestionModuleManager;
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        String channelId = suggestionModuleManager.getSuggestionChannel(event.getGuild().getId());
        User user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        if (channelId == null) {
            event.getHook().sendMessage("Le channel de suggestion n'as pas été spécifié pour ce serveur, ta suggestion n'as donc pas été postée.").setEphemeral(true).queue();
            Rene.getInstance().getLogger().error("User {} tried to send a new suggestion but channel has not be defined for guildId {}", user.getEffectiveName(), event.getGuild().getId());
            return false;
        }

        TextChannel textChannel = Rene.getInstance().getJda().getTextChannelById(channelId);

        if (textChannel == null) {
            event.getHook().sendMessage("Le channel de suggestion n'as pas été correctement spécifié pour ce serveur, ta suggestion n'as donc pas été postée.").setEphemeral(true).queue();
            Rene.getInstance().getLogger().error("User {} tried to send a new suggestion but channel has not been correctly defined for guildId {}, channel with id {} not present in this guild", user.getEffectiveName(), event.getGuild().getId(), channelId);
            return false;
        }

        String title = event.getOption("titre").getAsString();
        String content = event.getOption("contenu").getAsString();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(content)
                .setColor(0x00AE86)
                .setFooter("Suggestion par " + user.getEffectiveName(), user.getEffectiveAvatarUrl());

        event.getHook().sendMessage("Ta suggestion à bien été envoyé dans le channel <#" + channelId + ">").setEphemeral(true).queue();

        textChannel.sendMessageEmbeds(embed.build()).queue(message -> {
            Emoji[] emojis = EmojiUtils.getYesNoEmojis();
            for (Emoji emoji : emojis) {
                message.addReaction(emoji).queue();
            }
        });

        return true;
    }
}
