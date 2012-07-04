package com.cyprias.monarchy.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Config;
import com.cyprias.monarchy.Monarchy;
import com.cyprias.monarchy.Provinces;
import com.cyprias.monarchy.commands.Commands.requestInfo;

public class CommandProvince extends Commands {

	public CommandProvince(Monarchy plugin) {
		super("province");
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!plugin.hasPermission(sender, "monarchy.province")) {
			plugin.sendMessage(sender, F("stNoPermission", "monarchy.province"));
			return true;
		}
		if (!(sender instanceof Player)) {
			plugin.sendMessage(sender, F("stCommandRequiresPlayer"));
			return true;
		}
		
		if (args.length == 0) {
			//plugin.sendMessage(sender, "§a/" + cmd.getName() + " help §8- " + F("stShowMoreCommands"));
			
			if (plugin.hasPermission(sender, "monarchy.province.check"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " check §8- " + F("stProvinceCheckDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.province.claim"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " claim §8- " + F("stProvinceClaimDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.province.permit"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " permit §8- " + F("stChunkPermitDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.province.unclaim"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " unclaim §8- " + F("stProvinceUnclaimDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.province.transfer"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " transfer [who] §8- " + F("stProvinceTranasferDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.province.count"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " count [who] §8- " + F("stProvinceCountDesc"));
			
			if (plugin.hasPermission(sender, "monarchy.province.rent"))
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " rent [pay] §8- " + F("stProvinceRentDesc"));
			
			return true;
		}
		Player player = (Player) sender;
		String playerName = sender.getName();
		Location loc = player.getLocation();
		String worldName = loc.getWorld().getName();
		int chunkX = loc.getChunk().getX();
		int chunkZ = loc.getChunk().getZ();
		Provinces.provinceInfo province = plugin.database.getProvince(worldName, chunkX, chunkZ);
		

		if (args[0].equalsIgnoreCase("check") && args.length == 1) {
			if (province == null) {
				plugin.sendMessage(sender, L("stNoChunkOwnership"));
				plugin.sendMessage(sender, F("stTypeProvinceClaim", Config.claimProvinceCost, cmd.getName()));
				return true;
			}
			String myMonarch = plugin.getMonarch(playerName);
			String theirMonarch = plugin.getMonarch(province.playerName);
			String c_targetName = plugin.getColouredFullName(myMonarch, province.playerName, theirMonarch);
			plugin.sendMessage(sender, F("stChunkOwnedBy", c_targetName));
			return true;
			
		}else if (args[0].equalsIgnoreCase("claim") && args.length == 1) {
			if (province != null){
				plugin.sendMessage(sender, L("stAlreadyOwnedProvince"));
				return true;
			}
			
			if (!plugin.hasCommandPermission(sender, "monarchy.province.claim")) {
				return true;
			}

			int currentXP = Monarchy.getTotalExperience(player);

			if (currentXP < Config.claimProvinceCost) {
				plugin.sendMessage(sender, L("stNotEnoughExp"));
				return true;
			}

			if (plugin.database.claimProvince(sender.getName(), worldName, chunkX, chunkZ) > 0) {
				// province = plugin.database.getProvince(loc.worldName,
				// loc.x, loc.z);
				int adjustedXP = (currentXP - Config.claimProvinceCost);
				Monarchy.setExp(player, adjustedXP);

				plugin.sendMessage(sender, L("stProvinceClaimSuccessful"));
				plugin.sendMessage(sender, "§a/" + cmd.getName() + " permit §8- " + F("stChunkPermitDesc"));
				return true;
			}
			plugin.sendMessage(sender, F("stDBError"));
			return true;
			
		}else if (args[0].equalsIgnoreCase("unclaim") && args.length == 1) {
			
			if (!plugin.hasCommandPermission(sender, "monarchy.province.unclaim")) {
				return true;
			}
			
			if (province == null) {
				plugin.sendMessage(sender, L("stNoChunkOwnership"));
				return true;
			}
			
			if (!plugin.hasPermission(sender, "monarchy.province.unclaim.override")) {
				if (!province.playerName.equalsIgnoreCase(playerName)) {
					plugin.sendMessage(sender, F("stNotTheOwner"));
					return true;
				}
			}
			
			if (plugin.database.deleteProvince(province.worldName, province.x, province.z) > 0){
				plugin.sendMessage(sender, F("stUnclaimedProvince"));
				return true;
			}
			plugin.sendMessage(sender, F("stDBError"));
			
			return true;
		}else if (args[0].equalsIgnoreCase("count") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.province.count")) {
				return true;
			}
			
			String targetName = playerName;
			if (args.length > 1){
				if (!plugin.hasCommandPermission(sender, "monarchy.province.count.other")) {
					return true;
				}
				targetName = args[1];
				
				Player onlineTarget = plugin.findPlayerByName(targetName);
				if (onlineTarget == null) {
					if (!plugin.playerHasExisted(targetName)) {
						plugin.sendMessage(player, F("stPlayerHasNotExisted", targetName));
						return true;
					}
				}
			}
			int count = plugin.database.getProvinceCount(targetName);
			plugin.sendMessage(sender, F("stProvinceCount", targetName, count));
			
			
			return true;
		}else if (args[0].equalsIgnoreCase("rent") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.province.rent")) {
				return true;
			}

			if (args.length == 1){
				int count = plugin.database.getProvinceCount(playerName);
				plugin.sendMessage(sender, F("stProvinceRentInfo", count, (count*Config.provinceRentFee), Config.provinceRentTime));
			}else if (args.length == 2 && args[1].equalsIgnoreCase("pay")){
				if (Config.chargeProvinceRent == true){
					plugin.database.chargeProvinceRent(player);
				}else{
					plugin.sendMessage(sender,L("stProvinceRentNotEnabled"));
				}
			}else{
				final String message = getFinalArg(args, 0);
				plugin.sendMessage(sender,F("stUnknownCommand", cmd.getName() + " " + message));
			}
			
			
			
			
			
			return true;
		}else if (args[0].equalsIgnoreCase("transfer") && args.length <= 2) {
			if (!plugin.hasCommandPermission(sender, "monarchy.province.tranasfer")) {
				return true;
			}

			if (province == null) {
				plugin.sendMessage(sender, L("stNoChunkOwnership"));
				plugin.sendMessage(sender, F("stTypeProvinceClaim", Config.claimProvinceCost, cmd.getName()));
				return true;
			}
			
			if (!plugin.hasPermission(sender, "monarchy.province.tranasfer.override")) {
				if (!province.playerName.equalsIgnoreCase(playerName)) {
					plugin.sendMessage(sender, F("stNotTheOwner"));
					return true;
				}
			}
			
			if (args.length == 1){
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
			
			//plugin.info("targetName: " + targetName);
			Commands.pendingRequests.put(targetName, new requestInfo(5, province));
			
			
			
			plugin.sendMessage(sender, F("stSendingTransferRequest", targetName));
			plugin.sendMessage(onlineTarget, F("stPlayerWantsToTransferProvince", plugin.getDisplayName((Player) sender), province.worldName, province.x*16, province.z*16));
			plugin.sendMessage(onlineTarget, F("stTypeAccept"));
			
			//if (plugin.database.transferProvince(province.worldName, province.x, province.z, targetName) > 0){
			//	plugin.sendMessage(sender,F("stNewProvinceOwner", targetName));
			//	return true;
			//}
			

			
			
			return true;
		}else if (args[0].equalsIgnoreCase("permit") && args.length <= 2) {
			plugin.sendMessage(sender, F("stTryCommand", "monarchy permit province"));
			return true;
			/*
			if (!plugin.hasCommandPermission(sender, "monarchy.province.permit")) {
				return true;
			}
			if (!plugin.hasPermission(sender, "monarchy.province.permit.override")) {
				if (!province.playerName.equalsIgnoreCase(playerName)) {
					plugin.sendMessage(sender, F("stNotTheOwner"));
					return true;
				}
			}
			
			
			int permitted = plugin.provinces.getProvincePermitted(playerName);

			//plugin.info("Permitted: " + province.permitted + ", master permit: " + permitted);

			if (args.length == 1) {
				// add colours to these choices...

				plugin.sendMessage(sender, F("stProvincePermitUsage", cmd.getName()));

				plugin.sendMessage(
					sender,
					F("stPermitProvinceChoices", plugin.colouredHasMask(permitted, Commands.maskMonarchy).toString(),
						plugin.colouredHasMask(permitted, Commands.maskPatron).toString(), plugin.colouredHasMask(permitted, Commands.maskVassal)
							.toString(), plugin.colouredHasMask(permitted, Commands.maskFollower).toString(),
						plugin.colouredHasMask(permitted, Commands.maskAllied).toString(), plugin.colouredHasMask(permitted, Commands.maskFriendly)
							.toString(), plugin.colouredHasMask(permitted, Commands.maskNeutral).toString()));
				return true;
			}

			int mask = 0;

			if (args[1].equalsIgnoreCase("monarchy"))
				mask = Commands.maskMonarchy;
			if (args[1].equalsIgnoreCase("Patron"))
				mask = Commands.maskPatron;
			if (args[1].equalsIgnoreCase("Vassal"))
				mask = Commands.maskVassal;
			if (args[1].equalsIgnoreCase("Follower"))
				mask = Commands.maskFollower;
			if (args[1].equalsIgnoreCase("Allied"))
				mask = Commands.maskAllied;
			if (args[1].equalsIgnoreCase("Friendly"))
				mask = Commands.maskFriendly;
			if (args[1].equalsIgnoreCase("Neutral"))
				mask = Commands.maskNeutral;

			if (mask == 0) {
				plugin.sendMessage(sender, F("stUnknownChoice", args[1]));
				plugin.sendMessage(sender, F("stProvincePermitUsage", cmd.getName() + " " + args[0]));
				plugin.sendMessage(
					sender,
					F("stPermitProvinceChoices", plugin.colouredHasMask(permitted, Commands.maskMonarchy).toString(),
						plugin.colouredHasMask(permitted, Commands.maskPatron).toString(), plugin.colouredHasMask(permitted, Commands.maskVassal)
							.toString(), plugin.colouredHasMask(permitted, Commands.maskFollower).toString(),
						plugin.colouredHasMask(permitted, Commands.maskAllied).toString(), plugin.colouredHasMask(permitted, Commands.maskFriendly)
							.toString(), plugin.colouredHasMask(permitted, Commands.maskNeutral).toString()));
				return true;
			}
			// plugin.info("mask: " + mask);

			Boolean allowed = false;
			if (plugin.hasMask(permitted, mask)) {
				permitted = plugin.delMask(permitted, mask);
			} else {
				if (!plugin.hasCommandPermission(sender, "monarchy.province.permit." + args[1].toLowerCase())) {
					return true;
				}

				permitted = plugin.addMask(permitted, mask);
				allowed = true;
			}

			// plugin.info("playerName: " + playerName + ", mask: " + mask +
			// ", permitted: " + permitted);

			if (plugin.database.setProvincePermit(playerName, permitted) > 0) { // province.worldName, province.x, province.z,

				if (allowed == true) {
					plugin.sendMessage(sender, F("stPlayerCanNowModifyChunk", ChatColor.GREEN + args[1]));
				} else {
					plugin.sendMessage(sender, F("stPlayerCanNolongerModifyChunk", ChatColor.RED + args[1]));
				}
				return true;
			}
			plugin.sendMessage(sender, F("stDBError"));
			return true;*/
		}
		
		
		return false;
	}
	
}
