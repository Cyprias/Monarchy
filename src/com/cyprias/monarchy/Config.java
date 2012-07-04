package com.cyprias.monarchy;

import org.bukkit.configuration.Configuration;


public class Config {
	private Monarchy plugin;
	private static Configuration config;
	
	public static double leadershipSkillDefault, leadershipSkillIncrease,leadershipSkillCost, leadershipSkillCap;
	public static double loyaltySkillDefault, loyaltySkillCost, loyaltySkillCap, loyaltySkillIncrease;
	public static double passUpPercent, grandPatronPassupModifier;
	
	public static boolean allowGrandPatronXP, preventXPLooping, preventAllianceLoops, PvPMode, allowPatronXP, notifyOpsOfNewVersion, useSimplePassupPercent;
	public static boolean onlyMonarchCanTpFollower, colourNamesByRelationship, notifyTargetMonarchyOfStance, useProvinceProtection;
	public static boolean usemChatNames,   victimsCanDefendThemselves;
	public static boolean passupBottleExp, chargeProvinceRent, colourChatNamesByStance, userStanceOverridesTheirMonarch;
	public static int defaultStance, defendTime, interruptRequestDuration, defaultHomePermitFlag, minProvinceYProtection, maxProvinceYProtection, claimProvinceCost; 
	public static int defaultProvincePermitFlag, provinceRentFee, provinceRentTime, provinceRentExpire, defaultTeleportPermitFlag;
	public static String locale, sqlUsername, sqlPassword, sqlURL;
	
	//public static String tblHomes, tblAllegiance;
	
	
	public Config(Monarchy plugin) {
		this.plugin = plugin;
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		
		loadConfigOpts();
	}
	
	public void reloadOurConfig(){
		plugin.reloadConfig();
		config = plugin.getConfig().getRoot();
		loadConfigOpts();
	}
	
	private void loadConfigOpts(){
		passUpPercent = config.getDouble("passUpPercent");
		leadershipSkillDefault = config.getDouble("leadershipSkillDefault");
		loyaltySkillDefault = config.getDouble("loyaltySkillDefault");
		leadershipSkillIncrease = config.getDouble("leadershipSkillIncrease");
		leadershipSkillCost =  config.getDouble("leadershipSkillCost");
		leadershipSkillCap =  config.getDouble("leadershipSkillCap");
		useProvinceProtection = config.getBoolean("useProvinceProtection");
		claimProvinceCost = config.getInt("claimProvinceCost");
		defaultProvincePermitFlag= config.getInt("defaultProvincePermitFlag");
		chargeProvinceRent = config.getBoolean("chargeProvinceRent");
		provinceRentFee = config.getInt("provinceRentFee");
		provinceRentTime = config.getInt("provinceRentTime");
		provinceRentExpire = config.getInt("provinceRentExpire");
		colourChatNamesByStance = config.getBoolean("colourChatNamesByStance");
		userStanceOverridesTheirMonarch = config.getBoolean("userStanceOverridesTheirMonarch");
		defaultTeleportPermitFlag = config.getInt("defaultTeleportPermitFlag");
		

		passupBottleExp = config.getBoolean("passupBottleExp");
		
		minProvinceYProtection = config.getInt("minProvinceYProtection");
		maxProvinceYProtection = config.getInt("maxProvinceYProtection");
		
		loyaltySkillDefault = config.getDouble("loyaltySkillDefault");
		loyaltySkillCost = config.getDouble("loyaltySkillCost");
		loyaltySkillCap = config.getDouble("loyaltySkillCap");
		loyaltySkillIncrease = config.getDouble("loyaltySkillIncrease");
		
		useSimplePassupPercent = config.getBoolean("useSimplePassupPercent");
		
		grandPatronPassupModifier= config.getDouble("grandPatronPassupModifier");
		
		allowGrandPatronXP = config.getBoolean("allowGrandPatronXP");
		preventXPLooping = config.getBoolean("preventXPLooping");
		preventAllianceLoops = config.getBoolean("preventAllianceLoops");
		locale = config.getString("locale");
		PvPMode = config.getBoolean("PvPMode");

		victimsCanDefendThemselves = config.getBoolean("victimsCanDefendThemselves");
		allowPatronXP =  config.getBoolean("allowPatronXP");
		notifyOpsOfNewVersion =  config.getBoolean("notifyOpsOfNewVersion");
		colourNamesByRelationship =  config.getBoolean("colourNamesByRelationship");	
		notifyTargetMonarchyOfStance =  config.getBoolean("notifyTargetMonarchyOfStance");	 
		
		defaultHomePermitFlag = config.getInt("defaultHomePermitFlag");
		
		onlyMonarchCanTpFollower =  config.getBoolean("onlyMonarchCanTpFollower"); 
		
		interruptRequestDuration = config.getInt("interruptRequestDuration");
		
		defendTime = config.getInt("defendTime");
		
		usemChatNames = config.getBoolean("usemChatNames");
		
		String sStance = config.getString("defaultStance").toLowerCase();
		if (plugin.stanceIDs.containsKey(sStance))
			defaultStance = plugin.stanceIDs.get(sStance);

		
		
		sqlUsername = config.getString("mysql.username");
		sqlPassword = config.getString("mysql.password");
		sqlURL = "jdbc:mysql://" + config.getString("mysql.hostname") + ":" + config.getInt("mysql.port") + "/" + config.getString("mysql.database");
	}
	
	
	
}

