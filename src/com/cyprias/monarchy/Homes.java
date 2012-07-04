package com.cyprias.monarchy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.commands.Commands;

public class Homes {
	private Monarchy plugin;
	

	public Homes(Monarchy monarchy) {
		plugin = monarchy;
	}

	private String F(String string, Object... args) {
		return Localization.F(string, args);
	}
	
   
    public Boolean recallToHome(CommandSender sender, String targetName, int permitMask){
    	
		Database.homeInfo home = plugin.database.getHome(targetName);
		if (home != null){

			if (permitMask > -1){
				if (!plugin.hasMask(home.permitted, permitMask)){
				//	plugin.sendMessage(sender, targetName + " has not given you permission to enter their home.");
					return false;
				}
			}
			
			
			Location loc = new Location(plugin.getServer().getWorld(home.worldName), home.x, home.y, home.z, home.yaw, home.pitch);
			//loc.setX(home.x);
			
			Player player = (Player) sender;
			player.teleport(loc);
			plugin.sendMessage(sender, F("stRecalledToTargetsHome", targetName));
			return true;
		}
    	
    	return false;
    }
    public Boolean recallToHome(CommandSender sender, String targetName){
    	return recallToHome(sender, targetName, -1);
    }
    

    public void toggleHomePermit(CommandSender sender, Command cmd, String[] args){
		if (!plugin.hasCommandPermission(sender, "monarchy.permit.home")) {
			return;
		}
    	
		int mask = 0;
		String playerName = sender.getName();
		int permitted = plugin.database.getHomePermit(playerName);
		
		if (args.length <= 2) {
			plugin.sendMessage(sender, "§a/" + cmd.getName() + " permit "+args[1]+" [choice]");
			plugin.sendMessage(sender, F("stPermitChoices", plugin.colouredHasMask(permitted, Commands.maskMonarchy).toString(),
				plugin.colouredHasMask(permitted, Commands.maskPatron).toString(), plugin.colouredHasMask(permitted, Commands.maskVassal)
					.toString(), plugin.colouredHasMask(permitted, Commands.maskFollower).toString(),
				plugin.colouredHasMask(permitted, Commands.maskAllied).toString(), plugin.colouredHasMask(permitted, Commands.maskFriendly)
					.toString(), plugin.colouredHasMask(permitted, Commands.maskNeutral).toString()));
			return;
		}
		
		String toggleChoice = args[2];
		
		if (toggleChoice.equalsIgnoreCase("monarchy"))
			mask = Commands.maskMonarchy;
		if (toggleChoice.equalsIgnoreCase("Patron"))
			mask = Commands.maskPatron;
		if (toggleChoice.equalsIgnoreCase("Vassal"))
			mask = Commands.maskVassal;
		if (toggleChoice.equalsIgnoreCase("Follower"))
			mask = Commands.maskFollower;
		if (toggleChoice.equalsIgnoreCase("Allied"))
			mask = Commands.maskAllied;
		if (toggleChoice.equalsIgnoreCase("Friendly"))
			mask = Commands.maskFriendly;
		if (toggleChoice.equalsIgnoreCase("Neutral"))
			mask = Commands.maskNeutral;


		
		if (mask == 0) {
			plugin.sendMessage(sender, F("stUnknownChoice", toggleChoice));
			plugin.sendMessage(
				sender,
				F("stPermitChoices", plugin.colouredHasMask(permitted, Commands.maskMonarchy).toString(),
					plugin.colouredHasMask(permitted, Commands.maskPatron).toString(), plugin.colouredHasMask(permitted, Commands.maskVassal)
						.toString(), plugin.colouredHasMask(permitted, Commands.maskFollower).toString(),
					plugin.colouredHasMask(permitted, Commands.maskAllied).toString(), plugin.colouredHasMask(permitted, Commands.maskFriendly)
						.toString(), plugin.colouredHasMask(permitted, Commands.maskNeutral).toString()));
			return;
		}
		
		if (!plugin.hasCommandPermission(sender, "monarchy.permit.home." + toggleChoice.toLowerCase())) {
			return;
		}
		
		Boolean allowed = false;
		if (plugin.hasMask(permitted, mask)) {
			permitted = plugin.delMask(permitted, mask);
		} else {
			permitted = plugin.addMask(permitted, mask);
			allowed = true;
		}
		
		if (plugin.database.setHomePermit(playerName, permitted) > 0) { // province.worldName, province.x, province.z,

			if (allowed == true) {
				plugin.sendMessage(sender, F("stPlayerCanNowRecallHome", ChatColor.GREEN + toggleChoice));
			} else {
				plugin.sendMessage(sender, F("stPlayerCanNolongerRecallHome", ChatColor.RED + toggleChoice));
			}
			return;
		}
		plugin.sendMessage(sender, F("stDBError"));
		
    }
    
}
