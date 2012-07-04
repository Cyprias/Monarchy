package com.cyprias.monarchy.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Database;
import com.cyprias.monarchy.Monarchy;

public class CommandVassal extends Commands {
	public Monarchy plugin;

	public CommandVassal(Monarchy plugin) {
		super("vassal");
		this.plugin = plugin;
		// TODO Auto-generated constructor stub
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if (!plugin.hasPermission(sender, "monarchy.vassal")){
			plugin.sendMessage(sender, F("stNoPermission",  "monarchy.vassal"));
			return true;
		}
		
		
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			plugin.listPlayersVassals((Player)sender);
			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stShowMoreCommands"));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("help")) {
			plugin.sendMessage(sender, "§a/"+cmd.getName()+" help §8- " + F("stYouArehere"));
			
			if (plugin.hasPermission(sender, "monarchy.vassal.dissolve"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" dissolve <who> §8- " + F("stDissolveVassalAllegiance"));
			
			if (plugin.hasPermission(sender, "monarchy.vassal.tp"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" tp <who> §8- " + F("stTeleportDesc", cmd.getName()));
			
			if (plugin.hasPermission(sender, "monarchy.vassal.tphere"))
				plugin.sendMessage(sender, "§a/"+cmd.getName()+" tphere <who> §8- " + F("stTeleportHereDesc", cmd.getName()));
			
			return true;
		}else if (args[0].equalsIgnoreCase("dissolve") && args.length >= 2) {
			if (!plugin.hasPermission(sender, "monarchy.vassal.dissolve")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.vassal.dissolve"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			plugin.playerDissolveVassalAlliance((Player)sender, args[1]);

			return true;

			
		}else if (args[0].equalsIgnoreCase("tp") && args.length == 2) {
			if (!plugin.hasPermission(sender, "monarchy.vassal.tp")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.vassal.tp"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			List<Database.vassalInfo> vassals = plugin.database.getPlayerVassals(sender.getName());
			
			if (vassals.size() == 0) {
				plugin.sendMessage(sender, L("stListNoVassals"));
				return true;
			}

			String targetName = null;
			for (int i = 0; i < vassals.size(); i++) {
				
				if (vassals.get(i).vassalName.equalsIgnoreCase(args[1])){
					targetName = vassals.get(i).vassalName;
					break;
				}
			}
			if (targetName == null){
				plugin.sendMessage(sender, F("stCannotFindPlayer", args[1]));
				return true;
			}

			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null){
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}
			
			plugin.tpToPlayer((Player) sender, target);

			return true;
			

		}else if (args[0].equalsIgnoreCase("tphere") && args.length == 2) {
			if (!plugin.hasPermission(sender, "monarchy.vassal.tphere")){
				plugin.sendMessage(sender, F("stNoPermission",  "monarchy.vassal.tphere"));
				return true;
			}
			
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			List<Database.vassalInfo> vassals = plugin.database.getPlayerVassals(sender.getName());
			
			if (vassals.size() == 0) {
				plugin.sendMessage(sender, L("stListNoVassals"));
				return true;
			}
			
			String targetName = null;
			for (int i = 0; i < vassals.size(); i++) {
				if (vassals.get(i).vassalName.equalsIgnoreCase(args[1])){
					targetName = vassals.get(i).vassalName;
					break;
				}
			}
			if (targetName == null){
				plugin.sendMessage(sender, F("stCannotFindPlayer", args[1]));
				return true;
			}
			
			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null){
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}
			
			plugin.tpPlayerToYou((Player) sender, target);

			return true;
		}
		
		return false;
	}
}
