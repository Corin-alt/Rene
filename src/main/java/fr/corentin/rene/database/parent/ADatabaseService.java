package fr.corentin.rene.database.parent;

import fr.corentin.rene.Rene;
import fr.corentin.rene.managers.DatabaseManager;
import org.slf4j.Logger;

public abstract class ADatabaseService {
    protected final DatabaseManager dbManager;
    protected final Logger logger;

    protected ADatabaseService() {
        this.dbManager = DatabaseManager.getInstance();
        this.logger = Rene.getInstance().getLogger();
        createTable();
    }

    protected abstract void createTable();
}
