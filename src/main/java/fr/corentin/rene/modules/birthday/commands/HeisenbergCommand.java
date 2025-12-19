package fr.corentin.rene.modules.birthday.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

public class HeisenbergCommand extends AInteractionCommand {

    public HeisenbergCommand() {
        super("heisenberg", "rood/etrop", Permission.ALL, null);
    }

    /**
     //TODO Don't forget to delete
     - use a Vigenère cipher
     - take the numeric code and remove all spaces
     - convert each digit individually into a letter (A/a = 0)
        - if 13 36 -> BD DG -> BDDG
     - the resulting sequence of letters forms the decryption key
     Xszorw, qqq gliv, fe oh uhrc sbu buuwj smqtnh... Xy ew yrblohnv fsw rug n'bvemw qxfpmi ge txrsrkpft ?? Iaje.
     */
    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        event.reply("35 21 55 53 45 55 54 12 43 41 34 23 21").queue();
        return true;
    }
}
