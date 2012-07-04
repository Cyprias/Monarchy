package com.cyprias.monarchy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Monarchy;

public class CommandPatron extends Commands {
	public Monarchy plugin;

	public CommandPatron(Monarchy plugin) {
		super("patron");
		this.plugin = plugin;
		// TODO Auto-generated constructor stub
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (!plugin.hasPermission(sender, "monarchy.patron")) {
			plugin.sendMessage(sender, F("stNoPermission", "monarchy.patron"));
			return true;
		}

		if (args.length == 0) {

			String patron = plugin.getPatron(sender.getName());
			if (patron == null) {
				plugin.sendMessage(sender, F("stPlayerNoPatron"));
			} else {
				plugin.sendMessage(sender, F("stYourPatron", patron, plugin.database.getPlayersPatronXP(sender.getName())));
			}

			plugin.sendMessage(sender, "§a/" + cmd.getName() + " help §8- " + F("stShowMoreCommands"));

			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			plugin.sendMessage(sender, "§a/" + cmd.getName() + " help §8- " + F("stYouArehere"));

			if (plugin.hasPermission(sender, "monarchy.patron.pledge"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " pledge <who> §8- " + F("stPledgeYourAllegiance"));

			if (plugin.hasPermission(sender, "monarchy.patron.dissolve"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " dissolve <who> §8- " + F("stDissolvePatronAllegiance"));

			if (plugin.hasPermission(sender, "monarchy.patron.followers"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " followers §8- " + F("stShowNumPatronFollowers"));

			if (plugin.hasPermission(sender, "monarchy.patron.tp"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " tp §8- " + F("stTeleportDesc", cmd.getName()));

			if (plugin.hasPermission(sender, "monarchy.patron.tphere"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " tphere §8- " + F("stTeleportHereDesc", cmd.getName()));

			return true;
		} else if (args[0].equalsIgnoreCase("tp") && args.length == 1) {
			if (!plugin.hasPermission(sender, "monarchy.patron.tp")) {
				plugin.sendMessage(sender, F("stNoPermission", "monarchy.patron.tp"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String patronName = plugin.getPatron(sender.getName());
			if (patronName == null) {
				plugin.sendMessage(sender, F("stPlayerNoPatron"));
				return true;
			}

			Player target = (Player) plugin.findPlayerByName(patronName);
			if (target == null) {
				plugin.sendMessage(sender, F("stTargetOffline", patronName));
				return true;
			}

			plugin.tpToPlayer((Player) sender, target);

			return true;

		} else if (args[0].equalsIgnoreCase("tphere") && args.length == 1) {
			if (!plugin.hasPermission(sender, "monarchy.patron.tphere")) {
				plugin.sendMessage(sender, F("stNoPermission", "monarchy.patron.tphere"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String patronName = plugin.getPatron(sender.getName());
			if (patronName == null) {
				plugin.sendMessage(sender, F("stPlayerNoPatron"));
				return true;
			}

			Player target = (Player) plugin.findPlayerByName(patronName);
			if (target == null) {
				plugin.sendMessage(sender, F("stTargetOffline", patronName));
				return true;
			}

			plugin.tpPlayerToYou((Player) sender, target);

			return true;

		} else if (args[0].equalsIgnoreCase("pledge") && args.length >= 2) {

			if (!plugin.hasPermission(sender, "monarchy.patron.pledge")) {
				plugin.sendMessage(sender, F("stNoPermission", "monarchy.patron.pledge"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			// plugin.playerPledgeAlliance((Player)sender, args[1]);

			String targetName = args[1];
			Player target = (Player) plugin.findPlayerByName(targetName);
			if (target == null) {
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}

			plugin.sendMessage(sender, F("stSendingPledgeRequest", args[1]));
			plugin.sendMessage(target, F("stPlayerWantsToPledge", plugin.getDisplayName((Player) sender)));
			plugin.sendMessage(target, F("stTypeAccept"));
			
			
			pendingRequests.put(targetName, new requestInfo(3, (Player) sender));

			return true;

		} else if (args[0].equalsIgnoreCase("dissolve") && args.length >= 2) {
			if (!plugin.hasPermission(sender, "monarchy.patron.dissolve")) {
				plugin.sendMessage(sender, F("stNoPermission", "monarchy.patron.dissolve"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			plugin.playerDissolveAlliance((Player) sender, args[1]);

			return true;

		} else if (args[0].equalsIgnoreCase("followers")) {
			if (!plugin.hasPermission(sender, "monarchy.patron.followers")) {
				plugin.sendMessage(sender, F("stNoPermission", "monarchy.patron.followers"));
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String patron = plugin.getPatron(sender.getName());
			if (patron == null) {
				plugin.sendMessage(sender, F("stPlayerNoPatron"));
				return true;
			}

			int followers = plugin.database.getNumFollowers(patron, false);
			plugin.sendMessage(sender, F("stPatronHasNumFollowers", patron, followers));
			return true;

		}
		return false;
	}
}
