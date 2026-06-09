package fr.corentin.rene.modules.games.sutom;

import java.time.LocalDate;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SutomGameManager {
    private final Map<String, SutomGame> activeGames = new ConcurrentHashMap<>();

    private LocalDate cachedDate;
    private String cachedWord;

    private static final String[] WORDS = {
            // 6 lettres
            "ABATTU", "ABOLIR", "ABSOLU", "ACCENT", "ACCORD", "ACTION", "ACTUEL", "ADORER",
            "ADULTE", "AIMANT", "ALARME", "AMENDE", "ANCIEN", "ANIMAL", "ANNEAU", "APERCU",
            "ARGENT", "ARTERE", "AUTEUR", "AVENIR", "BAGAGE", "BALCON", "BALLON", "BANANE",
            "BARQUE", "BATEAU", "BERGER", "BESOIN", "BEURRE", "BILLET", "BLAGUE", "BLESSE",
            "BOUCHE", "BOUCLE", "BOUGER", "BOUGIE", "BRAISE", "BRISER", "BRONZE", "BUREAU",
            "CABANE", "CACHER", "CADEAU", "CALMER", "CAMION", "CANARD", "CARNET", "CARTON",
            "CASINO", "CASQUE", "CAUSER", "CERISE", "CHAISE", "CHANCE", "CHEMIN", "CHEVAL",
            "CLAQUE", "CLOCHE", "COFFRE", "COMBAT", "COMBLE", "COMMUN", "COMPTE", "COPAIN",
            "COULER", "COUPLE", "COURSE", "COUSIN", "CRAYON", "DANGER", "DEFAUT", "DEMAIN",
            "DESSIN", "DISQUE", "DONNER", "DOUBLE", "DRAGON", "EFFORT", "EMPLOI", "ENIGME",
            "ENTIER", "ESPION", "ESPOIR", "ESPRIT", "ETOILE", "EVITER", "FAILLE", "FAVEUR",
            "FIGURE", "FLAMME", "FLECHE", "FLOTTE", "FORCER", "FOURNI", "FRAISE", "GATEAU",
            "GAUCHE", "GRILLE", "GUIDER", "HUMAIN", "JARDIN", "JUNGLE", "LACHER", "LETTRE",
            "LIMITE", "MAISON", "MANGER", "MARCHE", "MARINE", "MARQUE", "MASQUE", "MENACE",
            "METIER", "MIROIR", "MODULE", "MOMENT", "MOUCHE", "MOULIN", "MOUTON", "NATURE",
            "NORMAL", "NUANCE", "ORACLE", "ORGANE", "PALACE", "PAILLE", "PARDON", "PATRON",
            "PENSER", "PERDRE", "PHRASE", "PIGEON", "PLAINE", "PLANTE", "PLAQUE", "POINTE",
            "POISON", "POLICE", "PORTER", "POTAGE", "PRINCE", "PRISON", "PROFIT", "RACINE",
            "RAISON", "RAPIDE", "REGARD", "REGIME", "RELAIS", "REMISE", "RISQUE", "RIVAGE",
            "ROULER", "SAISON", "SAUMON", "SECRET", "SIGNAL", "SIMPLE", "SIRENE", "SOLEIL",
            "SOURIS", "STATUE", "TAILLE", "TALENT", "TIMBRE", "TOMATE", "VALEUR", "VALISE",
            "VOYAGE", "DETOUR", "ECLAIR", "ECOUTE", "FICHET", "MARBRE", "PELAGE", "RECOIN",
            "SOURCE", "TRESOR", "VELOUR", "PIGNON", "REQUIN", "SORTIR", "TENDRE", "VENGER",
            "VERSER", "VIOLON", "VIRAGE", "TORCHE", "PALIER", "NAVIRE", "LANCER", "INSCRIRE",
            "FOUDRE", "DEVOIR", "CONVOI", "BONDIR", "ASTHME",
            // 7 lettres
            "ABANDON", "ABEILLE", "ACCUEIL", "ACHETER", "ADAPTER", "ADRESSE", "AFFAIRE",
            "AISANCE", "ALIMENT", "AMENDER", "ANNONCE", "AVANCER", "BALANCE", "BANDAGE",
            "BARRAGE", "BERCEAU", "BISCUIT", "BIZARRE", "BLANCHE", "BONHEUR", "BONJOUR",
            "BORDURE", "BOUCHER", "BRANCHE", "CABINET", "CAPRICE", "CAPITAL", "CAPTEUR",
            "CARREAU", "CERVEAU", "CHAGRIN", "CHALEUR", "CHAMBRE", "CHANSON", "CHATEAU",
            "CHIFFRE", "CITERNE", "CLAVIER", "COFFRET", "COLLEGE", "COLONNE", "COMPLET",
            "CONCERT", "CONFLIT", "CONSEIL", "CONTRAT", "COSTUME", "COURAGE", "CRAPAUD",
            "CREUSER", "CUISINE", "CULTURE", "CYCLONE", "DEMANDE", "DERNIER", "DEVINER",
            "DIAMANT", "DISPUTE", "DOSSIER", "DOULEUR", "DRAPEAU", "ECLIPSE", "ELEMENT",
            "EMOTION", "EMPLOYE", "ENQUETE", "ENTENTE", "ENVOYER", "EPISODE", "EPREUVE",
            "EQUIPER", "ESSENCE", "ESTIMER", "ETRANGE", "EVIDENT", "EXEMPLE", "FACETTE",
            "FAMILLE", "FANTOME", "FERMIER", "FEUILLE", "FICELLE", "FORTUNE", "FROMAGE",
            "GALERIE", "GESTION", "GLACIER", "GRANITE", "GRAVIER", "HABITER", "HORIZON",
            "HORLOGE", "HOSTILE", "INSECTE", "INSTANT", "IVRESSE", "JOURNEE", "JUSTICE",
            "LECTURE", "MACHINE", "MAGASIN", "MALADIE", "MANIERE", "MANTEAU", "MATIERE",
            "MEDECIN", "MELODIE", "MEMOIRE", "MESSAGE", "MIRACLE", "MISSION", "MOISSON",
            "MONNAIE", "MORCEAU", "MOUETTE", "MYSTERE", "NOURRIR", "OBTENIR", "OPINION",
            "OUVRIER", "PARTOUT", "PASSAGE", "PASSION", "PATIENT", "PAYSAGE", "PENDULE",
            "PENSEUR", "PENSION", "PERFIDE", "PERIODE", "PETROLE", "PLANETE", "PLATEAU",
            "PLONGER", "POISSON", "PORTAIL", "PORTION", "POTERIE", "POUVOIR", "PRENDRE",
            "PRESSER", "PROFOND", "PROGRES", "PROTEGE", "QUALITE", "RECETTE", "RECOLTE",
            "REFAIRE", "REFORME", "REMPLIR", "REPARER", "RESERVE", "RESPECT", "REUNION",
            "RIVIERE", "RUPTURE", "SAUVAGE", "SECOURS", "SEMAINE", "SERPENT", "SERVICE",
            "SILENCE", "SOCIETE", "SOMMEIL", "SOUPCON", "STATION", "SURPLUS", "TABLEAU",
            "TARTINE", "TERREUR", "TORRENT", "TOURNER", "TRAVAIL", "TROMPER", "UNIVERS",
            "URGENCE", "VERDICT", "VERSION", "VILLAGE", "VITESSE", "VOLTIGE",
            // 8 lettres
            "ABSTRAIT", "ACCIDENT", "ACCEPTER", "ACTIVITE", "ALLIANCE", "ALPHABET", "ANGOISSE",
            "APPAREIL", "ARGUMENT", "ATTITUDE", "AUTORITE", "AVENTURE", "BATIMENT", "BOUCLIER",
            "BROCHURE", "CAMPAGNE", "CEINTURE", "CHANTIER", "CHAPELLE", "CHOCOLAT", "COIFFURE",
            "COMMERCE", "COMPILER", "COMPOSER", "CONQUETE", "CREATURE", "CRITIQUE", "DECOUVIR",
            "DELICATE", "DEMANDER", "DIALOGUE", "DEMARRER", "DISTANCE", "DIVISION", "DOCTRINE",
            "DOCUMENT", "DOMICILE", "ECRIVAIN", "EFFICACE", "ELECTION", "ELEPHANT", "ENSEMBLE",
            "ENTENDRE", "EQUIPAGE", "ESCALIER", "ETONNANT", "EVIDENCE", "EXAMINER", "EXERCICE",
            "EXPLORER", "EXPORTER", "FABRIQUE", "FASCINER", "FEMININE", "FESTIVAL", "FIDELITE",
            "FONTAINE", "FORMULER", "FRACTION", "FRAGMENT", "FRANCAIS", "FRICTION", "GARANTIE",
            "GENEREUX", "GOURMAND", "GRANDEUR", "HABILLER", "HERITAGE", "HORRIBLE", "HUMIDITE",
            "IMAGINER", "IMMEUBLE", "IMMORTEL", "IMPERIAL", "INCIDENT", "INFERNAL", "INNOCENT",
            "INSCRIRE", "INVASION", "JALOUSIE", "JUVENILE", "LANTERNE", "LOCALITE", "LOCATION",
            "LOGEMENT", "LONGUEUR", "LUMINEUX", "MAQUETTE", "MARGINAL", "MASSACRE", "MATERIEL",
            "MEDAILLE", "MENSONGE", "MEILLEUR", "MODIFIER", "MONUMENT", "MULTIPLE", "NATIONAL",
            "NOCTURNE", "NOMBREUX", "NOUVELLE", "OBSTACLE", "OCCASION", "OCCUPANT", "OFFICIER",
            "ORIGINAL", "ORNEMENT", "PANTALON", "PARADOXE", "PARCELLE", "PARTAGER", "PATIENCE",
            "PATRIOTE", "PAVILLON", "PIQUANTE", "PLANCHER", "POIGNARD", "PORTRAIT", "POSSIBLE",
            "POURQUOI", "PRECISER", "PREMIERE", "PRESENCE", "PRESIDER", "PRESTIGE", "PREVENIR",
            "PRIMAIRE", "PRINCIPE", "PROBABLE", "PROBLEME", "PROCEDER", "PRODUIRE", "PROFITER",
            "PROMENER", "PROPOSER", "PROSPERE", "PROTEGER", "PROVINCE", "PRUDENCE", "PUBLIQUE",
            "PUNITION", "QUALIFIE", "QUANTITE", "QUARTIER", "QUESTION", "RACONTER", "REALISER",
            "RECEVOIR", "REFORMER", "REGARDER", "REGIONAL", "RELATION", "RELIGION", "REMARQUE",
            "RENOMMER", "REPASSER", "REPORTER", "RESIDENT", "RESULTAT", "RETARDER", "REVISION",
            "REVOLTER", "RICHESSE", "ROTATION", "RUISSEAU", "SANDWICH", "SAUCISSE", "SCENARIO",
            "SECURITE", "SENSIBLE", "SENTENCE", "SEQUENCE", "SERIEUSE", "SIGNALER", "SOLITUDE",
            "SOMMAIRE", "SORCIERE", "SOUFFLER", "SOUVENIR", "SPECIMEN", "SPONTANE", "SURPRISE",
            "SYMBIOSE", "TABLETTE", "TACTIQUE", "TERMINAL", "TERRASSE", "TERRIBLE", "TONNERRE",
            "TOURNANT", "TRAHISON", "TRAVERSE", "TREMBLER", "TRIBUNAL", "TRIOMPHE", "TROPICAL",
            "TROUBLER", "TROUPEAU", "UNIFORME", "VARIABLE", "VETEMENT", "VIOLENCE", "VIRTUOSE",
            "VOLATILE", "VULGAIRE"
    };

    public synchronized void ensureDailyWord() {
        LocalDate today = LocalDate.now();
        if (cachedDate != null && today.equals(cachedDate)) return;

        long seed = today.toEpochDay() * 53 + 104729;
        Random random = new Random(seed);
        cachedWord = WORDS[random.nextInt(WORDS.length)];
        cachedDate = today;
    }

    public SutomGame startGame(String userId, String channelId) {
        ensureDailyWord();
        SutomGame game = new SutomGame(userId, channelId, cachedDate, cachedWord);
        activeGames.put(userId, game);
        return game;
    }

    public SutomGame getGame(String userId) {
        return activeGames.get(userId);
    }

    public void removeGame(String userId) {
        activeGames.remove(userId);
    }

    public boolean hasActiveGame(String userId) {
        return activeGames.containsKey(userId);
    }
}
