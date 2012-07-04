package com.cyprias.monarchy.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.cyprias.monarchy.Config;
import com.cyprias.monarchy.Localization;
import com.cyprias.monarchy.Monarchy;
import com.cyprias.monarchy.Provinces;
import com.cyprias.monarchy.Monarchy.allegianceInfo;
import com.cyprias.monarchy.Monarchy.stanceInfo;
import com.cyprias.monarchy.Ranks.rankInfo;

// implements CommandExecutor
public class Commands implements CommandExecutor {
	public Monarchy plugin;

	public CommandMonarchy commandMonarchy;
	public CommandPatron commandPatron;
	public CommandVassal commandVassal;
	public CommandFollower commandFollower;
	public CommandMonarch commandMonarch;
	public CommandProvince commandProvince;
	
	public Commands(Monarchy plugin) {
		this.plugin = plugin;
		
		this.commandMonarchy = new CommandMonarchy(plugin);
		this.commandPatron = new CommandPatron(plugin);
		this.commandMonarch = new CommandMonarch(plugin);
		this.commandFollower = new CommandFollower(plugin);
		this.commandVassal = new CommandVassal(plugin);
		this.commandProvince = new CommandProvince(plugin);
		
		loadAliases(plugin);
	}
	
	public static HashMap<String, requestInfo> pendingRequests = new HashMap<String, requestInfo>();
	List<allegianceInfo> pledgedPlayers = new ArrayList<allegianceInfo>();
	
	
	public Commands(String string) {}

	protected String F(String string, Object... args) {
		return Localization.F(string, args);
	}

	protected String L(String string) {
		return Localization.L(string);
	}
	
	public static class requestInfo {
		/*
		 * 1 = TpTo
		 * 2 = TpHere
		 * 3 = pledge
		 * 4 = claim province
		 * 5 = transfer province
		 */
		int requestType;
		Object data;
		public Double time; 
		
		public requestInfo(int a, Object b ) {
			requestType = a;
			data = b;
			time = Monarchy.getUnixTime();
		}
	}

	
	/**/
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final String message = getFinalArg(args, 0);
		plugin.info(sender.getName() + ": /"+cmd.getName() + " " + message);
		return false;
	}
	
	
	
	public static String getFinalArg(final String[] args, final int start) {
		final StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	public static  HashMap<String, String> aliases = new HashMap<String, String>();
	
	public static void loadAliases(Monarchy plugin) {
		
		FileConfiguration cfgAliases = plugin.yml.getYMLConfig("aliases.yml"); //
		
		String value;
		ConfigurationSection info;
		for (String aliase : cfgAliases.getKeys(false)) {
			//info = cfgAliases.getConfigurationSection(aliase);
			//cfgAliases.add(new rankInfo(info.getString("title"), info.getInt("followers"), info.getList("permissions")));
			
			// info.getList("permissions")
			
			
			
			aliases.put(aliase, cfgAliases.getString(aliase));
		//	plugin.info("Adding " + aliase  + ": " + cfgAliases.getString(aliase));
			
			
		}
		
		/*
		for (int i = 0; i < rankList.size(); i++) {
			plugin.info(i+" rank: " + rankList.get(i).title + " = " +rankList.get(i).followers);
		}*/
		
	}

	public static int maskMonarchy=(int) Math.pow(2, 0);
	public static int maskPatron=(int) Math.pow(2, 1);
	public static int maskVassal=(int) Math.pow(2, 2);
	public static int maskFollower=(int) Math.pow(2, 3);
	public static int maskAllied=(int) Math.pow(2, 4);
	public static int maskFriendly=(int) Math.pow(2, 5);
	public static int maskNeutral=(int) Math.pow(2, 6);
	
	public boolean canTPToPlayer(CommandSender player, CommandSender target){
		String playerName = player.getName();
		String targetName = target.getName();
		
		
		
		int permitted = plugin.database.getTeleportPermit(targetName);
		
		String myMonarch = plugin.getMonarch(playerName);
		String theirMonarch = plugin.getMonarch(targetName);
		if (myMonarch.equalsIgnoreCase(theirMonarch) && plugin.hasMask(permitted, maskMonarchy)){
			return true;
		}
		String patronName = plugin.getPatron(targetName);
		if (patronName != null && patronName.equalsIgnoreCase(targetName) && plugin.hasMask(permitted, maskPatron)){
			return true;
		}
		
		boolean isVassal = plugin.isVassal(targetName, playerName);
		if (isVassal == true && plugin.hasMask(permitted, maskVassal)){
			return true;
		}
		
		boolean isFollower = plugin.isFollower(targetName, playerName);
		if (isFollower == true && plugin.hasMask(permitted, maskFollower)){
			return true;
		}
		
		//int stanceID = plugin.getInheritStance(theirMonarch, playerName, myMonarch);
		int stanceID = Config.defaultStance;
		 stanceInfo stance = plugin.getStance(targetName, playerName, true);
		
		if (stance != null){
			stanceID = stance.stanceID;
		}
		
		if (stanceID == 1 && plugin.hasMask(permitted, maskAllied)){
			return true;
		}
			
		if (stanceID == 2 && plugin.hasMask(permitted, maskFriendly)){
			return true;
		}
			
		if (stanceID == 3 && plugin.hasMask(permitted, maskNeutral)){
			return true;
		}
		
		return false;
	}
	
	
    public void toggleTeleportPermit(CommandSender sender, Command cmd, String[] args){
		if (!plugin.hasCommandPermission(sender, "monarchy.permit.teleport")) {
			return;
		}
    	
		int mask = 0;
		String playerName = sender.getName();
		int permitted = plugin.database.getTeleportPermit(playerName);
		
		if (args.length <= 2) {
			plugin.sendMessage(sender, "§a/" + cmd.getName() + " permit "+args[1]+" [choice]");
			plugin.sendMessage(sender, F("stPermitChoices", plugin.colouredHasMask(permitted, maskMonarchy).toString(),
				plugin.colouredHasMask(permitted, maskPatron).toString(), plugin.colouredHasMask(permitted, maskVassal)
					.toString(), plugin.colouredHasMask(permitted, maskFollower).toString(),
				plugin.colouredHasMask(permitted, maskAllied).toString(), plugin.colouredHasMask(permitted, maskFriendly)
					.toString(), plugin.colouredHasMask(permitted, maskNeutral).toString()));
			return;
		}
		
		String toggleChoice = args[2];
		
		if (toggleChoice.equalsIgnoreCase("monarchy"))
			mask = maskMonarchy;
		if (toggleChoice.equalsIgnoreCase("Patron"))
			mask = maskPatron;
		if (toggleChoice.equalsIgnoreCase("Vassal"))
			mask = maskVassal;
		if (toggleChoice.equalsIgnoreCase("Follower"))
			mask = maskFollower;
		if (toggleChoice.equalsIgnoreCase("Allied"))
			mask = maskAllied;
		if (toggleChoice.equalsIgnoreCase("Friendly"))
			mask = maskFriendly;
		if (toggleChoice.equalsIgnoreCase("Neutral"))
			mask = maskNeutral;


		
		if (mask == 0) {
			plugin.sendMessage(sender, F("stUnknownChoice", toggleChoice));
			plugin.sendMessage(
				sender,
				F("stPermitChoices", plugin.colouredHasMask(permitted, maskMonarchy).toString(),
					plugin.colouredHasMask(permitted, maskPatron).toString(), plugin.colouredHasMask(permitted, maskVassal)
						.toString(), plugin.colouredHasMask(permitted, maskFollower).toString(),
					plugin.colouredHasMask(permitted, maskAllied).toString(), plugin.colouredHasMask(permitted, maskFriendly)
						.toString(), plugin.colouredHasMask(permitted, maskNeutral).toString()));
			return;
		}
		
		if (!plugin.hasCommandPermission(sender, "monarchy.permit.teleport." + toggleChoice.toLowerCase())) {
			return;
		}
		
		Boolean allowed = false;
		if (plugin.hasMask(permitted, mask)) {
			permitted = plugin.delMask(permitted, mask);
		} else {
			permitted = plugin.addMask(permitted, mask);
			allowed = true;
		}
		
		if (plugin.database.setTeleportPermit(playerName, permitted) > 0) { // province.worldName, province.x, province.z,

			if (allowed == true) {
				plugin.sendMessage(sender, F("stPlayerCanNowTeleport", ChatColor.GREEN + toggleChoice));
			} else {
				plugin.sendMessage(sender, F("stPlayerCanNolongerTeleport", ChatColor.RED + toggleChoice));
			}
			return;
		}
		plugin.sendMessage(sender, F("stDBError"));
		
    }
}
