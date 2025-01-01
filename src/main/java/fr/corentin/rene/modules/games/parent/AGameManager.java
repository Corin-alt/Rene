package fr.corentin.rene.modules.games.parent;

import fr.corentin.rene.events.parent.AEventListener;

import java.util.List;

public abstract class AGameManager {

    public abstract List<AEventListener> registerListeners();
}
