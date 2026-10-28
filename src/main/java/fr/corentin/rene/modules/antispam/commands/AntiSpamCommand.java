package fr.corentin.rene.modules.antispam.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import fr.corentin.rene.modules.antispam.service.AntiSpamService;
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

public class AntiSpamCommand extends AInteractionCommand {
    private final AntiSpamService antiSpamService;

    public AntiSpamCommand(AntiSpamService antiSpamService) {
        super("antispam", "Gère la protection contre les comptes piratés qui spamment les salons", Permission.ADMIN, null);
        this.antiSpamService = antiSpamService;
    }

    @Override
    public List<SubcommandData> getSubcommands() {
        return List.of(
                new SubcommandData("seuil", "Définit le seuil de détection")
                        .addOptions(
                                new OptionData(OptionType.INTEGER, "salons", "Nombre de salons différents qui déclenche le ban", true)
                                        .setRequiredRange(AntiSpamService.MIN_CHANNELS, AntiSpamService.MAX_CHANNELS),
                                new OptionData(OptionType.INTEGER, "secondes", "Durée de la fenêtre de détection en secondes", true)
                                        .setRequiredRange(AntiSpamService.MIN_SECONDS, AntiSpamService.MAX_SECONDS)),
                new SubcommandData("logs", "Définit le salon de logs")
                        .addOptions(new OptionData(OptionType.CHANNEL, "salon", "Le salon de logs", true)
                                .setChannelTypes(ChannelType.TEXT)),
                new SubcommandData("exempter-ajouter", "Exempte un rôle de la détection")
                        .addOptions(new OptionData(OptionType.ROLE, "role", "Le rôle à exempter", true)),
                new SubcommandData("exempter-retirer", "Retire l'exemption d'un rôle")
                        .addOptions(new OptionData(OptionType.ROLE, "role", "Le rôle à ne plus exempter", true)),
                new SubcommandData("statut", "Affiche la configuration actuelle"),
                new SubcommandData("activer", "Réactive la protection"),
                new SubcommandData("desactiver", "Désactive la protection sans effacer la configuration")
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
            case "seuil" -> setThreshold(event);
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

    private void setThreshold(GenericCommandInteractionEvent event) {
        int channels = Objects.requireNonNull(event.getOption("salons")).getAsInt();
        int seconds = Objects.requireNonNull(event.getOption("secondes")).getAsInt();

        antiSpamService.setThreshold(channels, seconds);
        event.reply("Un membre qui écrit dans **" + antiSpamService.getChannelThreshold() + " salons différents** en moins de **"
                + antiSpamService.getWindowSeconds() + " secondes** sera banni.").setEphemeral(true).queue();
    }

    private void setLogChannel(GenericCommandInteractionEvent event) {
        TextChannel channel = Objects.requireNonNull(event.getOption("salon")).getAsChannel().asTextChannel();

        antiSpamService.setLogChannel(channel);
        event.reply("Le salon de logs est maintenant " + channel.getAsMention() + ".").setEphemeral(true).queue();
    }

    private void addExemptRole(GenericCommandInteractionEvent event) {
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();
        if (role.isPublicRole()) {
            event.reply("Impossible d'exempter @everyone : plus personne ne serait surveillé.").setEphemeral(true).queue();
            return;
        }

        if (antiSpamService.addExemptRole(role)) {
            event.reply("Le rôle " + role.getAsMention() + " est maintenant exempté.").setEphemeral(true).queue();
        } else {
            event.reply("Le rôle " + role.getAsMention() + " est déjà exempté.").setEphemeral(true).queue();
        }
    }

    private void removeExemptRole(GenericCommandInteractionEvent event) {
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

        if (antiSpamService.removeExemptRole(role)) {
            event.reply("Le rôle " + role.getAsMention() + " n'est plus exempté.").setEphemeral(true).queue();
        } else {
            event.reply("Le rôle " + role.getAsMention() + " n'était pas exempté.").setEphemeral(true).queue();
        }
    }

    private void showStatus(GenericCommandInteractionEvent event, Guild guild) {
        String roles = antiSpamService.getExemptRoleIds().stream()
                .map(id -> guild.getRoleById(id) != null ? "<@&" + id + ">" : "Rôle supprimé (`" + id + "`)")
                .collect(Collectors.joining("\n"));

        long logChannelId = antiSpamService.getLogChannelId();
        String logChannel = logChannelId == 0 ? "Non défini"
                : guild.getTextChannelById(logChannelId) != null ? "<#" + logChannelId + ">" : "Salon supprimé (`" + logChannelId + "`)";

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(antiSpamService.isEnabled() ? Color.GREEN : Color.GRAY)
                .setTitle("Anti-spam : configuration")
                .addField("État", antiSpamService.isEnabled() ? "Activé" : "Désactivé", true)
                .addField("Bans", String.valueOf(antiSpamService.getBanCount()), true)
                .addField("Seuil", antiSpamService.getChannelThreshold() + " salons en " + antiSpamService.getWindowSeconds() + " s", false)
                .addField("Salon de logs", logChannel, false)
                .addField("Rôles exemptés", roles.isEmpty() ? "Aucun" : roles, false);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void setEnabled(GenericCommandInteractionEvent event, boolean enabled) {
        if (antiSpamService.isEnabled() == enabled) {
            event.reply(enabled ? "La protection est déjà activée." : "La protection est déjà désactivée.").setEphemeral(true).queue();
            return;
        }

        antiSpamService.setEnabled(enabled);
        event.reply(enabled ? "La protection est réactivée." : "La protection est désactivée. La configuration est conservée.")
                .setEphemeral(true).queue();
    }
}
