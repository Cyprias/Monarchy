package com.cyprias.monarchy.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Config;
import com.cyprias.monarchy.Database;
import com.cyprias.monarchy.Database.homeInfo;
import com.cyprias.monarchy.Provinces.provinceInfo;
import com.cyprias.monarchy.commands.Commands.requestInfo;
import com.cyprias.monarchy.Homes;
import com.cyprias.monarchy.Monarchy;
import com.cyprias.monarchy.Provinces;
import com.cyprias.monarchy.Ranks;

public class CommandMonarchy extends Commands {

	public Monarchy plugin;

	public CommandMonarchy(Monarchy plugin) {
		super("monarchy");
		this.plugin = plugin;
		// TODO Auto-generated constructor stub
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final String message = getFinalArg(args, 0);
		plugin.info(sender.getName() + ": /" + cmd.getName() + " " + message);

		if (!plugin.hasPermission(sender, "monarchy")) {
			return true;
		}

		// TODO Auto-generated method stub
		if (args.length == 0) {
			plugin.sendMessage(sender, "§a/" + cmd.getName() + " help §8- " + F("stShowMoreCommands"));
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			plugin.sendMessage(sender, "§a/" + cmd.getName() + " help §8- " + F("stYouArehere"));

			if (plugin.hasPermission(sender, "monarchy.reload"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " reload §8- " + F("stReloadDesc"));

			if (plugin.hasPermission(sender, "monarchy.cache"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " cache §8- " + F("stCacheDesc"));

			if (plugin.hasPermission(sender, "monarchy.version"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " version §8- " + F("stVersionDesc"));

			if (plugin.hasPermission(sender, "monarchy.accept"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " accept §8- " + F("stAcceptDesc"));

			if (plugin.hasPermission(sender, "monarchy.stance"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " stance <who> §8- " + F("stStanceDesc"));

			if (plugin.hasPermission(sender, "monarchy.declare"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " declare <stance> <who> §8- " + F("stDeclareDesc"));

			if (plugin.hasPermission(sender, "monarchy.expunge"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " expunge <who> §8- " + F("stExpungeDesc"));

			if (plugin.hasPermission(sender, "monarchy.rank"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " rank [who] §8- " + F("stRankDesc"));

			if (plugin.hasPermission(sender, "monarchy.permit"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " permit [type] [choice] §8- " + F("stPermitDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.home"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " home [who] §8- " + F("stHomeDesc"));
			if (plugin.hasPermission(sender, "monarchy.sethome"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " sethome §8- " + F("stSetHomeDesc"));
			//if (plugin.hasPermission(sender, "monarchy.permithome"))
			//	plugin.sendMessage(sender, "§a/" + cmd.getName() + " permithome §8- " + F("stPermitHomeDesc"));

			if (plugin.hasPermission(sender, "monarchy.leadership"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " leadership §8- " + F("stLeadershipDesc"));

			if (plugin.hasPermission(sender, "monarchy.loyalty"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " loyalty §8- " + F("stLoyaltyDesc"));

			if (plugin.hasPermission(sender, "monarchy.province"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " province [claim] §8- " + F("stProvinceDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.exp"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " exp §8- " + F("stExpDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.commands"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " commands §8- " + F("stCommandsDesc"));

			if (plugin.hasPermission(sender, "monarchy.sendmessage"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " <message> §8- " + F("stMessageDesc"));

			return true;
		} else if (args[0].equalsIgnoreCase("commands") && args.length <= 1) {
			plugin.sendMessage(sender, "§a/monarch §8- " + F("stCMDMonarch"));
			plugin.sendMessage(sender, "§a/patron §8- " + F("stCMDPatron"));
			plugin.sendMessage(sender, "§a/vassal §8- " + F("stCMDVassal"));
			plugin.sendMessage(sender, "§a/follower §8- " + F("stCMDFollower"));

			return true;
		} else if (args[0].equalsIgnoreCase("killed") && args.length >= 4) {// Debugging
			if (!plugin.hasCommandPermission(sender, "monarchy.killed")) {
				return true;
			}

			// plugin.playerEarnedXP(args[1], Integer.valueOf(args[2]), false);

			plugin.events.FollowerKilledPlayer(args[1], args[2], args[3], args[4]);
			plugin.events.PlayerAttackedFollower(args[1], args[2], args[3], args[4]);

			return true;

		} else if (args[0].equalsIgnoreCase("forcepledge") && args.length >= 3) {
			if (!plugin.hasCommandPermission(sender, "monarchy.forcepledge")) {
				return true;
			}

			plugin.forcePledge(sender, args[1], args[2]);

			return true;
		} else if (args[0].equalsIgnoreCase("accept") && args.length == 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.accept")) {
				return true;
			}
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String playerName = sender.getName();
			//plugin.info("playerName: " + playerName);
			if (!Commands.pendingRequests.containsKey(playerName)) {
				plugin.sendMessage(sender, F("stNoPendingRequests"));
				return true;
			}

			Player target;

			Boolean success = false;
			plugin.info("requestType: " + pendingRequests.get(playerName).requestType);
			
			switch (pendingRequests.get(playerName).requestType) {
			case 1: // tp
				if (!plugin.hasCommandPermission(sender, "monarchy.accept.tp")) {
					break;
				}

				target = (Player) pendingRequests.get(playerName).data;
				
				
				if (target != null && target.isOnline()) {
					plugin.sendMessage(sender, F("stTeleportingPlayerToYou", plugin.getDisplayName(target)));
					plugin.sendMessage(target, F("stTeleportingToPlayer", plugin.getDisplayName(sender)));
					target.teleport((Player) sender);
					success = true;
				}
				break;
			case 2: // tphere
				if (!plugin.hasCommandPermission(sender, "monarchy.accept.tphere")) {
					break;
				}

				target = (Player) pendingRequests.get(playerName).data;
				if (target != null && target.isOnline()) {
					plugin.sendMessage(target, F("stTeleportingPlayerToYou", plugin.getDisplayName(sender)));
					plugin.sendMessage(sender, F("stTeleportingToPlayer", plugin.getDisplayName(target)));
					((Player) sender).teleport(target);
					success = true;
				}
				break;

			case 3: // pledge
				if (!plugin.hasCommandPermission(sender, "monarchy.accept.pledge")) {
					break;
				}
				target = (Player) pendingRequests.get(playerName).data;
				plugin.playerPledgeAlliance(target, sender.getName());
				success = true;
				break;
			/*
			case 4: // claim province
				if (!plugin.hasCommandPermission(sender, "monarchy.accept.claimprovince")) {
					break;
				}

				provinceLoc loc = (provinceLoc) pendingRequests.get(playerName).data;

				//Make sure no one's claimed it while we were waiting for an accept.
				Provinces.provinceInfo province = plugin.database.getProvince(loc.worldName, loc.x, loc.z);
				if (province != null){
					plugin.sendMessage(sender, L("stAlreadyOwnedProvince"));
					success = true;
					break;
				}
				
				Player player = (Player) sender;
				
				int currentXP = Monarchy.getTotalExperience(player);

				if (currentXP < Config.claimProvinceCost) {
					plugin.sendMessage(sender, L("stNotEnoughExp"));
					return true;
				}
				
				int adjustedXP = (currentXP - Config.claimProvinceCost);
				
				
				
				if (plugin.database.claimProvince(sender.getName(), loc.worldName, loc.x, loc.z) == 1){
					//province = plugin.database.getProvince(loc.worldName, loc.x, loc.z);
					Monarchy.setExp(player, adjustedXP);
					
					plugin.sendMessage(sender, L("stProvinceClaimSuccessful"));
					plugin.sendMessage(sender, "§a/" + cmd.getName() +" " + args[0] + " permit §8- " + F("stChunkPermitDesc"));
				}else{
					plugin.sendMessage(sender, F("stDBError"));
				}
				success = true;
			 	*/
			case 5: // transfer province
				if (!plugin.hasCommandPermission(sender, "monarchy.accept.transfer")) {
					break;
				}
				
				
				provinceInfo province = (provinceInfo) pendingRequests.get(playerName).data;
				
				CommandSender oldOwner = plugin.findPlayerByName(province.playerName);
				if (plugin.database.transferProvince(province.worldName, province.x, province.z, sender.getName()) > 0){
					plugin.sendMessage(sender,F("stNewProvinceOwner", sender.getName()));
					plugin.sendMessage(oldOwner,F("stNewProvinceOwner", sender.getName()));
					return true;
				}
				
				plugin.sendMessage(sender, F("stDBError"));
				plugin.sendMessage(oldOwner, F("stDBError"));
			}

			if (success == false)
				plugin.sendMessage(sender, L("stRequestFailed"));

			pendingRequests.remove(playerName);

			return true;
			// pendingRequests.put(patronName, new requestInfo("tp", (Player)
			// sender));

		} else if (args[0].equalsIgnoreCase("forcedissolve") && args.length >= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.forcedissolve")) {
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			plugin.forceDissolve(sender, args[1], args[2]);

			return true;

		} else if (args[0].equalsIgnoreCase("reload") && args.length == 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.reload")) {
				return true;
			}

			plugin.config.reloadOurConfig();
			plugin.localization.loadLocales();
			plugin.sendMessage(sender, F("stReloadedOurConfigs"));

			plugin.clearCaches();
			plugin.sendMessage(sender, F("stCachesCleared"));

			plugin.ranks.loadRanks();
			plugin.ranks.loadPermissions();
			plugin.sendMessage(sender, F("stRanksReloaded"));

			return true;
		} else if (args[0].equalsIgnoreCase("cache") && args.length == 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.cache")) {
				return true;
			}

			plugin.clearCaches();

			plugin.database.provinceIDCache.clear();
			plugin.database.provinceLocCache.clear();

			plugin.sendMessage(sender, F("stCachesCleared"));
			return true;

		} else if (args[0].equalsIgnoreCase("version") && args.length == 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.version")) {
				return true;
			}

			plugin.queueVersionCheck((Player) sender);
			return true;
		} else if (args[0].equalsIgnoreCase("debug") && args.length == 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.debug")) {
				return true;
			}

			
			int me = plugin.database.getHomePermit(sender.getName());
			plugin.info("------");
			int Bob = plugin.database.getHomePermit("Bob");

			plugin.info("me: " + me + ", Bob: " + Bob);
			
			
			return true;
			

		} else if (args[0].equalsIgnoreCase("permithome") && args.length == 1) {
			plugin.sendMessage(sender, F("stTryCommand", "monarchy permit home"));
			return true;
			
		} else if (args[0].equalsIgnoreCase("sethome") && args.length == 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.sethome")) {
				return true;
			}

			Player player = (Player) sender;
			Location loc = player.getLocation();

			String playerName = sender.getName();
			String worldName = player.getWorld().getName();
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			int yaw = (Math.round(loc.getYaw()) % 360);
			int pitch = (Math.round(loc.getPitch()) % 360);

			int saved = plugin.database.saveHome(playerName, worldName, x, y, z, yaw, pitch);
			plugin.info("saveHome: " + saved);
			// Needs user feedback.

			return true;

		} else if (args[0].equalsIgnoreCase("home") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.home.self")) {
				return true;
			}

			String senderName = sender.getName();
			if (args.length <= 1) {
				plugin.homes.recallToHome(sender, senderName);
				return true;
			}
			if (!plugin.hasCommandPermission(sender, "monarchy.home.other")) {
				return true;
			}

			String targetName = args[1];

			Player onlineTarget = plugin.findPlayerByName(targetName);
			if (onlineTarget == null) {
				if (!plugin.playerHasExisted(targetName)) {
					plugin.sendMessage(sender, F("stPlayerHasNotExisted", targetName));
					return true;
				}
			}

			// String monarchName = plugin.getMonarch(targetName);
			// if (monarchName.equalsIgnoreCase(senderName))
			// if (plugin.homes.recallToHome(sender, targetName,
			// Commands.maskMonarchy) == true) {return true;};
			String myMonarch = plugin.getMonarch(senderName);
			String theirMonarch = plugin.getMonarch(targetName);
			if (myMonarch.equalsIgnoreCase(theirMonarch))
				if (plugin.homes.recallToHome(sender, targetName, Commands.maskMonarchy) == true) {
					return true;
				}
			;

			String patronName = plugin.getPatron(targetName);
			if (patronName != null && patronName.equalsIgnoreCase(senderName))
				if (plugin.homes.recallToHome(sender, targetName, Commands.maskPatron) == true) {
					return true;
				}
			;

			boolean isVassal = plugin.isVassal(targetName, senderName);
			if (isVassal == true)
				if (plugin.homes.recallToHome(sender, targetName, Commands.maskVassal) == true) {
					return true;
				}
			;

			boolean isFollower = plugin.isFollower(targetName, senderName);
			if (isFollower == true)
				if (plugin.homes.recallToHome(sender, targetName, Commands.maskFollower) == true) {
					return true;
				}
			;

			int stanceID = plugin.getInheritStance(theirMonarch, senderName, myMonarch);
			if (stanceID == 1)
				if (plugin.homes.recallToHome(sender, targetName, Commands.maskAllied) == true) {
					return true;
				}
			;

			if (stanceID == 2)
				if (plugin.homes.recallToHome(sender, targetName, Commands.maskFriendly) == true) {
					return true;
				}
			;

			plugin.sendMessage(sender, F("stNoPermitToHome", targetName));

			return true;
		} else if (args[0].equalsIgnoreCase("declare") && args.length <= 3) {
			if (!plugin.hasCommandPermission(sender, "monarchy.declare")) {
				return true;
			}

			if (args.length <= 2) {
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " declare <stance> <who> §8- " + F("stDeclareDesc"));
				plugin.sendMessage(sender, F("stStances", L("stAllied"), L("stFriendly"), L("stNeutral"), L("stWary"), L("stHostile")));
				return true;
			}

			String targetName = args[2];

			String myMonarch = plugin.getMonarch(sender.getName());

			//if (myMonarch.equalsIgnoreCase(sender.getName())) {
				plugin.declareStance((Player) sender, myMonarch, targetName, args[1]);
			//} else {
			//	plugin.sendMessage(sender, F("stOnlyMonarchCanDeclare"));
			//}

			return true;

		} else if (args[0].equalsIgnoreCase("expunge")) {
			if (!plugin.hasCommandPermission(sender, "monarchy.expunge")) {
				return true;
			}
			if (args.length <= 1) {
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " expunge <who> §8- " + F("stExpungeDesc"));
				return true;
			}

			String targetName = args[1];

			String myMonarch = plugin.getMonarch(sender.getName());

			if (myMonarch.equalsIgnoreCase(sender.getName())) {
				plugin.expungeStance((Player) sender, myMonarch, targetName);
			} else {
				plugin.sendMessage(sender, F("stOnlyMonarchCanExpunge"));
			}

			return true;
		} else if (args[0].equalsIgnoreCase("tp") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.tp")) {
				return true;
			}
			if (args.length == 1) {
				plugin.sendMessage(sender, L("stMustIncludePlayerName"));
				return true;
			}
			String targetName = args[1];
			Player onlineTarget = plugin.findPlayerByName(targetName);
			if (onlineTarget == null) {
				if (!plugin.playerHasExisted(targetName)) {
					plugin.sendMessage(sender, F("stPlayerHasNotExisted", targetName));
					return true;
				}
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}else{
				targetName = onlineTarget.getName();
			}

			boolean canTP = plugin.commands.canTPToPlayer(sender, onlineTarget);
			
			if (canTP == false){
				plugin.sendMessage(sender, F("stCannotSendRequest", args[0], targetName));
				return true;
			}

			plugin.tpToPlayer(sender, onlineTarget);
			return true;
		} else if (args[0].equalsIgnoreCase("tphere") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.tphere")) {
				return true;
			}
			if (args.length == 1) {
				plugin.sendMessage(sender, L("stMustIncludePlayerName"));
				return true;
			}
			String targetName = args[1];
			Player onlineTarget = plugin.findPlayerByName(targetName);
			if (onlineTarget == null) {
				if (!plugin.playerHasExisted(targetName)) {
					plugin.sendMessage(sender, F("stPlayerHasNotExisted", targetName));
					return true;
				}
				plugin.sendMessage(sender, F("stTargetOffline", targetName));
				return true;
			}else{
				targetName = onlineTarget.getName();
			}
			
			boolean canTP = plugin.commands.canTPToPlayer(sender, onlineTarget);
			
			if (canTP == false){
				plugin.sendMessage(sender, F("stCannotSendRequest", args[0], targetName));
				return true;
			}

			plugin.tpPlayerToYou(sender, onlineTarget);
			return true;
		} else if (args[0].equalsIgnoreCase("permit") && args.length <= 3) {
			if (!plugin.hasCommandPermission(sender, "monarchy.permit")) {
				return true;
			}

			if (args.length <= 1) {
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " permit [type] [choice]");
				plugin.sendMessage(sender, L("stPermitTypes"));
				return true;
			}
			
			if (args[1].equalsIgnoreCase("home")){
				plugin.homes.toggleHomePermit(sender, cmd, args);
			}else if (args[1].equalsIgnoreCase("province")){
				plugin.provinces.toggleProvincePermit(sender, cmd, args);
			}else if (args[1].equalsIgnoreCase("teleport")){
				plugin.commands.toggleTeleportPermit(sender, cmd, args);
			}else{
				plugin.sendMessage(sender, F("stUnknownType", args[1]));
				plugin.sendMessage(sender, L("stPermitTypes"));
			}
			
			return true;
			
			
		} else if (args[0].equalsIgnoreCase("rank") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.rank")) {
				return true;
			}

			String targetName = sender.getName();

			if (args.length > 1) {

				Player onlineTarget = plugin.findPlayerByName(targetName);
				if (onlineTarget == null) {
					if (!plugin.playerHasExisted(targetName)) {
						plugin.sendMessage(sender, F("stPlayerHasNotExisted", targetName));
						return true;
					}
				}
			}

			int followers = plugin.getNumFollowers(targetName);
			Ranks.rankInfo rank = plugin.ranks.getRank(followers);

			plugin.sendMessage(sender, F("stPlayerRank", targetName, rank.title, followers));

			return true;

		} else if (args[0].equalsIgnoreCase("loyalty") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.loyalty")) {
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String targetName = sender.getName();

			Double skill = plugin.database.getLoyalty(targetName);

			int skillCost = plugin.getLoyaltyCost(skill);

			if (args.length == 1) {
				plugin.sendMessage(sender, F("stYourLoyaltySkill", Math.round(skill * 100), Math.round(Config.loyaltySkillIncrease * 100), skillCost));

				if (plugin.hasCommandPermission(sender, "monarchy.loyalty.up"))
					plugin.sendMessage(sender, F("stIncreaseSkillCommand", cmd.getName() + " " + args[0], args[0]));
				return true;
			}

			if (!args[1].equalsIgnoreCase("up")) {
				plugin.sendMessage(sender, F("stUnknownChoice", args[1]));
				return true;
			}

			if (!plugin.hasCommandPermission(sender, "monarchy.loyalty.up")) {
				return true;
			}

			if (skill >= Config.loyaltySkillCap) {// 100%
				plugin.sendMessage(sender, L("stYouveReachedLoyaltyCap"));
				return true;
			}

			Player player = (Player) sender;

			int currentXP = Monarchy.getTotalExperience(player);
			int adjustedXP = (currentXP - skillCost);

			if (currentXP < skillCost) {
				plugin.sendMessage(sender, L("stNotEnoughExp"));
				return true;
			}

			double newSkill = skill + Config.loyaltySkillIncrease;
			if (plugin.database.setLoyalty(targetName, newSkill) == 1) {
				Monarchy.setExp(player, adjustedXP);
				double nextSkill = newSkill + Config.loyaltySkillIncrease;
				plugin.sendMessage(sender,
					F("stLoyaltySkillIncreased", Math.round(newSkill * 100), Math.round(Config.loyaltySkillIncrease * 100), plugin.getLoyaltyCost(nextSkill)));
				return true;
			}

			plugin.sendMessage(sender, F("stDBError"));
			return true;

		} else if (args[0].equalsIgnoreCase("leadership") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.leadership")) {
				return true;
			}

			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}

			String targetName = sender.getName();

			Double skill = plugin.database.getLeadership(targetName);

			int skillCost = plugin.getLeadershipCost(skill);

			if (args.length == 1) {
				plugin.sendMessage(sender, F("stYourLeadershipSkill", Math.round(skill * 100), Math.round(Config.leadershipSkillIncrease * 100), skillCost));

				if (plugin.hasCommandPermission(sender, "monarchy.leadership.up"))
					plugin.sendMessage(sender, F("stIncreaseSkillCommand", cmd.getName() + " " + args[0], args[0]));
				return true;
			}

			if (!args[1].equalsIgnoreCase("up")) {
				plugin.sendMessage(sender, F("stUnknownChoice", args[1]));
				return true;
			}

			if (!plugin.hasCommandPermission(sender, "monarchy.leadership.up")) {
				return true;
			}

			if (skill >= Config.leadershipSkillCap) {// 100%
				plugin.sendMessage(sender, L("stYouveReachedLeadershipCap"));
				return true;
			}

			Player player = (Player) sender;

			int currentXP = Monarchy.getTotalExperience(player);
			int adjustedXP = (currentXP - skillCost);

			if (currentXP < skillCost) {
				plugin.sendMessage(sender, L("stNotEnoughExp"));
				return true;
			}

			double newSkill = skill + Config.leadershipSkillIncrease;
			if (plugin.database.setLeadership(targetName, newSkill) == 1) {
				Monarchy.setExp(player, adjustedXP);
				double nextSkill = newSkill + Config.leadershipSkillIncrease;
				plugin.sendMessage(
					sender,
					F("stLeadershipSkillIncreased", Math.round(newSkill * 100), Math.round(Config.leadershipSkillIncrease * 100),
						plugin.getLeadershipCost(nextSkill)));
				return true;
			}

			plugin.sendMessage(sender, F("stDBError"));
			return true;
		} else if (args[0].equalsIgnoreCase("stance") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.stance")) {
				return true;
			}

			if (args.length == 1) {
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " stance §8- " + F("stStanceDesc"));
				return true;
			}

			String targetName = args[1];

			Player onlineTarget = plugin.findPlayerByName(targetName);
			if (onlineTarget == null) {
				if (!plugin.playerHasExisted(targetName)) {
					plugin.sendMessage(sender, F("stPlayerHasNotExisted", targetName));
					return true;
				}

				// plugin.sendMessage(sender, F("stCannotFindPlayer",
				// targetName));
				// return true;
			}

			String playerName = sender.getName();

			String myMonarch = plugin.getMonarch(playerName);
			// if (myMonarch == null)
			// myMonarch = playerName;

			String theirMonarch = plugin.getMonarch(targetName);
			// if (theirMonarch == null)
			// theirMonarch = targetName;

			int stanceID = plugin.getInheritStance(myMonarch, targetName, theirMonarch);

			String c_victimName = plugin.getColouredFullName(myMonarch, targetName, theirMonarch);

			if (myMonarch.equalsIgnoreCase(playerName)) {
				plugin.sendMessage(sender, F("stYoureStanceWith", Monarchy.stanceColours[stanceID].toString() + Monarchy.stanceNames[stanceID], c_victimName));
			} else {
				String c_attackerMonarch = Monarchy.stanceColours[0].toString() + myMonarch;
				plugin.sendMessage(sender,
					F("stPlayerStanceWith", c_attackerMonarch, Monarchy.stanceColours[stanceID].toString() + Monarchy.stanceNames[stanceID], c_victimName));
			}

			return true;
		} else if (args[0].equalsIgnoreCase("exp") && args.length <= 1) {
			if (!plugin.hasCommandPermission(sender, "monarchy.exp")) {
				return true;
			}
			if (!(sender instanceof Player)) {
				plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
				return true;
			}
			
			int currentXP = Monarchy.getTotalExperience((Player) sender);
			plugin.sendMessage(sender, F("stYouHaveExp", currentXP));
			
			return true;

		}
		// notifyMonarchy(temp.get(i).monarchName, F("stPledgeNofify",
		// temp.get(i).playerName, temp.get(i).patronName,
		// temp.get(i).monarchName), "monarchy.notify.followerleave");

		if (!plugin.hasCommandPermission(sender, "monarchy.sendmessage")) {
			return true;
		}
		if (!(sender instanceof Player)) {
			plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
			return true;
		}

		String myMonarch = plugin.getMonarch(sender.getName());

		plugin.notifyMonarchy(myMonarch, Monarchy.chatPrefix + F("stMessageFormat", plugin.getDisplayName(sender), message), "monarchy.receivemessage");

		return true;
	}

	public static class provinceLoc {
		public String worldName;
		public int x;
		public int z;
		public provinceLoc(String worldName, int x, int z) {
			this.worldName = worldName;
			this.x = x;
			this.z = z;
		}
	}
}
