package com.cyprias.monarchy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Config;
import com.cyprias.monarchy.Monarchy;

public class CommandFollower extends Commands {
	public Monarchy plugin;

	public CommandFollower(Monarchy plugin) {
		super("follower");
		this.plugin = plugin;
		// TODO Auto-generated constructor stub
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		
		if (!plugin.hasPermission(sender, "monarchy.follower")){
			plugin.sendMessage(sender, F("stNoPermission",  "monarchy.follower"));
			return true;
		}
		
		
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stShowMoreCommands"));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("help")) {
			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stYouArehere"));
			
			if (plugin.hasPermission(sender, "monarchy.follower.check"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" check <who> §8- " + F("stCheckIfFollower"));
			
			if (plugin.hasPermission(sender, "monarchy.follower.followers"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" followers §8- " + F("stShowNumFollowers"));
			
			if (plugin.hasPermission(sender, "monarchy.follower.tp"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" tp <who> §8- " + F("stTeleportDesc", cmd.getName()));
			
			if (plugin.hasPermission(sender, "monarchy.follower.tphere"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" tphere <who> §8- " + F("stTeleportHereDesc", cmd.getName()));
			return true;
			


			
		}else if (args[0].equalsIgnoreCase("tp") && args.length == 2) {
			if (!plugin.hasPermission(sender, "monarchy.follower.tp")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.follower.tp"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			String myMonarch = plugin.getMonarch(sender.getName());
			
			if (!myMonarch.equalsIgnoreCase(sender.getName()) && Config.onlyMonarchCanTpFollower == true){
				plugin.sendMessage(sender, L("stYourNotMonarch"));
				return true;
			}
			
			String targetName = args[1];
			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null){
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}

			String targetMonarch = plugin.getMonarch(targetName);
			if (!myMonarch.equalsIgnoreCase(targetMonarch)){
				plugin.sendMessage(sender, F("stPlayerNotInMonarchy", args[1]));
				return true;
			}
			
			
			plugin.tpToPlayer((Player) sender, target);

			return true;
			

		}else if (args[0].equalsIgnoreCase("tphere") && args.length == 2) {
			if (!plugin.hasPermission(sender, "monarchy.follower.tphere")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.follower.tphere"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			
			String myMonarch = plugin.getMonarch(sender.getName());
			
			if (!myMonarch.equalsIgnoreCase(sender.getName()) && Config.onlyMonarchCanTpFollower == true){
				plugin.sendMessage(sender, L("stYourNotMonarch"));
				return true;
			}
			
			String targetName = args[1];
			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null){
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}

			String targetMonarch = plugin.getMonarch(targetName);
			if (!myMonarch.equalsIgnoreCase(targetMonarch)){
				plugin.sendMessage(sender, F("stPlayerNotInMonarchy", args[1]));
				return true;
			}
			
			plugin.tpPlayerToYou((Player) sender, target);

			return true;
			
			
		}else if (args[0].equalsIgnoreCase("check")) {
			if (!plugin.hasPermission(sender, "monarchy.follower.check")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.follower.check"));
				return true;
			}
			
			String myMonarch = plugin.getMonarch(sender.getName());
			if (myMonarch == null)
				myMonarch = sender.getName();
			
			Boolean isFollower = plugin.isFollower(myMonarch, args[1]);
			
			if (isFollower != null){
				plugin.sendMessage(sender, F("stPlayerIsYourFollower", args[1]));
			}else{
				plugin.sendMessage(sender, F("stPlayerIsNotYourFollower", args[1]));
			}
			
			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stShowMoreCommands"));
			return true;
			
		}else if (args[0].equalsIgnoreCase("followers")) {
			if (!plugin.hasPermission(sender, "monarchy.follower.followers")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.follower.followers"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			int followers =plugin.database.getNumFollowers(sender.getName(), false);
			plugin.sendMessage(sender, F("stYourNumFollowers", followers));

			return true;
			
		}
		return false;
	}
}
