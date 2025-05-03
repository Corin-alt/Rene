package fr.corentin.rene.events;

import fr.corentin.rene.events.parent.AMessageReceivedEventListener;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EightBallMessageReceivedEventListener extends AMessageReceivedEventListener {
    private static final Random random = new Random();
    private static final List<String> ANSWERS = Arrays.asList(
            "Wesh j'ai d'la Chloroquine en stock si tu veux, pas cher, 500 balles, c'est un prix d'ami que j'te fais là frérot",
            "Hep hep hep recule d'un mètre voyou ! La distanciation sociale tu connais pas ?",
            "Tu devrais aller aux réunions d'hydroalcooliques anonymes toi",
            "Bravo tu as trouvé un Easter Egg de mes répliques ! Nan je déconne muahaha",
            "T'as acheté ma mixtape ?",
            "Octogone sans règle dimanche prochain, oublie pas d'amener ton équerre",
            "Non je n'ai pas de bière Corona, as-tu bien lu la carte mon ami ?",
            "Le saviez-vous ? Le mot bal masqué provient à l'origine d'une pandémie.",
            "Il paraît que Fyrno a payé Eowalim pour être modérateur, mais chuuuut, ne le répète à personne.",
            "Tu savais que j'étais le père de Goldok ? Et que mon père, c'est Chuck Norris.",
            "Marre, marre, marre de la Taverne ! Emmène moi en Corée, voir de la Kpop... AHHHH BLBLBLBLBL faut que je me reprenne... ça va pas trop en ce moment...",
            "Damnation ! Je n'arrive pas à y croire.",
            "Cela est-il possible ?",
            "Franchement ! Je n'ai même pas besoin de répondre à ta question.",
            "Je n'en suis pas si sûr, vois-tu.",
            "Vraiment ? Je ne le savais pas, merci de m'apprendre un truc.",
            "Comment tu sais ça ? Tu es un espion du KGB ? Fais gaffe, je suis un agent infiltré de la CIA.",
            "Meuh oui.",
            "Moi !",
            "Je ne suis pas sûr de pouvoir répondre exactement à la question posée, donc je me tais.",
            "Je suis tavernier moi, pas diseur de bonne aventure.",
            "La boule magique a raison !",
            "Je ne te pensais pas capable de dire une chose pareille.",
            "Continue comme ça !",
            "Oh, c'est pas gentil ça, je vais le dire à Goldy !",
            "Bienvenue ! Ah non oups, je me suis trompé.",
            "Si tu le dis, c'est que c'est probablement vrai.",
            "D'accord !",
            "C'est très correct.",
            "Je ne pense pas.",
            "On m'appelle ? Non je ne suis pas là.",
            "Plouf plouf, ce sera toi qui sera la réponse, 1, 2, 3... zut alors, ça tombe sur oui.",
            "10€ et je te dis la réponse.",
            "Alors là, tu peux toujours rêver mon gars.",
            "Bien sûr que oui !",
            "À ta place, je ne poserai même pas la question.",
            "Nope.",
            "J'aimerai bien te dire la bonne réponse bien de chez nous, mais je l'ai oubliée. c'est ballot hein ?",
            "Salut ! Je vais bien et toi ?",
            "Hein ? Désolé je n'ai pas entendu ta question. Faudra que je pense à m'acheter des lunettes moi.",
            "10 puissance 2 ! Hé oui j'ai la réponse à tout moi.",
            "Apporte-moi un soda et je te donne la réponse.",
            "Hm, cela pose un sacré problème... laisse-moi y réfléchir quelques siècles, je donnerai la réponse à tes arrières-arrières-petits-fils.",
            "Salut, c'est René comme d'hab' ! Et... non, ta question est trop philosophique pour moi, désolé.",
            "En fait, je pense juste que je vais pas répondre.",
            "Bien fait pour toi !",
            "Moui, ça peut se tenter.",
            "Et pourquoi pas ?",
            "Mais bien sûr ! Et les marmottes elles mangent du chocolat.",
            "Et si c'était vrai ?",
            "C'est cela, oui.",
            "P't'et ben qu'oui, p't'et ben qu'non.",
            "Si je dis oui, c'est que je dirai pas non.",
            "Faut voir.",
            "Mais bien sûr.",
            "Fais comme chez toi !",
            "Qui sait ?",
            "Intéressant.",
            "René.exe a cessé de fonctionner, veuillez reposer la question.",
            "Tu sais quoi ? Moi pas.",
            "Flemme de répondre.",
            "Tu sais, dans la vie y'a jamais de problèmes, y'a que des solutions. Donc ta question ne vaut rien. Au suivant !",
            "Eowalim, réponds à ma place s'il te plaît, je te paie pas pour rien !",
            "Abracadabra, ça tombe sur... zut, dé cassé.",
            "What? Speak French please.",
            "¿Qué? Habla francés por favor.",
            "Ceci me laisse perplexe, et je n'aime pas être perplexe. Donc... une minute, c'était quoi ta question déjà ?",
            "René, tavernier de profession, j'exerce à... hein ? C'était pas ça la question ?",
            "D'après toi ?",
            "Je ne suis pas sûr d'avoir cerné le sens concret de ta question. En gros : tu peux répéter ?",
            "Je n'ai rien à dire là-dessus.",
            "Enorme !",
            "Bigre !",
            "Diantre, je le savais pas ça.",
            "Fichtre, je sais pas quoi répondre. Client suivant !",
            "Cornegidouille ! La réponse est... 42 ! Quoi ? Si c'est ça, René ne se trompe jamais.",
            "Par tous les dieux des tavernes ! C'est quoi cette question ?",
            "La boule magique !",
            "Essaye plus tard.",
            "Pas d'avis.",
            "C'est ton destin.",
            "Le sort en est jeté.",
            "Une chance sur deux.",
            "Repose ta question.",
            "D'après moi oui !",
            "C'est certain !",
            "Oui absolument !",
            "Tu peux compter dessus !",
            "Sans aucun doute !",
            "Très probable !",
            "Oui !",
            "C'est bien parti !",
            "C'est non.",
            "Peu probable.",
            "Faut pas rêver.",
            "N'y compte pas.",
            "Impossible.",
            "Non pas aujourd'hui.",
            "Fais-toi plaisir.",
            "Aucune idée.",
            "Essaie encore.",
            "Mais OUIIII…",
            "Absolument hors de question.",
            "Pire idée de la semaine.",
            "Clairement ça se tente !"
    );

    public EightBallMessageReceivedEventListener() {
        super(Collections.singleton(Channels.BOT));
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw();
        String emoji8ball = Emoji.fromUnicode("U+1F3B1").getFormatted();

        if (content.contains(":8ball:") || content.contains(emoji8ball)) {
            String randomAnswer = ANSWERS.get(random.nextInt(ANSWERS.size()));
            event.getChannel().sendMessage(randomAnswer).queue();
        }
    }
}