package com.cyprias.monarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;


public class Ranks {
	private Monarchy plugin;
	public Ranks(Monarchy monarchy) {
		this.plugin = monarchy;
		
		loadRanks();
		loadPermissions();
	}

	public void loadPermissions(){
		
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			clearPermissions(p);
			loadPlayerPermissions(p);
		}
	}
	
	public static class rankInfo {
		public String title;
		public int followers;
		public List<String> permissions;

		@SuppressWarnings("unchecked")
		public rankInfo(String a, int b, List<?> list) {
			title = a;
			followers = b;
			permissions = (List<String>) list;
		}
	}
	
	List<rankInfo> rankList = new ArrayList<rankInfo>();
	
	public void loadRanks() {
		
		FileConfiguration cfgRanks = plugin.yml.getYMLConfig("playerRanks.yml"); //
		
		String value;
		ConfigurationSection info;
		for (String rank : cfgRanks.getKeys(false)) {
			info = cfgRanks.getConfigurationSection(rank);
			rankList.add(new rankInfo(info.getString("title"), info.getInt("followers"), info.getList("permissions")));
		}
		
		/*
		for (int i = 0; i < rankList.size(); i++) {
			plugin.info(i+" rank: " + rankList.get(i).title + " = " +rankList.get(i).followers);
		}*/
		
	}
	
	public rankInfo getPlayerRank(String playerName){
		int followers =plugin.getNumFollowers(playerName);
		return getRank(followers);
	}
	
	public rankInfo getRank(int followers){
		for (int i = (rankList.size() - 1); i >= 0; i--) {
			if (followers >= rankList.get(i).followers)
				return rankList.get(i);
		}
		return null;
	}
	
	
	public static HashMap<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();
	public void registerPlayer(Player player){
		if (permissions.containsKey(player.getName())) {
			//info(chatPrefix + "Registering " + player.getName() + ": was already registered");
			return;
		}
		PermissionAttachment attachment = player.addAttachment(plugin);
		permissions.put(player.getName(), attachment);
	}
	
	public void addPermission(Player player, String permission, boolean value){
		
		if (plugin.hasPermission(player, "monarchy.ranks.permissions")){
		
			String playerName = player.getName();
			if (!permissions.containsKey(playerName)){
				registerPlayer(player);
			}
			
			//plugin.info("addPermission " + playerName + ": " + permission + " = " + value);
			
			PermissionAttachment attachment = permissions.get(player.getName());
			attachment.setPermission(permission, value);
		}
	}
	
	public void addPermission(Player player, String permission){
		addPermission(player, permission, true);
	}
	
	public void removePermission(Player player, String permission){
		String playerName = player.getName();
		if (!permissions.containsKey(playerName)){
			registerPlayer(player);
		}
		
		PermissionAttachment attachment = permissions.get(player.getName());
		attachment.unsetPermission(permission);
	}
	
	public void clearPermissions(Player player, boolean reRegister){
		String playerName = player.getName();
		if (permissions.containsKey(playerName)){
			PermissionAttachment attachment = permissions.get(player.getName());
			player.removeAttachment(attachment);
			
			permissions.remove(playerName);
			
			//attachment.remove();
		}

		if (reRegister == true)
			registerPlayer(player);
	}
	
	public void clearPermissions(Player player){
		clearPermissions(player, true);
	}
	
	public void loadPlayerPermissions(Player player){
		String playerName = player.getName();
		//plugin.info("Loading permissions for " + playerName);
		
		//rankInfo info = getPlayerRank(player.getName());
		
		int followers = plugin.getNumFollowers(playerName);
		
		for (int i = 0; i < rankList.size(); i++) {
			
			
			
			if (followers >= rankList.get(i).followers){
				
				if (rankList.get(i).permissions != null){
					
					//plugin.info("loadPlayerPermission: " + rankList.get(i).permissions + " = " + rankList.get(i).permissions.size());
					
					for (int p = 0; p < rankList.get(i).permissions.size(); p++) {
					//	plugin.info("loadPlayerPermissions " + playerName + ": " + rankList.get(i).permissions.get(p));
						addPermission(player,rankList.get(i).permissions.get(p));
					}
					
					
				}
				
			}
			
		}
		

	}
	
	
	public void unloadPlayerPermissions(Player player){
		String playerName = player.getName();
		//plugin.info("Unloading permissions for " + playerName);
		clearPermissions(player, false);
	}
	
}
