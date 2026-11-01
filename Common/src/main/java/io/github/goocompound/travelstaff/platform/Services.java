// Modified by goocompound.
package io.github.goocompound.travelstaff.platform;

import io.github.goocompound.travelstaff.TravelStaffMain;
import io.github.goocompound.travelstaff.platform.services.INetworkHelper;

import java.util.ServiceLoader;

public class Services {

    public static final INetworkHelper NETWORK = load(INetworkHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        TravelStaffMain.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
