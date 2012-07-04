package com.cyprias.monarchy;

import in.mDev.MiracleM4n.mChatSuite.mChatSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cyprias.monarchy.commands.Commands;
import com.cyprias.monarchy.commands.Commands.requestInfo;

public class Monarchy extends JavaPlugin {
	public static String chatPrefix = "§f[§6Monarchy§f] ";
	public Events events;
	public Config config;
	public Database database;
	public Commands commands;
	public Localization localization;
	public Ranks ranks;
	public YML yml;
	public Homes homes;
	public Provinces provinces;
	
	private String stPluginEnabled = "§f%s §7v§f%s §7is enabled.";

	mChatSuite mPlugin;

	String pluginName;

	public static String[] stanceNames = { "Monarchy", "Allied", "Friendly", "Neutral", "Wary", "Hostile", };

	
	public static ChatColor[] stanceColours = { ChatColor.LIGHT_PURPLE, ChatColor.AQUA,// "&b",
		ChatColor.GREEN,// "&a",
		ChatColor.BLUE,// "&9",
		ChatColor.YELLOW,// "&e",
		ChatColor.RED // "&c"
	};
	
	
	HashMap<String, Integer> stanceIDs = new HashMap<String, Integer>();

	public void onEnable() {
		
		
		pluginName = getDescription().getName();

		for (int i = 1; i < stanceNames.length; i++) {
			// info(i + ": " + stanceNames[i]);
			stanceIDs.put(stanceNames[i].toLowerCase(), i);
		}
		
		
		this.config = new Config(this);
		this.database = new Database(this);
		if (this.isEnabled() == false)
			return;
			
		this.yml = new YML(this);
		this.localization = new Localization(this);
		this.events = new Events(this);
		this.commands = new Commands(this);
		this.ranks = new Ranks(this);
		this.homes = new Homes(this);
		this.provinces = new Provinces(this);
		
		getCommand("patron").setExecutor(this.commands.commandPatron);
		getCommand("monarch").setExecutor(this.commands.commandMonarch);
		getCommand("vassal").setExecutor(this.commands.commandVassal);
		getCommand("follower").setExecutor(this.commands.commandFollower);
		getCommand("monarchy").setExecutor(this.commands.commandMonarchy);
		getCommand("province").setExecutor(this.commands.commandProvince);
		
		
		mPlugin = (mChatSuite) getServer().getPluginManager().getPlugin("mChatSuite");

		getServer().getPluginManager().registerEvents(this.events, this);
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {}
		
		
		info(String.format(this.stPluginEnabled, pluginName, getDescription().getVersion()));

		// if (config.startupVersionCheck == true)
		// queueVersionCheck(null);
	}

	public void onDisable() {
		this.getServer().getScheduler().cancelAllTasks();
		this.getServer().getScheduler().cancelTasks(this);
	}
	
	public void info(String msg) {
		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
	}

	HashMap<String, Boolean> grantedXP = new HashMap<String, Boolean>();

	public void playerEarnedXP(Object player, String worldName, int Amount, boolean isLoop) {
		if (Amount <= 0)
			return;

		String playerName = null;
		if (player instanceof Player) {
			playerName = ((Player) player).getName();
		} else if (player instanceof String) {
			playerName = (String) player;
		}

		// info(playerName +" receieved " + Amount + " XP.");

		if (Config.preventXPLooping == true) {
			grantedXP.put(playerName, true);
		}

		
		
		String patron = getPatron(playerName);
		if (patron != null) {
			
			int passUpXP; //
			
			if (Config.useSimplePassupPercent == true){
				if (isLoop == true){
					passUpXP = getPassupAmount(Amount, Config.passUpPercent * Config.grandPatronPassupModifier);
				}else{
					passUpXP = getPassupAmount(Amount, Config.passUpPercent);//Config.passUpPercent
				}
			}else{
				Double loyalty = database.getLoyalty(playerName);
				Double leadership = database.getLeadership(patron);
				double passUpPercent = getPassupPercentage(leadership, loyalty);
				
				if (isLoop == true){
					passUpXP = getPassupAmount(Amount, passUpPercent * Config.grandPatronPassupModifier);
				}else{
					passUpXP = getPassupAmount(Amount, passUpPercent);//Config.passUpPercent
				}
			}
			
			

			
			//= getPassupAmount(Amount, passUpPercent);//Config.passUpPercent
			
		//	info("Amount: " +Amount + ", player: " + playerName + ", loyalty: " + loyalty + ", patron: " + patron + ", leadership: " + leadership + ", passUpPercent: " +passUpPercent + ", passUpXP: " + passUpXP);
			
			if (passUpXP > 0) {

				if (grantedXP.containsKey(patron)) {
					// info(patron + " already recieved XP, exiting loop.");
					return;
				}

				Player oPlayer = getServer().getPlayer(patron);

				
				
				if (oPlayer.isOnline() && oPlayer.getWorld().getName().equalsIgnoreCase(worldName)) {
					oPlayer.getPlayer().giveExp(passUpXP);
				} else if (playerHasExisted(patron)) {
					database.storeOfflineXP(patron, worldName, passUpXP, playerName);
				}

				database.savePassupXP(playerName, patron, passUpXP);

				if (Config.allowGrandPatronXP == true) {
					playerEarnedXP(patron, worldName, passUpXP, true);
				}
			}

		}
		if (isLoop == false && Config.preventXPLooping == true) {
			// info("Clearing grantedXP");
			grantedXP.clear();
		}
	}
	
	public double getPassupPercentage(double leadershipSkill, double loyaltySkill){
		return (leadershipSkill + loyaltySkill) / 2;
		
	}
	

	public void playerEarnedXP(Object player, String world, int Amount) {
		playerEarnedXP(player, world, Amount, false);
	}
	
	public int getPassupAmount(int baseAmount, double passPercentage) {
		// Since we're working with ints, I can't give a percentage of an int,
		// so instead I do a random() and if it's lower than our percentage,
		// I return the full amount.
		if (Math.random() <= passPercentage) {
			return baseAmount;
		}
		return 0;
	}

	public void sendMessage(CommandSender sender, String message, Boolean showConsole) {
		if (sender instanceof Player && showConsole == true) {
			info("§e" + sender.getName() + "->§f" + message);
		}
		sender.sendMessage(chatPrefix + message);
	}

	public void sendMessage(CommandSender sender, String message) {
		sendMessage(sender, message, true);
	}
	
	HashMap<String, Double> msgSpam = new HashMap<String, Double>();
	public void sendMessageSpam(CommandSender sender, String message, int fullTimeout, Boolean showConsole) {
		if (msgSpam.containsKey(sender.getName()+message)){
			Double lastSent = msgSpam.get(sender.getName()+message);
			Double timeDiff = getUnixTime() - lastSent;

			if (timeDiff < fullTimeout){
				msgSpam.put(sender.getName()+message, getUnixTime());
				return;
			}
				
		}

		sendMessage(sender, message, showConsole);
		msgSpam.put(sender.getName()+message, getUnixTime());
	}
	
	public void sendMessageSpam(CommandSender sender, String message, int throttle) {
		sendMessageSpam(sender, message, throttle, true);
	}
	
	public void sendMessageSpam(CommandSender sender, String message) {
		sendMessageSpam(sender, message, 1, true);
	}
	
	public Player findPlayerByName(String name) {

		for (Player p : getServer().getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name))
				return p;

			if (p.getDisplayName().equalsIgnoreCase(name))
				return p;
		}

		return null;
	}

	public void listPlayersVassals(Player player) {

		List<Database.vassalInfo> vassals = database.getPlayerVassals(player.getName());
		// sendMessage(player, "You have " + vassals.size() + " vassals.");

		if (vassals.size() > 0) {

			String vassalList = vassals.get(0).vassalName + " (" + vassals.get(0).xpAmount + L("stXP") + ")";
			for (int i = 1; i < vassals.size(); i++) {
				vassalList += ", " + vassals.get(i).vassalName + " (" + vassals.get(i).xpAmount + L("stXP") + ")";
			}

			// sendMessage(player, "Vassals: " + vassalList + ".");
			sendMessage(player, F("stListVassals", vassals.size(), vassalList));

		} else {
			sendMessage(player, F("stListNoVassals"));
		}

	}



	public void expungeStance(Player player, String playerMonarch, String targetName) {
		/*
		Player onlineTarget = findPlayerByName(targetName);
		if (onlineTarget != null) {
			targetName = onlineTarget.getName();// Make sure we got the case
												// correct name.
		} else {
			if (!playerHasExisted(targetName)) {
				sendMessage(player, F("stPlayerHasNotExisted", targetName));
				return;
			}
		}
*/
		Player onlineTarget = findPlayerByName(targetName);
		if (onlineTarget == null) {
			if (!playerHasExisted(targetName)) {
				sendMessage(player, F("stPlayerHasNotExisted", targetName));
				return;
			}
		}
		
		
		if (!hasStance(playerMonarch, targetName)) {
			sendMessage(player, F("stNoExistingStance", targetName));
			return;
		}

		int stanceID = Config.defaultStance;
		int success = database.expungeStance(player.getName(), targetName);

		// info("declareStance: " + success);
		if (success == 1) {
			clearStanceCaches();
			String targetMonarch = getMonarch(targetName);
			sendMessage(player, F("stYourStanceChanged", stanceColours[stanceID].toString() + stanceNames[stanceID], getColouredFullName(playerMonarch, targetName, targetMonarch)));
			

			
			//getColouredFullName
			
			
			
			notifyMonarchy(
				playerMonarch,
				chatPrefix
					+ F("stDeclaredStance", stanceColours[0] + getDisplayName(player), stanceColours[stanceID].toString() + stanceNames[stanceID],
						ChatColor.WHITE + getColouredFullName(playerMonarch, targetName, targetMonarch)), "monarchy.notify.mymonarchdeclaredstance", player.getName());
			
			
			if (Config.notifyTargetMonarchyOfStance == true)
				notifyMonarchy(
					targetMonarch,
					chatPrefix
						+ F("stDeclaredStance", ChatColor.WHITE + getDisplayName(player), stanceColours[stanceID].toString() + stanceNames[stanceID],
							stanceColours[0] + targetName), "monarchy.notify.othermonarchdeclaredstance");

		
			return;
		}

		sendMessage(player, F("stDBError"));

	}

	public void declareStance(Player player, String playerMonarch, String targetName, String stance) {


		/*
		Player onlineTarget = findPlayerByName(targetName);
		if (onlineTarget != null) {
			targetName = onlineTarget.getName();// Make sure we got the case
												// correct name.
		} else {
			if (!playerHasExisted(targetName)) {
				sendMessage(player, F("stPlayerHasNotExisted", targetName));
				return;
			}
		}
*/
		
		Player onlineTarget = findPlayerByName(targetName);
		if (onlineTarget == null) {
			if (!playerHasExisted(targetName)) {
				sendMessage(player, F("stPlayerHasNotExisted", targetName));
				return;
			}
		}
		
		// if (!hasStance(playerMonarch, targetName)){
		// sendMessage(player, F("stNoExistingStance", targetName));
		// return;
		// }

		if (!stanceIDs.containsKey(stance.toLowerCase())) {
			sendMessage(player, F("stNotValidStance", stance));
			sendMessage(player, F("stStances", L("stAllied"), L("stFriendly"), L("stNeutral"), L("stWary"), L("stHostile")));
			return;
		}

		int stanceID = stanceIDs.get(stance.toLowerCase());

		if (stanceID == Config.defaultStance) {

			if (!hasStance(playerMonarch, targetName)) {

				sendMessage(player, F("stAlreadyThatStance", stanceColours[stanceID].toString() + stanceNames[stanceID], targetName));

				return;
			}

			expungeStance(player, playerMonarch, targetName);
			return;
		}

		int success = database.saveStance(player.getName(), stanceID, targetName);

		// info("declareStance: " + success);
		if (success == 1) {
			clearStanceCaches();
			String targetMonarch = getMonarch(targetName);
			sendMessage(player, F("stYourStanceChanged", stanceColours[stanceID].toString() + stanceNames[stanceID], getColouredFullName(playerMonarch, targetName, targetMonarch)));
			

			
			
			
			
			notifyMonarchy(
				playerMonarch,
				chatPrefix
					+ F("stDeclaredStance", stanceColours[0] + getDisplayName(player), stanceColours[stanceID].toString() + stanceNames[stanceID],
						ChatColor.WHITE + getColouredFullName(playerMonarch, targetName, targetMonarch)), "monarchy.notify.mymonarchdeclaredstance", player.getName());
			
			if (Config.notifyTargetMonarchyOfStance == true)
				notifyMonarchy(
					targetMonarch,
					chatPrefix
						+ F("stDeclaredStance", ChatColor.WHITE + getDisplayName(player), stanceColours[stanceID].toString() + stanceNames[stanceID],
							stanceColours[0] + targetName), "monarchy.notify.othermonarchdeclaredstance");
			
			
			return;
		}

		sendMessage(player, F("stDBError"));

		// stanceNames
		// stanceIDs

	}

	public void playerPledgeAlliance(Player player, String patronName) {
		// info("playerPledgeAlliance " + player.getName() + " to " +
		// patronName);

		String currentPatron = getPatron(player.getName());

		if (currentPatron != null) {
			sendMessage(player, F("stAlreadyHavePatron", currentPatron));
			return;
		}

		Player patron = findPlayerByName(patronName);
		if (patron == null) {
			if (!playerHasExisted(patronName)) {
				sendMessage(player, F("stPlayerHasNotExisted", patronName));
				return;
			}

			// Patron has to be online to pledge, makes sure we have the correct
			// name incase player mispelled it.
			sendMessage(player, F("stCannotFindPlayer", patronName));
			return;
		}

		forcePledge(player, player.getName(), patronName);

	}

	public static class allegianceInfo {
		String playerName;
		String patronName;
		String monarchName;

		public allegianceInfo(String a, String b, String c) {
			playerName = a;
			patronName = b;
			monarchName = c;
		}
	}

	// HashMap<String, String> pledgedPlayers = new HashMap<String, String>();

	List<allegianceInfo> pledgedPlayers = new ArrayList<allegianceInfo>();
	List<allegianceInfo> dissolvedPlayers = new ArrayList<allegianceInfo>();

	public void forcePledge(CommandSender sender, String playerName, String patronName) {
		// function for debugging.

		if (playerName.equalsIgnoreCase(patronName)) {
			sendMessage(sender, F("stCannotPledgeToYourself"));
			return;
		}

		Boolean follower = isFollower(playerName, patronName);

		if (follower == false || Config.preventAllianceLoops == false){
			database.playerPledgedAllegiance(playerName, patronName);
		}else{
			sendMessage(sender, F("stCannotPledgeToFollower", patronName));
			return;
		}
		

		clearAllegianceCaches();

		if (sender.getName().equalsIgnoreCase(playerName)) {
			sendMessage(sender, F("stPlayerPledgedAllegiance", patronName));
		} else {// should only happen upon force dissolve, not too worried about
				// locale.
			sendMessage(sender, playerName + " pledged to " + patronName);
		}

		// Notify everyone in the monarchy of the new pledge, run in new thread
		// due to high number of SQL calls.
		String monarchName = getMonarch(patronName);
		pledgedPlayers.add(new allegianceInfo(playerName, patronName, monarchName));
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				List<allegianceInfo> temp = new ArrayList<allegianceInfo>();
				temp.addAll(pledgedPlayers);
				pledgedPlayers.clear();

				if (temp.size() > 0) {
					for (int i = temp.size() - 1; i >= 0; i--) {
						// notifyOfDissolve(temp.get(i).playerName,
						// temp.get(i).patronName, temp.get(i).monarchName);

						notifyMonarchy(temp.get(i).monarchName,
							chatPrefix + F("stPledgeNofify", temp.get(i).playerName, temp.get(i).patronName, temp.get(i).monarchName),
							"monarchy.notify.followerjoin");
					}
				}

			}
		}, 0L);// asap

	}

	public void forceDissolve(CommandSender sender, String playerName, String patronName) {
		database.playerDissolveAllegiance(playerName, patronName);
		clearAllegianceCaches();

		if (sender.getName().equalsIgnoreCase(playerName)) {
			sendMessage(sender, F("stPlayerDissolvedAllegiance", patronName));
		} else {// should only happen upon force dissolve, not too worried about
				// locale.
			sendMessage(sender, playerName + " left " + patronName);
		}

		// Notify everyone in the monarchy of the new pledge, run in new thread
		// due to high number of SQL calls.
		String monarchName = getMonarch(patronName);
		dissolvedPlayers.add(new allegianceInfo(playerName, patronName, monarchName));
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {

				List<allegianceInfo> temp = new ArrayList<allegianceInfo>();
				temp.addAll(dissolvedPlayers);
				dissolvedPlayers.clear();

				if (temp.size() > 0) {
					for (int i = temp.size() - 1; i >= 0; i--) {
						// notifyOfDissolve(temp.get(i).playerName,
						// temp.get(i).patronName, temp.get(i).monarchName);
						notifyMonarchy(temp.get(i).monarchName,
							chatPrefix + F("stDissolveNofify", temp.get(i).playerName, temp.get(i).patronName, temp.get(i).monarchName),
							"monarchy.notify.followerleave");
					}
				}

			}
		}, 0L);// asap

	}

	public void notifyMonarchy(String monarchName, String message, String permissionNode, String bypassPlayer) {
		String oMonarch;
		for (Player p : getServer().getOnlinePlayers()) {
			oMonarch = getMonarch(p.getName());
			if (monarchName.equalsIgnoreCase(oMonarch)) {
				if (permissionNode == null || hasPermission(p, permissionNode)) {
					// sendMessage(p, message);
					
					if (bypassPlayer == null || !bypassPlayer.equalsIgnoreCase(p.getName()))
						p.sendMessage(message);
				}
			}
		}
	}
	
	public void notifyMonarchy(String monarchName, String message, String permissionNode) {
		notifyMonarchy(monarchName, message, permissionNode, null);
	}

	public void playerDissolveAlliance(Player player, String patronName) {
		String currentPatron = getPatron(player.getName());
		if (currentPatron == null) {
			sendMessage(player, F("stPlayerNoPatron"));

		} else if (patronName.equalsIgnoreCase(currentPatron)) {
			forceDissolve(player, player.getName(), currentPatron);

		} else {
			sendMessage(player, F("stPlayerMispellPatron", patronName, currentPatron));
		}

	}

	public void playerDissolveVassalAlliance(Player player, String vassalName) {

		boolean isVassal = isVassal(player.getName(), vassalName);
		if (isVassal == false) {
			sendMessage(player, F("stNotYourVassal", vassalName));

			return;
		}

		int success = database.dropVassal(player.getName(), vassalName);
		if (success > 0) {
			sendMessage(player, F("stPlayerDroppedVassal", vassalName));
			clearAllegianceCaches();
			return;
		}
		sendMessage(player, F("stErrorFailedToDropVassal", vassalName));
	}

	public boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		if (player.isOp()) {
			return true;
		}

		if (player.isPermissionSet(node))
			return player.hasPermission(node);

		String[] temp = node.split("\\.");
		String wildNode = temp[0];
		for (int i = 1; i < (temp.length); i++) {
			wildNode = wildNode + "." + temp[i];

			if (player.isPermissionSet(wildNode + ".*"))
				// plugin.info("wildNode1 " + wildNode+".*");
				return player.hasPermission(wildNode + ".*");

		}
		if (player.isPermissionSet(wildNode))
			return player.hasPermission(wildNode);

		if (player.isPermissionSet(wildNode))
			return player.hasPermission(wildNode);

		return player.hasPermission(pluginName.toLowerCase() + ".*");
	}

	private String F(String string, Object... args) {
		return Localization.F(string, args);
	}

	private String L(String string) {
		return Localization.L(string);
	}

	HashMap<String, Integer> playerFollowersCache = new HashMap<String, Integer>();
	HashMap<String, String> playerMonarchCache = new HashMap<String, String>();
	HashMap<String, String> playerPatronCache = new HashMap<String, String>();
	HashMap<String, Boolean> existedPlayersCache = new HashMap<String, Boolean>();
	HashMap<String, Boolean> isVassalCache = new HashMap<String, Boolean>();
	HashMap<String, Boolean> isFollowerCache = new HashMap<String, Boolean>();
	HashMap<String, Integer> playerStanceCache = new HashMap<String, Integer>();
	HashMap<String, Integer> inheritStanceCache = new HashMap<String, Integer>();
	HashMap<String, Boolean> playerHasStanceCache = new HashMap<String, Boolean>();
	HashMap<String, ChatColor> relationshipColoursCache = new HashMap<String, ChatColor>();
	
	public void clearCaches() {
		existedPlayersCache.clear();
		events.hasAttacked.clear();
		database.clearSkillCache();
		clearAllegianceCaches();
	}

	public void clearStanceCaches() {
		playerStanceCache.clear();
		inheritStanceCache.clear();
		playerHasStanceCache.clear();
		relationshipColoursCache.clear();
		Provinces.permittedCache.clear();
	}

	public void clearAllegianceCaches() {
		playerMonarchCache.clear();
		playerFollowersCache.clear();
		playerPatronCache.clear();
		isVassalCache.clear();
		isFollowerCache.clear();
		clearStanceCaches();
		ranks.loadPermissions();
	}


	
	
	public boolean playerHasExisted(String playerName) {
		if (!existedPlayersCache .containsKey(playerName)) {
			String worldName = getServer().getWorlds().get(0).getName(); // p.getWorld().getName();
			existedPlayersCache.put(playerName, (new File(worldName + "/players/" + playerName + ".dat")).exists());
		}

		return existedPlayersCache .get(playerName);

	}

	public boolean hasStance(String playerName, String targetName) {
		String together = playerName + targetName;
		if (!playerHasStanceCache .containsKey(together)) {
			playerHasStanceCache .put(together, database.hasStance(playerName, targetName));
		}

		return playerHasStanceCache .get(together);
	}

	public static class stanceInfo {
		String playerName;
		String targetName;
		public int stanceID;
		
		public stanceInfo(String a, String b, int c) {
			this.playerName = a;
			this.targetName = b;
			this.stanceID = c;
		}
	}
	
	public stanceInfo getStance(String playerName, String targetName, boolean includePlayerStance) {
		int stance;
		String targetMonarch = getMonarch(targetName);
		if (includePlayerStance == true){
			stance = getDirectStance(playerName, targetName);
			if (stance != Config.defaultStance)
				return new stanceInfo(playerName, targetName, stance);

			if (!targetMonarch.equalsIgnoreCase(targetName)){
				stance = getDirectStance(playerName, targetMonarch);
				if (stance != Config.defaultStance)
					return new stanceInfo(playerName, targetMonarch, stance);
			}
		}
		
		String playerMonarch = getMonarch(playerName);
		if (!playerMonarch.equalsIgnoreCase(playerName)){
			stance = getDirectStance(playerMonarch, targetName);
			if (stance != Config.defaultStance)
				return new stanceInfo(playerMonarch, targetName, stance);
			
			if (!targetMonarch.equalsIgnoreCase(targetName)){
				stance = getDirectStance(playerMonarch, targetMonarch);
				if (stance != Config.defaultStance)
					return new stanceInfo(playerMonarch, targetMonarch, stance);
			}
			
		}
		
		
		return null;
	}
	
	public stanceInfo getStance(String playerName, String targetName) {
		return getStance(playerName, targetName, Config.userStanceOverridesTheirMonarch);
	}
	
	public int getDirectStance(String playerName, String targetName) {
		String together = playerName + targetName;
		if (!playerStanceCache.containsKey(together)) {
			playerStanceCache.put(together, database.getStance(playerName, targetName));
		}

		return playerStanceCache.get(together);
	}

	public int getInheritStance(String playerMonarch, String targetName, String targetMonarch) {
		String together = playerMonarch + targetName;
		if (!inheritStanceCache.containsKey(together)) {
			
			//String playerMonarch = getPlayersMonarch(playerName);
			//String targetMonarch = getPlayersMonarch(targetName);
			
			int stanceID = Config.defaultStance;

			if (hasStance(playerMonarch, targetName) == true) {
				stanceID = getDirectStance(playerMonarch, targetName);
			} else if (targetMonarch != null && hasStance(playerMonarch, targetMonarch)== true) {
				stanceID = getDirectStance(playerMonarch, targetMonarch);
			}

			inheritStanceCache.put(together, stanceID);
		}

		return inheritStanceCache.get(together);
	}
	

	public int getNumFollowers(String monarchName) {
		if (!playerFollowersCache.containsKey(monarchName)) {
			int followers = database.getNumFollowers(monarchName);
		//	info("getMonarchFollowers " + monarchName + " has " + followers + " follows.");

			playerFollowersCache.put(monarchName, followers);
		}

		return playerFollowersCache.get(monarchName);
	}

	public Boolean isFollower(String playerName, String targetName) {
		String together = playerName+targetName;
		
		if (!isFollowerCache.containsKey(together)) {
			isFollowerCache.put(together, database.isFollower(playerName, targetName));
		}
		return isFollowerCache.get(together);
		
	}
	
	//boolean isVassal = database.isVassal(player.getName(), vassalName);
	public Boolean isVassal(String playerName, String targetName) {
		String together = playerName+targetName;
		
		if (!isVassalCache.containsKey(together)) {
			isVassalCache.put(together, database.isVassal(playerName, targetName));
		}
		return isVassalCache.get(together);
	}
	
	public String getPatron(String playerName) {
		if (!playerPatronCache.containsKey(playerName)) {
			playerPatronCache.put(playerName, database.getPatron(playerName));
		}

		return playerPatronCache.get(playerName);
	}
	
	public String getMonarch(String playerName) {
		// String playerName = player.getName();
		String monarchName;
		if (!playerMonarchCache.containsKey(playerName)) {
			monarchName = database.getMonarch(playerName);
			// info("getPlayersMonarch: " + monarchName);
			if (monarchName == null)
				monarchName = playerName;

			playerMonarchCache.put(playerName, monarchName);
		}

		monarchName = playerMonarchCache.get(playerName);

		/*
		 * if (monarchName.equalsIgnoreCase(playerName)) { if
		 * (getPlayerFollowers(monarchName) == 0) return null; }
		 */

		return monarchName;
	}

	public String getPlayersPatron(Player player) {
		String playerName = player.getName();
		String patronName;
		if (!playerPatronCache.containsKey(playerName)) {
			patronName = getPatron(playerName);
			if (patronName == null)
				patronName = playerName;

			playerPatronCache.put(playerName, patronName);
		}

		patronName = playerPatronCache.get(playerName);

		if (patronName.equalsIgnoreCase(playerName))
			return null;

		return patronName;
	}

	List<Player> versionRequested = new ArrayList<Player>();

	public void queueVersionCheck(Player requester) {

		if (requester != null) {
			versionRequested.add(requester);
		}

		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				versionCheck();
			}
		}, 0L);// asap

	}

	public String getLatestVersion() {
		/* This function pulls the latest version from the dev.bukkit.org (Curse) website. 
			It's my belief this automated request doesn't violate the Curse Terms of Service (http://www.curse.com/terms). */

		String latestVersion = getDescription().getVersion();

		final String address = "http://dev.bukkit.org/server-mods/monarchy/files/";
		final URL url;
		URLConnection connection = null;
		BufferedReader bufferedReader = null;
		try {
			url = new URL(address.replace(" ", "%20"));
			connection = url.openConnection();

			connection.setConnectTimeout(8000);
			connection.setReadTimeout(15000);
			connection.setRequestProperty("User-agent", pluginName + getDescription().getVersion());

			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			// info("versionCheck1: " + bufferedReader.readLine());

			String str;
			Pattern titleFinder = Pattern.compile("<td[^>]*><a[^>]*>(.*?)</a></td>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher regexMatcher;
			while ((str = bufferedReader.readLine()) != null) {
				str = str.trim();

				
				regexMatcher = titleFinder.matcher(str);
				if (regexMatcher.find()) {
					// info("found match: "+regexMatcher.group(1));
					latestVersion = regexMatcher.group(1);
					break;
				}
			}

			bufferedReader.close();
			connection.getInputStream().close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return latestVersion;
	}

	public String latestVersion = null;

	public void versionCheck() {
		// "<td class=\"col-file\"><a href=\"/server-mods/monarchy/files/2-0-0-2/\">0.0.2</a></td>";

		if (latestVersion == null) {
			// double start = getUnixTime();
			latestVersion = getLatestVersion();
			// double end = getUnixTime();
			// info("Took " + (end - start) +
			// " seconds to get latest verison.");
		}

		String msg = null;
		String curVersion = getDescription().getVersion();
		if (latestVersion != null) {
			int compare = curVersion.compareTo(latestVersion);

			if (compare < 0) {
				msg = F("stVersionAvailable", ChatColor.RED + curVersion, ChatColor.GREEN + latestVersion);
			} else if (compare == 0) {
				msg = F("stVersion", ChatColor.GREEN + curVersion);
			} else {// newer than what's available, dev version?
				msg = F("stVersion", ChatColor.AQUA + curVersion);
			}
		} else {
			msg = F("stVersion", ChatColor.WHITE + latestVersion);
		}

		if (versionRequested.size() > 0) {
			for (int i = 0; i < versionRequested.size(); i++) {
				sendMessage(versionRequested.get(i), msg);
			}

			versionRequested.clear();
		} else {
			info(msg);

		}

	}

	public static double getUnixTime() {
		return (System.currentTimeMillis() / 1000D);
	}

	/*
	public String getColouredName(CommandSender sender) {
		// I have mChat set to colour tabbed names to match their rank colours.
		// This may not work for everyone else.
		String senderName = sender.getName();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			senderName = player.getDisplayName();
			

			
			if (Config.usemChatNames == true && mPlugin != null) {
				String world = player.getLocation().getWorld().toString();
				// senderName =
				// mPlugin.getAPI().ParseTabbedList(sender.getName(), world);
				senderName = mPlugin.getParser().parseTabbedList(sender.getName(), world);
			}
		}
		return senderName;
	}
*/
	
	public ChatColor getRelationshipColour(String playerMonarch, String targetName, String targetMonarch){
		String together = playerMonarch+targetName+targetMonarch;

		if (!relationshipColoursCache.containsKey(together)) {
			if (Config.colourNamesByRelationship == true){
				int stanceID = Config.defaultStance;
				
					if (targetMonarch != null){
						stanceID = getInheritStance(playerMonarch, targetName, targetMonarch);
					}else{
						stanceID = getDirectStance(playerMonarch, targetName);
					}
					
					//getInheritStance(playerMonarch, targetName, targetMonarch);
				relationshipColoursCache.put(together, Monarchy.stanceColours[stanceID]);
			}else{
				relationshipColoursCache.put(together, ChatColor.WHITE);
			}
		}
		 
		return relationshipColoursCache.get(together);
	}
	public ChatColor getRelationshipColour(String playerMonarch, String targetName){
		return getRelationshipColour(playerMonarch, targetName, null);
	}

	public String getDisplayName(CommandSender entity, String target, String targetMonarch) {
		String senderName = entity.getName();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			senderName = player.getDisplayName();
			
			if (Config.colourNamesByRelationship == true && target != null){
				String entityMonarch = getMonarch(entity.getName());
				senderName = getRelationshipColour(entityMonarch,  entity.getName(), targetMonarch).toString() + senderName;
			}

			if (Config.usemChatNames == true && mPlugin != null) {
				String world = player.getLocation().getWorld().toString();
				senderName = mPlugin.getParser().parsePlayerName(player.getName(), world);

			}
		}
		return senderName;
	}

	public String getDisplayName(CommandSender entity, String target) { //
		return getDisplayName(entity, target, null);
	}
	
	public String getDisplayName(CommandSender entity) { //
		return getDisplayName(entity, null);
	}
	
	public static String relationshipAntonym(String relationship) {
		if (relationship.equalsIgnoreCase("monarch"))
			return "Follower";
		if (relationship.equalsIgnoreCase("patron"))
			return "Vassal";
		if (relationship.equalsIgnoreCase("vassal"))
			return "Patron";
		if (relationship.equalsIgnoreCase("follower"))
			return "Kin"; // kindred

		return "unknown";
	}

	public Boolean canAttack(Player player, int stanceID) {
		boolean attack = false;

		switch (stanceID) {
		case 0:
			attack = hasPermission(player, "monarchy.canattack.monarchy");
			break;
		case 1:
			attack = hasPermission(player, "monarchy.canattack.allied");
			break;
		case 2:
			attack = hasPermission(player, "monarchy.canattack.friendly");
			break;
		case 3:
			attack = hasPermission(player, "monarchy.canattack.neutral");
			break;
		case 4:
			attack = hasPermission(player, "monarchy.canattack.wary");
			break;
		case 5:
			attack = hasPermission(player, "monarchy.canattack.hostile");
			break;
		}

		return attack;
	}

	public void tpToPlayer(CommandSender player, Player target) {
		String targetName = target.getName();

		if (commands.pendingRequests.containsKey(targetName)) {
			Double timeDiff = Monarchy.getUnixTime() - commands.pendingRequests.get(targetName).time;
			if (timeDiff < Config.interruptRequestDuration) {
				sendMessage(player, F("stPlayerHasPendingRequest", targetName, (Config.interruptRequestDuration - timeDiff.intValue())));
				return;
			}
		}

		sendMessage(player, F("stSendingTeleportRequest", targetName));
		sendMessage(target, F("stPlayerWantsToTp", getDisplayName(player)));
		sendMessage(target, F("stTypeAccept", L("stTypeAccept")));
		
		
		commands.pendingRequests.put(targetName, new requestInfo(1, player));

	}

	public void tpPlayerToYou(CommandSender player, Player target) {
		String targetName = target.getName();

		if (commands.pendingRequests.containsKey(targetName)) {
			Double timeDiff = Monarchy.getUnixTime() - commands.pendingRequests.get(targetName).time;
			if (timeDiff < Config.interruptRequestDuration) {
				sendMessage(player, F("stPlayerHasPendingRequest", targetName, (Config.interruptRequestDuration - timeDiff.intValue())));
				return;
			}
		}

		
		sendMessage(player, F("stSendingTeleportRequest", targetName));
		sendMessage(target, F("stPlayerWantsToTpHere", getDisplayName(player)));
		sendMessage(target, F("stTypeAccept", L("stTypeAccept")));
		
		commands.pendingRequests.put(targetName, new requestInfo(2, player));

	}

	public String getColouredName(String playerMonarch, String targetName, String targetMonarch){
		return getRelationshipColour(playerMonarch, targetName, targetMonarch).toString()+targetName+ChatColor.RESET.toString();
	}
	
	public String getColouredFullName(String playerMonarch, String targetName, String targetMonarch){
		if (targetName.equalsIgnoreCase(targetMonarch)){
			return getRelationshipColour(playerMonarch, targetName).toString()+targetName;
		}
		
		String c_name = getRelationshipColour(playerMonarch, targetName).toString()+targetName;
		String c_monarch = getRelationshipColour(playerMonarch, targetMonarch).toString()+targetMonarch;
		String reset = ChatColor.RESET.toString();
		return c_name + reset+ " ("+c_monarch+ reset+")";
	}
	
	
	public boolean hasCommandPermission(CommandSender player, String permission){
		if (hasPermission(player,permission)) {
			return true;
		}
		sendMessage(player, F("stNoPermission", permission));
		return false;
	}
	
	public static double Round(double Rval, int Rpl) {
		double p = (double) Math.pow(10, Rpl);
		Rval = Rval * p;
		double tmp = Math.round(Rval);
		return (double) tmp / p;
	}
	
	public int getLeadershipCost(double currentSkill){
		double diff = Monarchy.Round(currentSkill -Config.leadershipSkillDefault,2);
		diff = (diff / Config.leadershipSkillIncrease)+1;
		return (int) (diff*Config.leadershipSkillCost);
	}
	
	public int getLoyaltyCost(double currentSkill){
		double diff = Monarchy.Round(currentSkill -Config.loyaltySkillDefault,2);
		diff = (diff / Config.loyaltySkillIncrease)+1;
		return (int) (diff*Config.loyaltySkillCost);
	}

	public static int getTotalExperience(Player player){
		// player.getTotalExperience() sometimes reports the wrong XP due to a bug with enchanting not updating player's total XP.
		double userLevel = player.getLevel() + player.getExp();
		return (int) Math.ceil(1.75D * Math.pow(userLevel, 2.0D) + 5.0D * userLevel);
	}
	public static void setExp(Player player, int amount){
		//Clear player's XP.
		player.setTotalExperience(0);
		player.setLevel(0);
		player.setExp(0.0F);

		//Give player XP minus the enchant cost.
		player.giveExp(amount);
	}
	
	public static void takeExp(Player player, int amount){
		int current = getTotalExperience(player);
		setExp(player, (current - amount));
	}
	
    public static boolean hasMask(int flags, int mask) {
        return ((flags & mask) == mask);
    }
	
    public static int addMask(int flags, int mask){
    	return (flags |= mask);
    }
    public static int delMask(int flags, int mask){
    	return (flags &= ~mask);
    }
    public ChatColor colouredHasMask(int flags, int mask){
    	if (hasMask(flags, mask)){
    		return ChatColor.GREEN;
    	}
    	return ChatColor.RED;
    }
    
    
}