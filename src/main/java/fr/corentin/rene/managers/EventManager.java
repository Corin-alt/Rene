package fr.corentin.rene.managers;

import fr.corentin.rene.Rene;
import fr.corentin.rene.events.EightBallMessageListener;
import fr.corentin.rene.events.InteractionCommandsListener;
import fr.corentin.rene.events.PrefixedCommandsListener;
import fr.corentin.rene.events.parent.AEventListener;
import net.dv8tion.jda.api.JDA;

import java.util.*;

public class EventManager {
    private static EventManager eventManager;

    private final JDA jda;
    private final Map<String, List<AEventListener>> moduleEventsListeners;

    private EventManager() {
        this.jda = Rene.getInstance().getJda();
        this.moduleEventsListeners = new HashMap<>();
    }

    public void registerListener(String moduleId, AEventListener listener) {
        // Synchronize on the list for the module to avoid concurrent modification
        List<AEventListener> moduleListeners = moduleEventsListeners.computeIfAbsent(moduleId, k -> Collections.synchronizedList(new ArrayList<>()));
        moduleListeners.add(listener);
        jda.addEventListener(listener);
    }

    public void registerListeners(String moduleId, List<AEventListener> listener) {
        // Synchronize on the list for the module to avoid concurrent modification
        List<AEventListener> moduleListeners = moduleEventsListeners.computeIfAbsent(moduleId, k -> Collections.synchronizedList(new ArrayList<>()));
        moduleListeners.addAll(listener);

        listener.forEach(jda::addEventListener);
    }

    public void unregisterListeners(String moduleId) {
        List<AEventListener> moduleListeners = moduleEventsListeners.remove(moduleId);
        if (moduleListeners != null) {
            // It's safe to iterate because we've already removed the list from the map
            moduleListeners.forEach(jda::removeEventListener);
        }
    }

    public List<AEventListener> getListeners(String moduleId) {
        return moduleEventsListeners.getOrDefault(moduleId, Collections.emptyList());
    }

    public void unregisterListener(String moduleId, AEventListener listener) {
        List<AEventListener> moduleListeners = moduleEventsListeners.get(moduleId);
        if (moduleListeners != null) {
            moduleListeners.remove(listener);
            jda.removeEventListener(listener);
        }
    }

    public void registerListeners() {
        jda.addEventListener(new InteractionCommandsListener());
        jda.addEventListener(new PrefixedCommandsListener());
        jda.addEventListener(new EightBallMessageListener());
        moduleEventsListeners.forEach((moduleId, listeners) -> jda.addEventListener(listeners));
    }

    public static EventManager getInstance() {
        if (eventManager == null) {
            eventManager = new EventManager();
        }
        return eventManager;
    }
}
