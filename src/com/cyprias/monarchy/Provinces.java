package com.cyprias.monarchy;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.monarchy.Monarchy.stanceInfo;
import com.cyprias.monarchy.commands.Commands;

public class Provinces {
	private Monarchy plugin;
	public Provinces(Monarchy monarchy) {
		this.plugin = monarchy;
		// TODO Auto-generated constructor stub
	}


    public String getProvinceOwner(String world, int chunkX, int chunkZ){
    	provinceInfo province = plugin.database.getProvince(world, chunkX, chunkZ);
    	
    	if (province != null){
    		return province.playerName;
    	}
    	
    	return null;
    }
    
    public int getProvincePermitted(String playerName){
    	return plugin.database.getProvincePermit(playerName);
    	//return getMasterProvince(province).permitted;
    }
    
    public provinceInfo getMasterProvince(provinceInfo province){
    	if (province.parent > 0){
    		provinceInfo parent = plugin.database.getProvinceByID(province.parent);
    		if (parent != null){
    			if (parent.parent > 0)
    				return getMasterProvince(parent);
    			
    			return parent;
    		}
    	}
    	return province;
    }
    
    public static HashMap<String, Boolean> permittedCache = new HashMap<String, Boolean>();
    
    public boolean canModifyChunk(Player player, Chunk chunk){
    	String playerName = player.getName();
    	String worldName = chunk.getWorld().getName();

    	int chunkX = chunk.getX();
    	int chunkZ = chunk.getZ();
    	
    	String together =playerName+"+"+worldName + "+"+chunkX+"+"+chunkZ;
    	
    	//plugin.sendMessage(player, "Chunk: " + chunkX + " x " + chunkZ);
    	
    	if (permittedCache.containsKey(together))
    		return permittedCache.get(together);
    	

		provinceInfo province = plugin.database.getProvince(worldName, chunkX, chunkZ);
		
		if (province != null){
			String targetName = province.playerName;
			
			if (targetName.equalsIgnoreCase(player.getName())){
				permittedCache.put(together, true);
				return true;
			}
			
			int permitted = getProvincePermitted(targetName);
			
			String myMonarch = plugin.getMonarch(playerName);
			String theirMonarch = plugin.getMonarch(targetName);
			if (myMonarch.equalsIgnoreCase(theirMonarch) && plugin.hasMask(permitted, Commands.maskMonarchy)){
				permittedCache.put(together, true);
				return true;
			}
				
			String patronName = plugin.getPatron(targetName);
			if (patronName != null && patronName.equalsIgnoreCase(targetName) && plugin.hasMask(permitted, Commands.maskPatron)){
				permittedCache.put(together, true);
				return true;
			}
			
			boolean isVassal = plugin.isVassal(targetName, playerName);
			if (isVassal == true && plugin.hasMask(permitted, Commands.maskVassal)){
				permittedCache.put(together, true);
				return true;
			}
			
			boolean isFollower = plugin.isFollower(targetName, playerName);
			if (isFollower == true && plugin.hasMask(permitted, Commands.maskFollower)){
				permittedCache.put(together, true);
				return true;
			}
			
			//int stanceID = plugin.getInheritStance(theirMonarch, playerName, myMonarch);
			 stanceInfo stance = plugin.getStance(targetName, playerName, true);
			
			if (stance == null){
				return false;
			}
			int stanceID = stance.stanceID;
			
			if (stanceID == 1 && plugin.hasMask(permitted, Commands.maskAllied)){
				permittedCache.put(together, true);
				return true;
			}
				
			if (stanceID == 2 && plugin.hasMask(permitted, Commands.maskFriendly)){
				permittedCache.put(together, true);
				return true;
			}
				
			if (stanceID == 2 && plugin.hasMask(permitted, Commands.maskNeutral)){
				permittedCache.put(together, true);
				return true;
			}

			
			permittedCache.put(together, false);
			return false;
		}
		
		permittedCache.put(together, true);
    	return true;
    }
    
   
    public boolean canModifyBlock(Player player, Block block){
    	int Y = block.getY();
      	if (Y < Config.minProvinceYProtection || Y > Config.maxProvinceYProtection)
    		return true;

    	return canModifyChunk(player, block.getChunk());
    }
	
	public static class provinceInfo {
		public String playerName;
		public String worldName;
		public int x;
		public int z;
		public int parent;
		//public int permitted;
		public String invited;
		public double time;
		public int id; 
		
		public provinceInfo(int id, String playerName, String worldName, int x, int z, int parent, String invited, double time) {
			this.id = id;
			this.playerName = playerName;
			this.worldName = worldName;
			this.x = x;
			this.z = z;
			this.parent = parent;
			//this.permitted = permitted;
			this.invited = invited;
			this.time = time;
		}
	}
    
    public void toggleProvincePermit(CommandSender sender, Command cmd, String[] args){
		if (!plugin.hasCommandPermission(sender, "monarchy.permit.province")) {
			return;
		}
    	
		int mask = 0;
		String playerName = sender.getName();
		int permitted = plugin.database.getProvincePermit(playerName);
		
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
		
		if (!plugin.hasCommandPermission(sender, "monarchy.permit.province." + toggleChoice.toLowerCase())) {
			return;
		}
		
		Boolean allowed = false;
		if (plugin.hasMask(permitted, mask)) {
			permitted = plugin.delMask(permitted, mask);
		} else {
			permitted = plugin.addMask(permitted, mask);
			allowed = true;
		}
		
		if (plugin.database.setProvincePermit(playerName, permitted) > 0) { // province.worldName, province.x, province.z,

			if (allowed == true) {
				plugin.sendMessage(sender, F("stPlayerCanNowRecallHome", ChatColor.GREEN + toggleChoice));
			} else {
				plugin.sendMessage(sender, F("stPlayerCanNolongerRecallHome", ChatColor.RED + toggleChoice));
			}
			return;
		}
		plugin.sendMessage(sender, F("stDBError"));
		
    }
    
	private String F(String string, Object... args) {
		return Localization.F(string, args);
	}
}
