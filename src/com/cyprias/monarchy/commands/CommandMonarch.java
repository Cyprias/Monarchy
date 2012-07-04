package com.cyprias.monarchy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Monarchy;

public class CommandMonarch  extends Commands {
	public Monarchy plugin;

	public CommandMonarch(Monarchy plugin) {
		super("monarch");
		this.plugin = plugin;
		// TODO Auto-generated constructor stub
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!plugin.hasPermission(sender, "monarchy.monarch")){
			plugin.sendMessage(sender, F("stNoPermission",  "monarchy.monarch"));
			return true;
		}
		
		if (args.length == 0) {
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String monarchName = plugin.getMonarch(sender.getName());
			if (monarchName != null){
				plugin.sendMessage(sender, F("stYourMonarchIs", monarchName));
			}else{
				plugin.sendMessage(sender, F("stNoMonarch"));
			}
				
			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stShowMoreCommands"));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("help")) {
			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stYouArehere"));
			
			if (plugin.hasPermission(sender, "monarchy.monarch.followers"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" followers §8- " + F("stShowNumMonarchFollowers"));
			
			if (plugin.hasPermission(sender, "monarchy.monarch.tp"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" tp §8- " + F("stTeleportDesc", cmd.getName()));
			
			if (plugin.hasPermission(sender, "monarchy.monarch.tphere"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" tphere §8- " + F("stTeleportHereDesc", cmd.getName()));
			
			return true;
		}else if (args[0].equalsIgnoreCase("tp") && args.length == 1) {
			if (!plugin.hasPermission(sender, "monarchy.monarch.tp")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.monarch.tp"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			String targetName = plugin.getMonarch(sender.getName());
			if (targetName == null){
				plugin.sendMessage(sender, F("stPlayerNoMonarch"));
				return true;
			}
			
			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null){
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}
			
			plugin.tpToPlayer((Player) sender, target);

			return true;
			
		}else if (args[0].equalsIgnoreCase("tphere") && args.length == 1) {
			if (!plugin.hasPermission(sender, "monarchy.monarch.tphere")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.monarch.tphere"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			String targetName = plugin.getMonarch(sender.getName());
			if (targetName == null){
				plugin.sendMessage(sender, F("stPlayerNoMonarch"));
				return true;
			}
			
			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null){
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}
			
			plugin.tpPlayerToYou((Player) sender, target);

			return true;
			
			
		}else if (args[0].equalsIgnoreCase("check")) {
			if (!plugin.hasPermission(sender, "monarchy.monarch.check")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.monarch.check"));
				return true;
			}
			
			String myMonarch = plugin.getMonarch(sender.getName());
			if (myMonarch == null)
				myMonarch = sender.getName();
			
			Boolean isFollower = plugin.isFollower(myMonarch, args[1]);
			
			if (myMonarch.equalsIgnoreCase(sender.getName())){
				if (isFollower != null){
					plugin.sendMessage(sender, F("stPlayerIsYourFollower", args[1], plugin.database.getFollowerRelationship(myMonarch, args[1])));
				}else{
					plugin.sendMessage(sender, F("stPlayerIsNotYourFollower", args[1]));
				}
			}else{
				
				if (isFollower != null){
					plugin.sendMessage(sender, F("stPlayerIsMonarchsFollower", args[1], myMonarch,  plugin.database.getFollowerRelationship(myMonarch, args[1])));
				}else{
					plugin.sendMessage(sender, F("stPlayerIsNotMonarchsFollower", args[1], myMonarch));
				}
				
			}
			return true;
			

			
		}else if (args[0].equalsIgnoreCase("followers")) {
			if (!plugin.hasPermission(sender, "monarchy.monarch.followers")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.monarch.followers"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String monarchName = plugin.getMonarch(sender.getName());
			if (monarchName == null){
				String patron = plugin.getPatron(sender.getName());
				
				if (patron != null){
					plugin.sendMessage(sender, F("stNoMonarchLoop"));
					return true;
				}
				monarchName = sender.getName();
				
			}
				
			int followers =plugin.database.getNumFollowers(monarchName);
			
			//if (monarchName.equalsIgnoreCase(sender.getName())){
			//	plugin.sendMessage(sender, F("stYourNumFollowers", followers));
			//}else{
				plugin.sendMessage(sender, F("stMonarchHasNumFollowers", monarchName, followers));
			//}
			return true;
			


		}
		return false;
	}
}
