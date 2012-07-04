package com.cyprias.monarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.cyprias.monarchy.Monarchy.stanceInfo;
import com.cyprias.monarchy.commands.Commands;

public class Events implements Listener {
	private Monarchy plugin;

	public static class xpChange {
		Player player;
		int Amount;
		String worldName;

		public xpChange(Player a, String b, int c) {
			player = a;
			worldName = b;
			Amount = c;
		}
	}

	private String F(String string, Object... args) {
		return Localization.F(string, args);
	}

	public ArrayList<xpChange> queuedXP = new ArrayList<xpChange>();

	public Events(final Monarchy plugin) {
		this.plugin = plugin;

		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (queuedXP.size() > 0) {
					int totalAmount = 0;

					// for (int i = queuedXP.size() - 1; i >= 0; i--) {
					for (int i = 0; i < queuedXP.size(); i++) {
						totalAmount += queuedXP.get(i).Amount;

						try {
							plugin.playerEarnedXP(queuedXP.get(i).player, queuedXP.get(i).worldName, queuedXP.get(i).Amount);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// plugin.info("Passed up " + totalAmount + " XP from " +
					// queuedXP.size() + " players.");
					queuedXP.clear();
				}
			}
		}, 0, 20 * 1L);// every 1 second.
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if (Config.allowPatronXP == false)
			return;

		Player player = event.getPlayer();
		int amount = event.getAmount();
		// plugin.info("onPlayerExpChange: " + event.getAmount());

		boolean bottleExp = false;

		for (int i = bottleOrbs.size() - 1; i >= 0; i--) {

			if (bottleOrbs.get(i).isDead() == true) {
				bottleOrbs.remove(i);
				break;
			} else {
				// plugin.info("onPlayerExpChange bottledOrb: " +
				// bottleOrbs.get(i).getEntityId() + " = " +
				// bottleOrbs.get(i).getExperience() + " (dead:" +
				// bottleOrbs.get(i).isDead() + ")");
				if (bottleOrbs.get(i).getExperience() == amount) {
					double dist = bottleOrbs.get(i).getLocation().distance(player.getLocation());
					// plugin.info("onPlayerExpChange dist: " + dist);
					if (dist < 5) {// Generally < 3 i've found.
						bottleExp = true;
					}

				}

			}
		}

		if (bottleExp == false || Config.passupBottleExp == true) {

			if (plugin.hasPermission(player, "monarchy.passupexp"))
				queuedXP.add(new xpChange(player, player.getWorld().getName(), event.getAmount()));
		}

	}

	/*
	 * List<ThrownExpBottle> bottledExp = new ArrayList<ThrownExpBottle>();
	 * HashMap<ThrownExpBottle, Integer> bottledExpAmount = new
	 * HashMap<ThrownExpBottle, Integer>();
	 * 
	 * @EventHandler(priority=EventPriority.MONITOR) public void
	 * onExpBottleEvent(ExpBottleEvent event) {
	 * plugin.info("onExpBottleEvent getExperience: " + event.getExperience());
	 * plugin.info("onExpBottleEvent getEntityId: " +
	 * event.getEntity().getEntityId()); bottledExp.add(event.getEntity());
	 * bottledExpAmount.put(event.getEntity(), event.getExperience()); }
	 */

	// HashMap<Integer, ExperienceOrb> aliveOrbs = new HashMap<Integer,
	// ExperienceOrb>();

	List<ExperienceOrb> bottleOrbs = new ArrayList<ExperienceOrb>();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile x = event.getEntity();
		// plugin.info("onProjectileHit getEntityId: " + x.getEntityId());

		List<Entity> nearEntities = x.getNearbyEntities(1, 1, 1);
		for (Entity e : nearEntities) {
			if (e instanceof ExperienceOrb) {
				ExperienceOrb orb = (ExperienceOrb) e;
				if (orb.isDead() != true) {
					// plugin.info("onProjectileHit near: " + e.getEntityId() +
					// " " + e.getType() + " " + orb.getExperience() + ", dead:"
					// + orb.isDead() + ", empty:" + orb.isEmpty());
					bottleOrbs.add(orb);
				}
			}
		}

		for (int i = bottleOrbs.size() - 1; i >= 0; i--) {
			if (bottleOrbs.get(i).isDead() == true) {
				// plugin.info("onProjectileHit removing dead: " +
				// bottleOrbs.get(i).getEntityId());
				bottleOrbs.remove(i);
				break;
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		plugin.ranks.loadPlayerPermissions(event.getPlayer());

		if (plugin.hasPermission(player, "monarchy.receiveofflineexp"))
			plugin.database.givePlayerOfflineXP(player);

		if (Config.chargeProvinceRent == true)
			plugin.database.chargeProvinceRent(player);

		if (plugin.hasPermission(player, "monarchy.exp")) {
			int currentXP = Monarchy.getTotalExperience(player);
			plugin.sendMessage(player, F("stYouHaveExp", currentXP));
		}

		if (player.isOp() && Config.notifyOpsOfNewVersion == true) {
			if (plugin.latestVersion == null) {
				plugin.latestVersion = plugin.getLatestVersion();
			}
			String curVersion = plugin.getDescription().getVersion();

			if (curVersion.compareTo(plugin.latestVersion) < 0) {
				plugin.sendMessage(player, F("stVersionAvailable", ChatColor.RED + curVersion, ChatColor.GREEN + plugin.latestVersion));
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeave(PlayerQuitEvent event) {
		plugin.ranks.unloadPlayerPermissions(event.getPlayer());
	}

	public HashMap<String, HashMap<String, Double>> hasAttacked = new HashMap<String, HashMap<String, Double>>();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.isCancelled())
			return;

		if (Config.PvPMode == false)
			return;

		if (event.getDamager().getType() == EntityType.PLAYER && event.getEntity().getType() == EntityType.PLAYER) {
			// if (event.getDamager().getType() == EntityType.PLAYER){
			Player attacker = (Player) event.getDamager();
			String attackerName = attacker.getName();
			String attackerMonarch = plugin.getMonarch(attackerName);

			Player victim = (Player) event.getEntity();
			String victimName = victim.getName();
			String victimMonarch = plugin.getMonarch(victimName);

			// String victimName = "ent"+event.getEntity().getEntityId();
			// String victimMonarch = event.getEntity().getType().toString();

			int stanceID = Config.defaultStance;
			
			stanceInfo stance = plugin.getStance(attackerName, victimName);
			if (stance != null)
				stanceID = stance.stanceID;
			
			
			//int stanceID = plugin.getInheritStance(attackerMonarch, victimName, victimMonarch);

			Boolean canAttack = plugin.canAttack(attacker, stanceID);

			if (playerHasAttacked(victimName, attackerName))
				canAttack = true;

			/*
			 * plugin.info(attackerName + " (" + attackerMonarch + ") attacked "
			 * + victimName + " (" +victimMonarch + ") . " +
			 * plugin.stanceColours[stanceID].toString() +
			 * plugin.stanceNames[stanceID] + ": " + canAttack);
			 */

			// String c_victimName =
			// plugin.getRelationshipColour(attackerMonarch,
			// victimName).toString()+victimName;
			// String c_victimMonarch =
			// plugin.getRelationshipColour(attackerMonarch,
			// victimMonarch).toString()+victimMonarch;

			String c_victimName = plugin.getColouredFullName(attackerMonarch, victimName, victimMonarch);

			if (attackerMonarch.equalsIgnoreCase(attackerName)) {
				plugin.sendMessageSpam(attacker,
					F("stYoureStanceWith", Monarchy.stanceColours[stanceID].toString() + Monarchy.stanceNames[stanceID], c_victimName), 5, false);
			} else {
				String c_attackerMonarch = Monarchy.stanceColours[0].toString() + attackerMonarch;
				plugin.sendMessageSpam(attacker,
					F("stPlayerStanceWith", c_attackerMonarch, Monarchy.stanceColours[stanceID].toString() + Monarchy.stanceNames[stanceID], c_victimName), 5,
					false);
			}

			if (canAttack == false) {
				plugin.sendMessageSpam(attacker,
					F("stNoPermissionToAttack", Monarchy.stanceColours[stanceID].toString() + Monarchy.stanceNames[stanceID], 1, false));
				event.setCancelled(true);
				return;
			}
			playerAttackedPlayer(attackerName, victimName);

		}

	}

	public void playerAttackedPlayer(String attackerName, String victimName) {
		if (!hasAttacked.containsKey(attackerName))
			hasAttacked.put(attackerName, new HashMap<String, Double>());

		hasAttacked.get(attackerName).put(victimName, Monarchy.getUnixTime());
	}

	public boolean playerHasAttacked(String attackerName, String victimName) {
		if (hasAttacked.containsKey(attackerName)) {
			if (hasAttacked.get(attackerName).containsKey(victimName)) {
				Double attackTime = (Double) hasAttacked.get(attackerName).get(victimName);
				Double timeDiff = Monarchy.getUnixTime() - attackTime;
				// plugin.info("playerHasAttacked " + attackerName + " > " +
				// victimName + " = " + timeDiff);
				if (timeDiff < Config.defendTime)
					return true;
			}
		}
		return false;
	}

	/*
	 * @EventHandler(priority = EventPriority.NORMAL) public void
	 * onEntityDeath(EntityDeathEvent event) { plugin.info("onEntityDeath1 " +
	 * event.getEntity().getEntityId() + " > " + event.getDroppedExp());
	 * plugin.info("onEntityDeath2 " + event.getEntity().getLastDamageCause());
	 * }
	 */

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(PlayerChatEvent event) {
		if (Config.colourChatNamesByStance == false)
			return;
		
		if (event.isCancelled()) {
			return;
		}
		if (event.getMessage() == null) {
			return;
		}
		Player player = event.getPlayer();
		String playerName = player.getName();
		String message = event.getMessage();
		String format = event.getFormat();
		
		//Set<Player> x = event.getRecipients();
		String c_targetName;
		String senderMonarch = plugin.getMonarch(playerName);
		String receiverMonarch;
		for (Player oPlayer : this.plugin.getServer().getOnlinePlayers()) {
			if (!oPlayer.equals(player)){
				event.getRecipients().remove(oPlayer);
				receiverMonarch = plugin.getMonarch(oPlayer.getName());
				String c_senderName = plugin.getColouredName(receiverMonarch, playerName, senderMonarch);
				oPlayer.sendMessage(String.format(format, c_senderName, message));
			}
			
			
		}
		
		
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if (!(event instanceof PlayerDeathEvent)) {
			return;
		}

		PlayerDeathEvent subEvent = (PlayerDeathEvent) event;
		EntityDamageEvent dEvent = event.getEntity().getLastDamageCause();

		Player victim = (Player) event.getEntity();

		if (!(dEvent instanceof EntityDamageByEntityEvent)) {
			return;
		}

		EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) dEvent;

		if (!(damageEvent.getDamager() instanceof Player)) {
			return;
		}

		Player attacker = (Player) damageEvent.getDamager();

		// plugin.info("onEntityDeath2 " + attacker.getName() + " > " +
		// victim.getName() );

		PvPKill(attacker, victim);
	}

	public void PvPKill(Player attacker, Player victim) {
		// Player attacker = (Player) event.getDamager();
		String attackerName = attacker.getName();
		String attackerMonarch = plugin.getMonarch(attackerName);

		// Player victim = (Player) event.getEntity();
		String victimName = victim.getName();
		String victimMonarch = plugin.getMonarch(victimName);

		// plugin.info("PvPKill " + attackerName + " > " +victimName );

		String attackerFullName = attackerName + " §7(§f" + attackerMonarch + "§7)";
		String victimFullName = victimName + " §7(§f" + victimMonarch + "§7)";

		plugin.info(F("stFollowerKilledPlayer", attackerFullName, victimFullName));

		FollowerKilledPlayer(attackerName, attackerMonarch, victimName, victimMonarch);
		PlayerAttackedFollower(attackerName, attackerMonarch, victimName, victimMonarch);

	}

	public void FollowerKilledPlayer(String attackerName, String attackerMonarch, String victimName, String victimMonarch) {

		String attackerFullName = plugin.stanceColours[0] + attackerName;
		String victimFullName = plugin.getColouredFullName(attackerMonarch, victimName, victimMonarch);

		// String c_victimName = plugin.getColouredFullName(attackerMonarch,
		// victimName, victimMonarch);

		String message = F("stFollowerKilledPlayer", attackerFullName, victimFullName);
		// plugin.info("FollowerKilledPlayer: " + message);

		plugin.notifyMonarchy(attackerMonarch, plugin.chatPrefix + message, "monarchy.notify.myfollowerkilled");

	}

	public void PlayerAttackedFollower(String attackerName, String attackerMonarch, String victimName, String victimMonarch) {

		String attackerFullName = plugin.getColouredFullName(victimMonarch, attackerName, attackerMonarch);
		String victimFullName = plugin.stanceColours[0] + victimName;

		String message = F("stPlayerKilledFollower", victimFullName, attackerFullName);

		// plugin.info("PlayerAttackedFollower: " + message);

		plugin.notifyMonarchy(victimMonarch, plugin.chatPrefix + message, "monarchy.notify.myfollowerslain");

	}

	/*
	 * @EventHandler(priority = EventPriority.NORMAL) public void
	 * onPlayerDeath(PlayerDeathEvent event) { }
	 */

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		if (plugin.hasPermission(player, "monarchy.receiveofflineexp"))
			plugin.database.givePlayerOfflineXP(player);

		if (Config.chargeProvinceRent == true)
			plugin.database.chargeProvinceRent(player);

		if (plugin.hasPermission(player, "monarchy.exp")) {
			int currentXP = Monarchy.getTotalExperience(player);
			plugin.sendMessage(player, F("stYouHaveExp", currentXP));
		}

		plugin.ranks.unloadPlayerPermissions(player);
		plugin.ranks.loadPlayerPermissions(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();
		String command = msg.split(" ")[0].replace("/", "");

		if (Commands.aliases.containsKey(command.toLowerCase())) {
			event.setMessage(msg.replaceFirst("/" + command, "/" + Commands.aliases.get(command.toLowerCase())));
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockDamage(BlockDamageEvent event) {
		if (Config.useProvinceProtection == false)
			return;

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (plugin.provinces.canModifyBlock(player, block) == false) {
			String playerName = player.getName();
			// stCannotModifyProvince

			//

			String owner = plugin.provinces.getProvinceOwner(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
			String myMonarch = plugin.getMonarch(playerName);
			String c_targetName = plugin.getRelationshipColour(myMonarch, owner).toString() + owner;
			plugin.sendMessage(player, F("stCannotModifyProvince", c_targetName), false);

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (Config.useProvinceProtection == false)
			return;

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (plugin.provinces.canModifyBlock(player, block) == false) {
			String playerName = player.getName();
			String owner = plugin.provinces.getProvinceOwner(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
			String myMonarch = plugin.getMonarch(playerName);
			String c_targetName = plugin.getRelationshipColour(myMonarch, owner).toString() + owner;
			plugin.sendMessage(player, F("stCannotModifyProvince", c_targetName), false);

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (Config.useProvinceProtection == false)
			return;

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (plugin.provinces.canModifyBlock(player, block) == false) {
			String playerName = player.getName();
			String owner = plugin.provinces.getProvinceOwner(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
			String myMonarch = plugin.getMonarch(playerName);
			String c_targetName = plugin.getRelationshipColour(myMonarch, owner).toString() + owner;
			plugin.sendMessage(player, F("stCannotModifyProvince", c_targetName), false);

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (Config.useProvinceProtection == false)
			return;

		IgniteCause cause = event.getCause();
		if (cause == IgniteCause.FLINT_AND_STEEL) {
			Player player = event.getPlayer();
			Block block = event.getBlock();

			if (plugin.provinces.canModifyBlock(player, block) == false) {
				String playerName = player.getName();
				String owner = plugin.provinces.getProvinceOwner(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
				String myMonarch = plugin.getMonarch(playerName);
				String c_targetName = plugin.getRelationshipColour(myMonarch, owner).toString() + owner;
				plugin.sendMessage(player, F("stCannotModifyProvince", c_targetName), false);

				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event) {
		if (Config.useProvinceProtection == false)
			return;

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (plugin.provinces.canModifyBlock(player, block) == false) {
			String playerName = player.getName();
			String owner = plugin.provinces.getProvinceOwner(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
			String myMonarch = plugin.getMonarch(playerName);
			String c_targetName = plugin.getRelationshipColour(myMonarch, owner).toString() + owner;
			plugin.sendMessage(player, F("stCannotModifyProvince", c_targetName), false);

			event.setCancelled(true);
			return;
		}
	}

}
