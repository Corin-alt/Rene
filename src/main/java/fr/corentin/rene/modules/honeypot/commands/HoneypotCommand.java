package fr.corentin.rene.modules.honeypot.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import fr.corentin.rene.modules.honeypot.service.HoneypotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HoneypotCommand extends AInteractionCommand {
    private final HoneypotService honeypotService;

    public HoneypotCommand(HoneypotService honeypotService) {
        super("honeypot", "Gère le salon piège anti-comptes piratés", Permission.ADMIN, null);
        this.honeypotService = honeypotService;
    }

    @Override
    public List<SubcommandData> getSubcommands() {
        return List.of(
                new SubcommandData("salon", "Définit le salon piège")
                        .addOptions(new OptionData(OptionType.CHANNEL, "salon", "Le salon piège", true)
                                .setChannelTypes(ChannelType.TEXT)),
                new SubcommandData("logs", "Définit le salon de logs")
                        .addOptions(new OptionData(OptionType.CHANNEL, "salon", "Le salon de logs", true)
                                .setChannelTypes(ChannelType.TEXT)),
                new SubcommandData("exempter-ajouter", "Exempte un rôle du piège")
                        .addOptions(new OptionData(OptionType.ROLE, "role", "Le rôle à exempter", true)),
                new SubcommandData("exempter-retirer", "Retire l'exemption d'un rôle")
                        .addOptions(new OptionData(OptionType.ROLE, "role", "Le rôle à ne plus exempter", true)),
                new SubcommandData("statut", "Affiche la configuration actuelle"),
                new SubcommandData("activer", "Réactive le salon piège"),
                new SubcommandData("desactiver", "Désactive le salon piège sans effacer la configuration")
        );
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        String subcommand = event.getSubcommandName();
        if (guild == null || subcommand == null) {
            event.reply("Cette commande s'utilise sur le serveur.").setEphemeral(true).queue();
            return false;
        }

        switch (subcommand) {
            case "salon" -> setTrapChannel(event);
            case "logs" -> setLogChannel(event);
            case "exempter-ajouter" -> addExemptRole(event);
            case "exempter-retirer" -> removeExemptRole(event);
            case "statut" -> showStatus(event, guild);
            case "activer" -> setEnabled(event, true);
            case "desactiver" -> setEnabled(event, false);
            default -> {
                event.reply("Sous-commande inconnue.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private void setTrapChannel(GenericCommandInteractionEvent event) {
        TextChannel channel = Objects.requireNonNull(event.getOption("salon")).getAsChannel().asTextChannel();
        event.deferReply(true).queue();

        honeypotService.setTrapChannel(channel);
        honeypotService.syncEmbed(error -> {
            if (error == null) {
                event.getHook().editOriginal("Le salon piège est maintenant " + channel.getAsMention() + ".").queue();
            } else {
                event.getHook().editOriginal("Le salon piège est enregistré (" + channel.getAsMention()
                        + ") mais je n'ai pas pu y poster l'avertissement : " + error.getMessage()).queue();
            }
        });
    }

    private void setLogChannel(GenericCommandInteractionEvent event) {
        TextChannel channel = Objects.requireNonNull(event.getOption("salon")).getAsChannel().asTextChannel();

        honeypotService.setLogChannel(channel);
        event.reply("Le salon de logs est maintenant " + channel.getAsMention() + ".").setEphemeral(true).queue();
    }

    private void addExemptRole(GenericCommandInteractionEvent event) {
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();
        if (role.isPublicRole()) {
            event.reply("Impossible d'exempter @everyone : plus personne ne serait piégé.").setEphemeral(true).queue();
            return;
        }

        if (honeypotService.addExemptRole(role)) {
            event.reply("Le rôle " + role.getAsMention() + " est maintenant exempté.").setEphemeral(true).queue();
        } else {
            event.reply("Le rôle " + role.getAsMention() + " est déjà exempté.").setEphemeral(true).queue();
        }
    }

    private void removeExemptRole(GenericCommandInteractionEvent event) {
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

        if (honeypotService.removeExemptRole(role)) {
            event.reply("Le rôle " + role.getAsMention() + " n'est plus exempté.").setEphemeral(true).queue();
        } else {
            event.reply("Le rôle " + role.getAsMention() + " n'était pas exempté.").setEphemeral(true).queue();
        }
    }

    private void showStatus(GenericCommandInteractionEvent event, Guild guild) {
        String roles = honeypotService.getExemptRoleIds().stream()
                .map(id -> guild.getRoleById(id) != null ? "<@&" + id + ">" : "Rôle supprimé (`" + id + "`)")
                .collect(Collectors.joining("\n"));

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(honeypotService.isEnabled() ? Color.GREEN : Color.GRAY)
                .setTitle("Honeypot : configuration")
                .addField("État", honeypotService.isEnabled() ? "Activé" : "Désactivé", true)
                .addField("Bans", String.valueOf(honeypotService.getBanCount()), true)
                .addField("Salon piège", channelLabel(guild, honeypotService.getTrapChannelId()), false)
                .addField("Salon de logs", channelLabel(guild, honeypotService.getLogChannelId()), false)
                .addField("Rôles exemptés", roles.isEmpty() ? "Aucun" : roles, false);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private String channelLabel(Guild guild, long channelId) {
        if (channelId == 0) {
            return "Non défini";
        }
        return guild.getTextChannelById(channelId) != null ? "<#" + channelId + ">" : "Salon supprimé (`" + channelId + "`)";
    }

    private void setEnabled(GenericCommandInteractionEvent event, boolean enabled) {
        if (honeypotService.isEnabled() == enabled) {
            event.reply(enabled ? "Le salon piège est déjà activé." : "Le salon piège est déjà désactivé.").setEphemeral(true).queue();
            return;
        }

        honeypotService.setEnabled(enabled);
        if (!enabled) {
            event.reply("Le salon piège est désactivé. La configuration est conservée.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        honeypotService.syncEmbed(error -> event.getHook().editOriginal(error == null
                ? "Le salon piège est réactivé."
                : "Le salon piège est réactivé, mais l'embed d'avertissement n'a pas pu être mis à jour : " + error.getMessage()).queue());
    }
}
