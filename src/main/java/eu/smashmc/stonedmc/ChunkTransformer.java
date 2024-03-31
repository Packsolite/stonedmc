package eu.smashmc.stonedmc;

import com.mojang.authlib.GameProfile;
import eu.smashmc.api.core.Invoke;
import eu.smashmc.api.core.Managed;
import eu.smashmc.lib.bukkit.world.location.Locations;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.lang.reflect.Field;
import java.util.UUID;

@Managed
public class ChunkTransformer implements Listener {

	private static final GameProfile STONE_PROFILE = new GameProfile(UUID.randomUUID(), "test");


	@EventHandler
	public void onLoad(ChunkLoadEvent event) {
		var chunk = event.getChunk();
		transferChunk(chunk);
	}

	@Invoke
	public void init() {
		System.out.println("Running initial chunk transformer...");
		PacketTransformer.transformProfile(STONE_PROFILE);
		var world = Locations.spawn().getWorld();
		for (var chunk : world.getLoadedChunks()) {
			transferChunk(chunk);
		}
		System.out.println("Done.");
	}


	private void transferChunk(Chunk chunk) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				var y = 255;
				while (--y > 0) {
					var block = chunk.getBlock(x, y, z);
					var type = block.getType();
					var data = block.getData();
					if ((type == Material.STONE && data == 0) || type == Material.AIR || type == Material.STATIONARY_WATER || type == Material.STATIONARY_LAVA) {
						continue;
					}
					if (type == Material.SKULL) {
						Skull skullPaste = (Skull) block.getState();
						if (skullPaste.getOwner() != null) {
							skullPaste.setOwner(null);
						}
						try {
							Field field = skullPaste.getClass().getDeclaredField("profile");
							field.setAccessible(true);
							field.set(skullPaste, STONE_PROFILE);
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
						skullPaste.update(true, false);
					} else if (type.isSolid()) {
						block.setType(Material.STONE, false);
					} else {
						block.setType(Material.AIR, false);
					}
				}

			}
		}
	}
}
