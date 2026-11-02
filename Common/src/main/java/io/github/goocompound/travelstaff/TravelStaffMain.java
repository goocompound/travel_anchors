// Modified by goocompound.
package io.github.goocompound.travelstaff;

import io.github.goocompound.travelstaff.config.TravelStaffConfig;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class TravelStaffMain {
	public static final String MOD_ID = "travelstaff";

	public static final Supplier<ClientConfigData> CLIENT_CONFIG = TravelStaffConfig::getClientConfig;
	public static final Supplier<SyncedConfigData> CONFIG = TravelStaffConfig::getConfig;

	public static void init() {
		TravelAnchorRegistry.init();
	}

	public static SyncedConfigData getConfig(Level level) {
		return level.isClientSide() ? TravelStaffConfig.getSyncedConfig() : TravelStaffConfig.getServerConfig();
	}

	public static ClientConfigData getClientConfig() {
		return TravelStaffConfig.getClientConfig();
	}

	public static final String MOD_NAME = "Staff of Travelling";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final class ClientConfigData {
		public boolean isElevatorMode = true;
		public double nameplateScale = 0.7;
	}

	public static final class SyncedConfigData {
		public int maxTeleportDistance = 64;
		public int shortTeleportDistance = 7;
		public int staffCooldownTicks = 20;
		public float rangeEnchantScaling = 0.5F;
		public double maxAngle = 30;
	}
}
