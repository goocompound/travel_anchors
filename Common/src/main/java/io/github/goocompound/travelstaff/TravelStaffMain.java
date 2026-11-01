// Modified by goocompound.
package io.github.goocompound.travelstaff;

import ca.lukegrahamlandry.lib.config.Comment;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelStaffMain {
	public static final String MOD_ID = "travelstaff";

	public static ConfigWrapper<ClientConfigData> CLIENT_CONFIG = ConfigWrapper.client(ClientConfigData.class).named(MOD_ID);
	public static ConfigWrapper<SyncedConfigData> CONFIG = ConfigWrapper.synced(SyncedConfigData.class).named(MOD_ID);

	public static void init() {
		TravelAnchorRegistry.ITEMS.init();
	}

	public static final String MOD_NAME = "Staff of Travelling";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final class ClientConfigData {
		@Comment("When true, while standing on an anchor, you can use space to go to an anchor directly above you or shift to go directly down. When false you must actually look at the target anchor to teleport.")
		public boolean isElevatorMode = true;
	}

	public static final class SyncedConfigData {
		@Comment("The maximum distance you can teleport to an anchor (with no range enchantment).")
		public int maxTeleportDistance = 64;
		@Comment("The maximum distance you can short-range teleport with shift-click. This will not be scaled by the range enchantment. ")
		public int shortTeleportDistance = 7;
		@Comment("How long between uses of the staff of travelling (or item with teleportation enchantment)")
		public int staffCooldownTicks = 20;
		@Comment("How much farther does each level of range enchantment allow you to teleport to an anchor. dist = maxTeleportDistance * (1 + (lvl * rangeEnchantScaling))")
		public float rangeEnchantScaling = 0.5F;
		@Comment("The maximum angle you can look at the anchor to teleport.")
		public double maxAngle = 30;
	}
}