package fr.corentin.rene.modules.linkcleaner;

import com.google.gson.annotations.Expose;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;

public class LinkCleanerModuleConfiguration extends AModuleConfiguration {

    @Expose
    private boolean removeReferralMarketing = false;

    public boolean isRemoveReferralMarketing() {
        return removeReferralMarketing;
    }
}
