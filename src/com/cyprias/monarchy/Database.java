package com.cyprias.monarchy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.World;

import org.bukkit.entity.Player;

import com.cyprias.monarchy.Monarchy.allegianceInfo;
import com.cyprias.monarchy.Provinces.provinceInfo;

public class Database {
	private Monarchy plugin;

	public Database(Monarchy plugin) {
		this.plugin = plugin;

		if (testDBConnection()) {
			setupMysql();
		} else {
			plugin.info("Failed to connect to database, disabling plugin...");
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}

	String tblAllegiance = "Monarchy_Allegiances";
	String tblOfflineGains = "Monarchy_OfflineXP";
	String tblStances = "Monarchy_Stances";
	String tblHomes = "Monarchy_Homes";
	static String tblSkills = "Monarchy_Skills";

	public boolean testDBConnection() {
		try {
			Connection con = DriverManager.getConnection(Config.sqlURL, Config.sqlUsername, Config.sqlPassword);
			
			con.close();
			return true;
		} catch (SQLException e) {
		}
		return false;
	}

	public void setupMysql() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = getSQLConnection();

			String query;

			PreparedStatement statement = con.prepareStatement("show tables like '%" + tblAllegiance + "%'");
			ResultSet result = statement.executeQuery();
			result.last();
			if (result.getRow() == 0) {

				query = "CREATE TABLE `" + tblAllegiance + "` (" + " `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," + " `player` VARCHAR( 32 ) NOT NULL ,"
					+ " `patron` VARCHAR( 32 ) NOT NULL ," + " `XP` INT( 11 ) DEFAULT 0 NOT NULL" + ") ENGINE = InnoDB;";

				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}

			// //////

			statement = con.prepareStatement("show tables like '" + tblOfflineGains + "'");
			result = statement.executeQuery();
			result.last();
			if (result.getRow() == 0) {

				query = "CREATE TABLE `Monarchy_OfflineXP` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR(32) NOT NULL, `world` VARCHAR(32) NOT NULL, `amount` INT NOT NULL, `vassal` INT NOT NULL) ENGINE = InnoDB";

				statement = con.prepareStatement(query);
				statement.executeUpdate();
			} else if (tableFieldExists("Monarchy_OfflineXP", "world") == false) {

				query = "ALTER TABLE `Monarchy_OfflineXP` ADD `world` VARCHAR( 32 ) NOT NULL AFTER `player` ";
				statement = con.prepareStatement(query);
				statement.executeUpdate();
				plugin.info("Added 'world' field to Monarchy_OfflineXP db table.");

				query = "UPDATE `Monarchy_OfflineXP` SET `world` = ? WHERE `world` = ?;";
				String worldName = plugin.getServer().getWorlds().get(0).getName();
				statement = con.prepareStatement(query);
				statement.setString(1, worldName);
				statement.setString(2, "");
				int count = statement.executeUpdate();

				plugin.info("Set world field to '" + worldName + "' in " + count + " rows in offlineXP.");
			}

			// /////////////////////////////////

			statement = con.prepareStatement("show tables like '%" + tblStances + "%'");
			result = statement.executeQuery();
			result.last();
			if (result.getRow() == 0) {

				query = "CREATE TABLE " + tblStances + "(`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " + "`player` VARCHAR(32) NOT NULL, "
					+ "`stance` INT NOT NULL DEFAULT '0', " + "`target` VARCHAR(32) NOT NULL, " + "`time` DOUBLE NOT NULL) ENGINE = InnoDB;";

				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}

			// ////////////////////////////////

			/*
			statement = con.prepareStatement("show tables like '%" + tblHomes + "%'");
			result = statement.executeQuery();
			result.last();
			if (result.getRow() == 0) {

				
				statement = con.prepareStatement(query);
				statement.executeUpdate();
				
				//`permitted` INT NOT NULL, 
				//REmove permit thing.
			}
*/
			
			if (tableExists(tblHomes) == false){
				query = "CREATE TABLE "
					+ tblHomes
					+ " (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR(32) NOT NULL, `world` VARCHAR(32) NOT NULL, `x` INT NOT NULL, `y` INT NOT NULL, `z` INT NOT NULL, `yaw` INT NOT NULL, `pitch` INT NOT NULL, `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE (`player`)) ENGINE = InnoDB";
				statement = con.prepareStatement(query);
				statement.executeUpdate();
				
			}else if (tableFieldExists(tblHomes, "permitted")){
				plugin.info("Removing permitted field from "+tblHomes+".");
				query = "ALTER TABLE `"+tblHomes+"` DROP `permitted`;";
				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}
			
			// /////////////

			statement = con.prepareStatement("show tables like '%" + tblSkills + "%'");
			result = statement.executeQuery();
			result.last();
			if (result.getRow() == 0) {

				query = "CREATE TABLE "
					+ tblSkills
					+ " (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR(32) NOT NULL, `leadership` DOUBLE NULL, `loyalty` DOUBLE NULL, UNIQUE (`player`)) ENGINE = InnoDB";

				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}

			// /////////////
			/*
			statement = con.prepareStatement("show tables like '%Monarchy_Provinces%'");
			result = statement.executeQuery();
			result.last();
			if (result.getRow() == 0) {

				query = "CREATE TABLE Monarchy_Provinces (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR(32) NOT NULL, `world` VARCHAR(32) NOT NULL, `x` INT NOT NULL, `z` INT NOT NULL, `parent` INT NULL, `permitted` INT NOT NULL, `invited` TEXT NULL, `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE = InnoDB";

				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}
			 */
			if (tableExists("Monarchy_Provinces") == false){
				query = "CREATE TABLE Monarchy_Provinces (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR(32) NOT NULL, `world` VARCHAR(32) NOT NULL, `x` INT NOT NULL, `z` INT NOT NULL, `parent` INT NULL, `invited` TEXT NULL, `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE = InnoDB";
				statement = con.prepareStatement(query);
				statement.executeUpdate();
				
			}else if (tableFieldExists("Monarchy_Provinces", "permitted")){
				plugin.info("Removing permitted field from Monarchy_Provinces.");
				query = "ALTER TABLE `Monarchy_Provinces` DROP `permitted`;";
				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}
			
			
			///////////////////
			
			if (tableExists("Monarchy_Permits") == false){
				plugin.info("Creating Monarchy_Permits table...");
				query = "CREATE TABLE `Monarchy_Permits` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR(32) NOT NULL, `homePermit` INT NULL, `provincePermit` INT NULL, `tpPermit` INT NULL, UNIQUE (`player`)) ENGINE = InnoDB";
				statement = con.prepareStatement(query);
				statement.executeUpdate();
			}
			

			result.close();
			statement.close();
			con.close();

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean tableExists(String table, Connection con){
		boolean exists = false;
		//Connection con = getSQLConnection();
		
		try {
			PreparedStatement statement = con.prepareStatement("show tables like ?");
			statement.setString(1, table);
			ResultSet result = statement.executeQuery();
			result.last();
			if (result.getRow() > 0) {
				exists = true;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return exists;
	}
	

	
	public boolean tableExists(String table){
		Connection con = getSQLConnection();
		boolean exists = tableExists(table, con);
		closeSQLConnection(con);
		return exists;
	}
	
	
	
	public boolean tableFieldExists(String table, String field, Connection con){
		boolean found = false;
		String query = "SELECT * FROM " + table + ";";

		try {
			PreparedStatement statement = con.prepareStatement(query);
			
			ResultSet result = statement.executeQuery();
			ResultSetMetaData rsMetaData = result.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();

			String columnName;
			for (int i = 1; i < numberOfColumns + 1; i++) {
				columnName = rsMetaData.getColumnName(i);
				if (columnName.equalsIgnoreCase(field)) {
					// plugin.info("offline XP  world found.");
					found = true;
					break;
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return found;
	}
	
	public boolean tableFieldExists(String table, String field){
		Connection con = getSQLConnection();
		boolean exists = tableFieldExists(table, field, con);
		closeSQLConnection(con);
		return exists;
	}
	
	public static Connection getSQLConnection() {
		try {
			return DriverManager.getConnection(Config.sqlURL, Config.sqlUsername, Config.sqlPassword);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getMonarch(String playerName) {
		String monarchName = playerName;

		String tMonarch = null;

		HashMap<String, Boolean> vassal = new HashMap<String, Boolean>();
		vassal.put(playerName, true);

		while (true) {
			tMonarch = plugin.getPatron(monarchName);
			if (tMonarch != null) {

				if (vassal.containsKey(tMonarch)) {
					// We've in a loop, there's no real monarch.
					// plugin.info("getPlayersMonarch loop");
					monarchName = null;
					break;
				}

				// plugin.info("getPlayersMonarch " + monarchName +
				// "'s patron is " + tMonarch + ".");
				monarchName = tMonarch;

				vassal.put(tMonarch, true);
			} else {
				break;
			}
		}

		return monarchName;
	}

	public Boolean isFollower(String playerName, String targetName) {
		String patronName = plugin.getPatron(targetName);

		// plugin.info("isYourFollower1 "+targetName +" > " + patronName);

		// String patronChain = targetName + ">" + patronName;

		int i = 0;

		while (true) {

			if (patronName == null)
				break;

			if (patronName.equalsIgnoreCase(playerName)) {
				return true;// patronChain
			}

			patronName = plugin.getPatron(patronName);

			// if (patronName != null) {
			// patronChain += ">" + patronName;
			// }

		}

		return false;
	}

	public String getFollowerRelationship(String playerName, String targetName) {
		String patronName = plugin.getPatron(targetName);

		String patronChain = targetName + ">" + patronName;

		int i = 0;

		while (true) {

			if (patronName == null)
				break;

			if (patronName.equalsIgnoreCase(playerName)) {
				return patronChain;
			}

			patronName = plugin.getPatron(patronName);

			if (patronName != null) {
				patronChain += ">" + patronName;
			}

		}

		return null;
	}

	public String getPatron(String playerName) {
		String patronsName = null;
		String SQL = "SELECT `patron` " + " FROM " + tblAllegiance + " WHERE `player` LIKE ?";

		Connection con = getSQLConnection();

		try {

			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				patronsName = result.getString(1);
			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return patronsName;
	}

	public int getPlayersPatronXP(String playerName) {
		int patronsXP = 0;
		String SQL = "SELECT `XP` " + " FROM " + tblAllegiance + " WHERE `player` LIKE ?";

		Connection con = getSQLConnection();

		try {

			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				patronsXP = result.getInt(1);
			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return patronsXP;
	}

	public static class vassalInfo {
		public String vassalName;
		int xpAmount;

		public vassalInfo(String a, int b) {
			vassalName = a;
			xpAmount = b;
		}
	}

	public List<vassalInfo> getPlayerVassals(String playerName) {
		List<vassalInfo> vassals = new ArrayList<vassalInfo>();

		String SQL = "SELECT *" + " FROM " + tblAllegiance + " WHERE `patron` LIKE ?" + " ORDER BY XP DESC;";

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				// patron = result.getString(1);
				vassals.add(new vassalInfo(result.getString(2), result.getInt(4)));

			}

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return vassals;
	}

	HashMap<String, Boolean> countedFollower = new HashMap<String, Boolean>();

	public int getNumFollowers(String playerName, boolean isLoop) {
		countedFollower.put(playerName, true); // Prevent infinite loops.

		List<vassalInfo> vassals = getPlayerVassals(playerName);

		int followers = vassals.size();
		// plugin.info(playerName + " has " + followers + " followers.");

		if (vassals.size() > 0) {

			for (int i = 0; i < vassals.size(); i++) {
				// vassalList += ", " + vassals.get(i).vassalName + " (" +
				// vassals.get(i).xpAmount + ")";

				if (!countedFollower.containsKey(vassals.get(i).vassalName)) {
					// plugin.info("Getting " + vassals.get(i).vassalName +
					// "'s followers.");
					followers += getNumFollowers(vassals.get(i).vassalName, true);
				}
			}
		}

		if (isLoop == false)
			countedFollower.clear();// Our main call is over, clear the
									// followers table.

		return followers;
	}

	public int getNumFollowers(String playerName) {
		return getNumFollowers(playerName, false);
	}

	public int dropVassal(String patronName, String vassalName) {

		String SQL = "DELETE FROM " + tblAllegiance + " WHERE `player` LIKE ? AND `patron` = ?;";
		int success = 0;

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, vassalName);
			statement.setString(2, patronName);

			success = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return success;

	}

	public boolean isVassal(String playerName, String vassalName) {
		boolean isvassal = false;

		String SQL = "SELECT *" + " FROM " + tblAllegiance + " WHERE `player` LIKE ?" + " AND `patron` = ?";

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, vassalName);
			statement.setString(2, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				// patron = result.getString(1);
				isvassal = true;
				break;
			}

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isvassal;
	}

	public class homeInfo {
		public String playerName;
		public int x;
		public int y;
		public int z;
		public int yaw;
		public int pitch;
		public int permitted;
		public String worldName;

		public homeInfo(String playerName, String worldName, int x, int y, int z, int yaw, int pitch, int permitted) {
			this.playerName = playerName;
			this.worldName = worldName;
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.permitted = permitted;
		}
	}

	public homeInfo getHome(String playerName) {

		homeInfo home = null;

		//String SQL = "SELECT *" + " FROM " + tblHomes + " WHERE `player` LIKE ?;";
		
		String SQL = "SELECT *" + " FROM " + tblHomes + " WHERE `player` LIKE ?;";
		
		
		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {

				home = new homeInfo(playerName, result.getString(3), result.getInt(4), result.getInt(5), result.getInt(6), result.getInt(7), result.getInt(8),
					result.getInt(9));

				// plugin.info("getStance: " + stance);

				// double time = result.getTimestamp(5).getTime()/1000D;
				// double diff = (plugin.getUnixTime() - time);
				// plugin.info("time diff: " + diff);

				break;
			}

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return home;
	}

	public int getHomePermit(String playerName, Connection con){
		String query = "SELECT `homePermit` FROM `Monarchy_Permits` WHERE `player` = ? AND `homePermit` IS NOT NULL;";
		int value = Config.defaultHomePermitFlag;
		try {
			PreparedStatement statement = con.prepareStatement(query);

			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();
			while (result.next()) {
				value = result.getInt(1);
			}

			result.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return value;
	}
	public int getHomePermit(String playerName){
		Connection con = getSQLConnection();
		int value = getHomePermit(playerName, con);
		closeSQLConnection(con);
		return value;
	}
	
	public int getProvincePermit(String playerName, Connection con){
		String query = "SELECT `provincePermit` FROM `Monarchy_Permits` WHERE `player` = ? AND `provincePermit` IS NOT NULL;";
		int value = Config.defaultProvincePermitFlag;
		try {
			PreparedStatement statement = con.prepareStatement(query);

			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();
			while (result.next()) {
				value = result.getInt(1);
			}

			result.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return value;
	}
	public int getProvincePermit(String playerName){
		Connection con = getSQLConnection();
		int value = getProvincePermit(playerName, con);
		closeSQLConnection(con);
		return value;
	}
	

	
	public int getTeleportPermit(String playerName, Connection con){
		String query = "SELECT `teleportPermit` FROM `Monarchy_Permits` WHERE `player` = ? AND `teleportPermit` IS NOT NULL;";
		int value = Config.defaultTeleportPermitFlag;
		try {
			PreparedStatement statement = con.prepareStatement(query);

			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();
			while (result.next()) {
				value = result.getInt(1);
			}

			result.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return value;
	}
	public int getTeleportPermit(String playerName){
		Connection con = getSQLConnection();
		int value = getTeleportPermit(playerName, con);
		closeSQLConnection(con);
		return value;
	}
	
	
	public int addPlayerToPermits(String playerName, Connection con){
		int updateSuccessful = 0;
		String query = "INSERT INTO `Monarchy_Permits` (`id`, `player`, `homePermit`, `provincePermit`, `teleportPermit`) VALUES (NULL, ?, NULL, NULL, NULL);";
		try {
			PreparedStatement statement = con.prepareStatement(query);
			statement.setString(1, playerName);
			updateSuccessful = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return updateSuccessful;
	}
	
	public boolean tableHasValue(String table, String field, String value, Connection con){
		boolean found = false;
		String query = "SELECT "+field+" FROM "+table+" WHERE "+field+" = ? ;";
		
		try {
			PreparedStatement statement = con.prepareStatement(query);
			//statement.setString(1, field);
			//statement.setString(2, table);
			//statement.setString(3, field);
			statement.setString(1, value);
			
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				found = true;
				break;
			}
			result.close();
			statement.close();
			
		} catch (SQLException e) {e.printStackTrace();
		}
		
	//	plugin.info("tableHasValue2 table: " + table  + ", field: " + field + ", value: " + value + ", found: " + found);
		
		return found;
	}
	public boolean tableHasValue(String table, String field, String value){
		Connection con = getSQLConnection();
		boolean result = tableHasValue(table, field, value, con);
		closeSQLConnection(con);
		return result;
	}
	
	
	public int addPlayerToPermits(String playerName) {
		Connection con = getSQLConnection();
		int value = addPlayerToPermits(playerName);
		closeSQLConnection(con);
		return value;
	}
	
	public int setProvincePermit(String playerName, int permitted, Connection con) {
		int updateSuccessful = 0;
		
		if (!tableHasValue("Monarchy_Permits", "player", playerName, con)){
			addPlayerToPermits(playerName, con);
		}
		
		
		//String SQL = "UPDATE " + tblHomes + " SET " + "`permitted` = ?, `time` = CURRENT_TIMESTAMP WHERE `player` LIKE ? ;";
		String query = "UPDATE `Monarchy_Permits` SET `provincePermit` = ? WHERE `player` = ?;";

		PreparedStatement statement;
		
		try {
			statement = con.prepareStatement(query);

			statement.setInt(1, permitted);
			statement.setString(2, playerName);

			updateSuccessful = statement.executeUpdate();

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return updateSuccessful;
	}
	public int setProvincePermit(String playerName, int permitted) {
		Connection con = getSQLConnection();
		int value = setProvincePermit(playerName, permitted, con);
		closeSQLConnection(con);
		return value;
	}
	
	
	
	
	
	public int setTeleportPermit(String playerName, int permitted, Connection con) {
		int updateSuccessful = 0;
		
		if (!tableHasValue("Monarchy_Permits", "player", playerName, con)){
			addPlayerToPermits(playerName, con);
		}
		
		
		//String SQL = "UPDATE " + tblHomes + " SET " + "`permitted` = ?, `time` = CURRENT_TIMESTAMP WHERE `player` LIKE ? ;";
		String query = "UPDATE `Monarchy_Permits` SET `teleportPermit` = ? WHERE `player` = ?;";

		PreparedStatement statement;
		
		try {
			statement = con.prepareStatement(query);

			statement.setInt(1, permitted);
			statement.setString(2, playerName);

			updateSuccessful = statement.executeUpdate();

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return updateSuccessful;
	}

	public int setTeleportPermit(String playerName, int permitted) {
		Connection con = getSQLConnection();
		int value = setTeleportPermit(playerName, permitted, con);
		closeSQLConnection(con);
		return value;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public int setHomePermit(String playerName, int permitted, Connection con) {
		int updateSuccessful = 0;
		
		if (!tableHasValue("Monarchy_Permits", "player", playerName, con)){
			addPlayerToPermits(playerName, con);
		}
		
		
		//String SQL = "UPDATE " + tblHomes + " SET " + "`permitted` = ?, `time` = CURRENT_TIMESTAMP WHERE `player` LIKE ? ;";
		String query = "UPDATE `Monarchy_Permits` SET `homePermit` = ? WHERE `player` = ?;";

		PreparedStatement statement;
		
		try {
			statement = con.prepareStatement(query);

			statement.setInt(1, permitted);
			statement.setString(2, playerName);

			updateSuccessful = statement.executeUpdate();

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return updateSuccessful;
	}

	public int setHomePermit(String playerName, int permitted) {
		Connection con = getSQLConnection();
		int value = setHomePermit(playerName, permitted, con);
		closeSQLConnection(con);
		return value;
	}
	
	public void closeSQLConnection(Connection con){
		try {
			con.close();
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	public int saveHome(String playerName, String worldName, int x, int y, int z, int yaw, int pitch, int permitted) {

		int updateSuccessful = 0;

		String SQL = "UPDATE " + tblHomes + " SET `world` = ?, `x` = ?, `y` = ?, `z` = ?, `yaw` = ?, `pitch` = ? "
			+ "WHERE `player` LIKE ? ;";

		Connection con = getSQLConnection();
		PreparedStatement statement = null;

		try {
			statement = con.prepareStatement(SQL);

			statement.setString(1, worldName);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.setInt(5, yaw);
			statement.setInt(6, pitch);
			statement.setString(7, playerName);

			updateSuccessful = statement.executeUpdate();

			// plugin.info("saveHome1: " + updateSuccessful);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (updateSuccessful == 0) {
			SQL = "INSERT INTO " + tblHomes
				+ " (`id` ,`player` ,`world` ,`x` ,`y` ,`z` ,`yaw` ,`pitch` ,`time`)VALUES (NULL , ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);";

			try {
				statement = con.prepareStatement(SQL);

				statement.setString(1, playerName);
				statement.setString(2, worldName);
				statement.setInt(3, x);
				statement.setInt(4, y);
				statement.setInt(5, z);
				statement.setInt(6, yaw);
				statement.setInt(7, pitch);
				statement.setInt(8, permitted);

				updateSuccessful = statement.executeUpdate();

				statement.close();

			} catch (SQLException e) {

				e.printStackTrace();
			}

		}

		try {
			statement.close();
			con.close();
		} catch (SQLException e) {
		}

		return updateSuccessful;
	}

	public int saveHome(String playerName, String worldName, int x, int y, int z, int pitch, int yaw) {
		return saveHome(playerName, worldName, x, y, z, yaw, pitch, Config.defaultHomePermitFlag);
	}

	public void storeOfflineXP(String playerName, String worldName, int Amount, String vassalName) {
		String SQL = "UPDATE " + tblOfflineGains + " SET amount=amount+" + Amount + " WHERE `player` = ? AND `world` = ? AND `vassal` = ?";

		int updateSuccessful = 0;

		Connection con = getSQLConnection();
		PreparedStatement statement = null;

		try {
			statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);
			statement.setString(2, worldName);
			statement.setString(3, vassalName);

			updateSuccessful = statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// plugin.info("updateSuccessful: " + updateSuccessful);

		if (updateSuccessful == 0) {
			// Entry doesn't exist yet, time to insert it into the table.
			SQL = "INSERT INTO " + tblOfflineGains + " (`id`, `player`, `world`, `amount`, `vassal`) VALUES (NULL, ?, ?, ?, ?);";

			try {

				statement = con.prepareStatement(SQL);

				statement.setString(1, playerName);
				statement.setString(2, worldName);
				statement.setInt(3, Amount);
				statement.setString(4, vassalName);

				statement.executeUpdate();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {
			statement.close();
			con.close();
		} catch (SQLException e) {
		}
	}

	public int expungeStance(String playerName, String targetName) {
		int success = 0;

		String SQL = "DELETE FROM " + tblStances + " WHERE `player` = ?" + " AND `target` = ?;";

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);
			statement.setString(2, targetName);

			success = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return success;
	}

	public int saveStance(String playerName, int stance, String targetName) {
		int success = 0;

		String SQL = "UPDATE " + tblStances + " SET `stance` = ?" + " WHERE `player` = ?" + " AND `target` = ?;";

		Connection con = getSQLConnection();
		PreparedStatement statement = null;
		try {
			statement = con.prepareStatement(SQL);
			statement.setInt(1, stance);
			statement.setString(2, playerName);
			statement.setString(3, targetName);

			success = statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (success == 0) {// Entry wasn't already in the table, we'll create it
							// now.
			SQL = "INSERT INTO " + tblStances + " (`id` , `player` , `stance` , `target` , `time`)" + " VALUES (NULL , ?, ?, ?, NULL)";

			try {

				statement = con.prepareStatement(SQL);

				statement.setString(1, playerName);
				statement.setInt(2, stance);
				statement.setString(3, targetName);

				success = statement.executeUpdate();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			statement.close();
			con.close();
		} catch (SQLException e) {
		}
		return success;
	}

	public boolean hasStance(String playerName, String targetName) {
		boolean stance = false;

		if (playerName.equalsIgnoreCase(targetName))
			return true;

		String SQL = "SELECT *" + " FROM " + tblStances + " WHERE `player` LIKE ?" + " AND `target` LIKE ?";
		// boolean found = false;

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);
			statement.setString(2, targetName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				stance = true;
				break;
			}

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stance;
	}

	public int getStance(String playerName, String targetName) {
		if (playerName.equalsIgnoreCase(targetName))
			return 0;

		int stance = Config.defaultStance;// Not found.

		String SQL = "SELECT *" + " FROM " + tblStances + " WHERE `player` LIKE ?" + " AND `target` LIKE ?";
		// boolean found = false;

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);
			statement.setString(2, targetName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				stance = result.getInt(3);
				// plugin.info("getStance: " + stance);

				// double time = result.getTimestamp(5).getTime()/1000D;
				// double diff = (plugin.getUnixTime() - time);
				// plugin.info("time diff: " + diff);

				break;
			}

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stance;
	}

	public int playerPledgedAllegiance(String playerName, String patronName) {
		// tblAllegiance
		int success = 0;

		String SQL = "INSERT INTO " + tblAllegiance + " (`id`, `player`, `patron`) VALUES (NULL, ?, ?);";

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);
			statement.setString(2, patronName);

			success = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return success;
	}

	public int playerDissolveAllegiance(String playerName, String patronName) {
		// tblAllegiance

		String SQL = "DELETE FROM " + tblAllegiance + " WHERE `player` = ? AND `patron` = ?;";
		int success = 0;

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, playerName);
			statement.setString(2, patronName);

			success = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return success;
	}

	private String F(String string, Object... args) {
		return Localization.F(string, args);
	}

	public void givePlayerOfflineXP(Player player) {
		String SQL = "SELECT * FROM " + tblOfflineGains + " WHERE `player` = ? AND `world` = ?";

		// plugin.info("givePlayerOfflineXP: " + player.getName() + " in " +
		// player.getWorld().getName());

		boolean deleteEntries = false;

		Connection con = getSQLConnection();
		PreparedStatement statement = null;
		try {
			statement = con.prepareStatement(SQL);

			statement.setString(1, player.getName());
			statement.setString(2, player.getWorld().getName());

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				// plugin.info("givePlayerOfflineXP " + result.getInt(4)
				// +" from " + result.getString(5));

				plugin.sendMessage(player, F("stGiveOfflineXP", result.getInt(4), result.getString(5)));

				player.giveExp(result.getInt(4));
				deleteEntries = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (deleteEntries == true) {
			SQL = "DELETE FROM " + tblOfflineGains + " WHERE `player` = ? AND `world` = ?;";

			try {
				statement = con.prepareStatement(SQL);
				statement.setString(1, player.getName());
				statement.setString(2, player.getWorld().getName());

				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int savePassupXP(String vassalName, String patronName, int amount) {
		String SQL = "UPDATE " + tblAllegiance + " SET XP=XP+" + amount + " WHERE `player` LIKE ?" + " AND `patron` LIKE ?";

		int updateSuccessful = 0;
		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, vassalName);
			statement.setString(2, patronName);

			updateSuccessful = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return updateSuccessful;
	}

	public static String resourceToString(String name) {
		InputStream input = Monarchy.class.getResourceAsStream("/" + name);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];

		if (input != null) {
			try {
				int n;
				Reader reader = new BufferedReader(new InputStreamReader(input));
				while ((n = reader.read(buffer)) != -1)
					writer.write(buffer, 0, n);
			} catch (IOException e) {
				try {
					input.close();
				} catch (IOException ex) {
				}
				return null;
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		} else {
			return null;
		}

		String text = writer.toString().trim();
		text = text.replace("\r\n", " ").replace("\n", " ");
		return text.trim();
	}

	//
	/*
	 * CREATE TABLE `minecraft`.`Monarchy_Skills` (`id` INT NOT NULL
	 * AUTO_INCREMENT, `player` VARCHAR(32) NOT NULL, `leadership` FLOAT NOT
	 * NULL, `loyalty` FLOAT NOT NULL, PRIMARY KEY (`id`, `player`)) ENGINE =
	 * InnoDB
	 */

	public void clearSkillCache() {
		leadershipCache.clear();
		loyaltyCache.clear();
	}

	HashMap<String, Double> leadershipCache = new HashMap<String, Double>();

	public Double getLeadership(String playerName) {
		if (leadershipCache.containsKey(playerName))
			return leadershipCache.get(playerName);

		Double leadership = Config.leadershipSkillDefault;
		String SQL = "SELECT `leadership` FROM " + tblSkills + " WHERE `player` = ? AND `leadership` IS NOT NULL";

		Connection con = getSQLConnection();

		try {

			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				leadership = result.getDouble(1);
			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		leadershipCache.put(playerName, leadership);
		return leadership;
	}

	HashMap<String, Double> loyaltyCache = new HashMap<String, Double>();

	public Double getLoyalty(String playerName) {
		if (loyaltyCache.containsKey(playerName))
			return loyaltyCache.get(playerName);

		Double loyalty = Config.leadershipSkillDefault;
		String SQL = "SELECT `loyalty` FROM " + tblSkills + " WHERE `player` = ? AND `leadership` IS NOT NULL";

		Connection con = getSQLConnection();

		try {

			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				loyalty = result.getDouble(1);
			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		loyaltyCache.put(playerName, loyalty);
		return loyalty;
	}

	public int setLeadership(String playerName, double leadership) {
		String SQL = "UPDATE " + tblSkills + " SET " + "`leadership` = ? WHERE `player` LIKE ? ;";

		int updateSuccessful = 0;

		Connection con = getSQLConnection();
		PreparedStatement statement = null;

		try {
			statement = con.prepareStatement(SQL);
			statement.setDouble(1, leadership);
			statement.setString(2, playerName);
			updateSuccessful = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (updateSuccessful == 0) {
			SQL = "INSERT INTO " + tblSkills + " (`id` ,`player` ,`leadership` ,`loyalty`) VALUES (NULL , ?, ?, NULL);";
			try {
				statement = con.prepareStatement(SQL);
				statement.setString(1, playerName);
				statement.setDouble(2, leadership);
				updateSuccessful = statement.executeUpdate();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			statement.close();
			con.close();
		} catch (SQLException e) {
		}

		leadershipCache.put(playerName, leadership);

		return updateSuccessful;
	}

	public int setLoyalty(String playerName, double loyalty) {
		String SQL = "UPDATE " + tblSkills + " SET " + "`leadership` = ? WHERE `player` LIKE ? ;";

		int updateSuccessful = 0;

		Connection con = getSQLConnection();
		PreparedStatement statement = null;

		try {
			statement = con.prepareStatement(SQL);
			statement.setDouble(1, loyalty);
			statement.setString(2, playerName);
			updateSuccessful = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (updateSuccessful == 0) {
			SQL = "INSERT INTO " + tblSkills + " (`id` ,`player` ,`leadership` ,`loyalty`) VALUES (NULL , ?, NULL, ?);";
			try {
				statement = con.prepareStatement(SQL);
				statement.setString(1, playerName);
				statement.setDouble(2, loyalty);
				updateSuccessful = statement.executeUpdate();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			statement.close();
			con.close();
		} catch (SQLException e) {
		}

		loyaltyCache.put(playerName, loyalty);

		return updateSuccessful;
	}

	// ///////////
	// INSERT INTO `minecraft`.`Monarchy_Provinces` (`id`, `player`, `x`, `z`,
	// `permitted`, `invited`, `time`) VALUES (NULL, 'Cyprias', '-11', '16',
	// NULL, NULL, CURRENT_TIMESTAMP);

	public int claimProvince(String playerName, String worldName, int chunkX, int chunkZ) {
		int updateSuccessful = 0;

		Connection con = getSQLConnection();
		PreparedStatement statement = null;

		String SQL = "INSERT INTO `Monarchy_Provinces` (`id`, `player`, `world`, `x`, `z`, `parent`, `invited`, `time`) VALUES (NULL, ?, ?, ?, ?, NULL, NULL, CURRENT_TIMESTAMP);";

		try {
			statement = con.prepareStatement(SQL);
			statement.setString(1, playerName);
			statement.setString(2, worldName);
			statement.setInt(3, chunkX);
			statement.setInt(4, chunkZ);
			//statement.setInt(5, Config.defaultProvincePermitFlag);

			updateSuccessful = statement.executeUpdate();

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (updateSuccessful > 0)
			removeProvinceFromCache(worldName, chunkX, chunkZ);

		return updateSuccessful;
	}

	public void removeProvinceFromCache(String worldName, int chunkX, int chunkZ) {
		String together = worldName + "+" + chunkX + "+" + chunkZ;

		if (provinceLocCache.containsKey(together)) {
			int id = provinceLocCache.get(together);
			plugin.info("Removing provinceLocCache: " + together + " = " + id);

			if (provinceIDCache.containsKey(id)) {
				plugin.info("Removing provinceIDCache: " + id);
				provinceIDCache.remove(id);
			}
			// provinceIDCache.clear();

			provinceLocCache.remove(id);
		}
		Provinces.permittedCache.clear();
	}

	public void removeProvince(int id) {
		if (provinceIDCache.containsKey(id)) {
			String together = provinceIDCache.get(id).worldName + "+" + provinceIDCache.get(id).x + "+" + provinceIDCache.get(id).z;
			if (provinceLocCache.containsKey(together))
				provinceLocCache.remove(together);

			provinceIDCache.remove(id);
		}
	}

	public HashMap<Integer, provinceInfo> provinceIDCache = new HashMap<Integer, provinceInfo>();
	public HashMap<String, Integer> provinceLocCache = new HashMap<String, Integer>();

	// public HashMap<String, provinceInfo> provinceCache = new HashMap<String,
	// provinceInfo>();

	public provinceInfo getProvinceByID(int id) {
		if (id == 0)
			return null;

		if (provinceIDCache.containsKey(id))
			return provinceIDCache.get(id);

		provinceInfo province = null;
		String SQL = "SELECT * FROM `Monarchy_Provinces` WHERE `id` = ?";

		Connection con = getSQLConnection();

		try {

			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setInt(1, id);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				String together = result.getString(3) + "+" + result.getInt(4) + "+" + result.getInt(5);

				province = new provinceInfo(result.getInt(1), // id
					result.getString(2),// player
					result.getString(3),// world
					result.getInt(4),// x
					result.getInt(5),// z
					result.getInt(6),// parent
					//result.getInt(7),// permitted
					result.getString(7),// invited
					(result.getTimestamp(8).getTime() / 1000D) // time
				);

				provinceLocCache.put(together, id);
				provinceIDCache.put(id, province);

			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return province;
	}

	public provinceInfo getProvince(String world, int x, int z) {
		// String together=x+"+"+z;
		String together = world + "+" + x + "+" + z;

		if (provinceLocCache.containsKey(together))
			return getProvinceByID(provinceLocCache.get(together));

		provinceInfo province = null;
		String SQL = "SELECT * FROM `Monarchy_Provinces` WHERE `world` = ? AND `x` = ? AND `z` = ?";

		Connection con = getSQLConnection();

		int id = 0;

		try {

			PreparedStatement statement = con.prepareStatement(SQL);
			statement.setString(1, world);
			statement.setInt(2, x);
			statement.setInt(3, z);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				id = result.getInt(1);
				provinceLocCache.put(together, id);

				province = new provinceInfo(id,// id
					result.getString(2),// player
					result.getString(3),// world
					result.getInt(4),// x
					result.getInt(5),// z
					result.getInt(6),// parent
				//	result.getInt(7),// permitted
					result.getString(7),// invited
					(result.getTimestamp(8).getTime() / 1000D) // time
				);

			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return province;
	}



	public int deleteProvince(String worldName, int chunkX, int chunkZ) {
		String SQL = "DELETE FROM `Monarchy_Provinces` WHERE `world` = ? AND `x` = ? AND `z` = ?;";
		int success = 0;

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, worldName);
			statement.setInt(2, chunkX);
			statement.setInt(3, chunkZ);

			success = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (success > 0)
			removeProvinceFromCache(worldName, chunkX, chunkZ);

		return success;
	}

	public int transferProvince(String worldName, int chunkX, int chunkZ, String newOwner) {
		int success = 0;
		String SQL = "UPDATE `Monarchy_Provinces` SET `player` = ? WHERE `world` = ? AND `x` = ? AND `z` = ?;";

		try {
			Connection con = getSQLConnection();
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setString(1, newOwner);
			statement.setString(2, worldName);
			statement.setInt(3, chunkX);
			statement.setInt(4, chunkZ);

			success = statement.executeUpdate();

			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (success > 0)
			removeProvinceFromCache(worldName, chunkX, chunkZ);

		return success;
	}

	public int getProvinceCount(String playerName) {
		int count = 0;
		String query = "SELECT `id` FROM `Monarchy_Provinces` WHERE `player` = ?";

		Connection con = getSQLConnection();
		try {
			PreparedStatement statement = con.prepareStatement(query);
			statement.setString(1, playerName);

			ResultSet result = statement.executeQuery();
			while (result.next()) {
				count += 1;
			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return count;
	}

	public void chargeProvinceRent(Player player) {
		String playerName = player.getName();
		String worldName = player.getWorld().getName();

		String query = "SELECT * FROM `Monarchy_Provinces` WHERE `player` = ? AND `world` = ? ORDER BY `time` ASC;";

		Connection con = getSQLConnection();

		try {

			PreparedStatement statement = con.prepareStatement(query);
			statement.setString(1, playerName);
			statement.setString(2, worldName);

			ResultSet result = statement.executeQuery();

			double lastPaid, secondsSince;

			double rentTime;
			int rentDue;
			int totalDue = 0;
			int totalPaid = 0;

			List<provinceInfo> paidProvinces = new ArrayList<provinceInfo>();
			List<provinceInfo> lateProvinces = new ArrayList<provinceInfo>();

			int x, z;
			provinceInfo province;
			while (result.next()) {

				lastPaid = result.getTimestamp(8).getTime() / 1000D;

				secondsSince = (Monarchy.getUnixTime() - lastPaid);

				rentTime = (secondsSince / (Config.provinceRentTime * 60 * 60));
				rentDue = (int) Math.floor(rentTime * Config.provinceRentFee);
				totalDue += rentDue;

				// x = result.getInt(4);
				// z = result.getInt(5);

				// plugin.info("x:"+x+",z:"+z+", seconds: " + secondsSince +
				// ", rentTime:" +rentTime + ", rentDue:"+rentDue);
				// provinceRentFee

				province = new provinceInfo(result.getInt(1), // id
					result.getString(2),// player
					result.getString(3),// world
					result.getInt(4),// x
					result.getInt(5),// z
					result.getInt(6),// parent
					//result.getInt(7),// permitted
					result.getString(7),// invited
					(result.getTimestamp(8).getTime() / 1000D) // time
				);

				if (rentDue > 0) {
					if (Monarchy.getTotalExperience(player) >= rentDue) {
						totalPaid += rentDue;
						Monarchy.takeExp(player, rentDue);
						paidProvinces.add(province);
					} else {
						lateProvinces.add(province);
					}
				}
			}

			if (totalPaid > 0)
				plugin.sendMessage(player, F("stPaidProvinceFees", totalPaid));

			// plugin.info(playerName + " owes " + totalDue + " exp.");

			/**/
			int success;

			query = "UPDATE `Monarchy_Provinces` SET `time` = CURRENT_TIMESTAMP WHERE `id` = ?;";
			statement = con.prepareStatement(query);
			for (int i = 0; i < paidProvinces.size(); i++) {
				statement.setInt(1, paidProvinces.get(i).id);
				success = statement.executeUpdate();
				// plugin.info("Updated " + paidProvinces.get(i).id +
				// " to current date.");
			}

			query = "DELETE FROM `Monarchy_Provinces` WHERE `id` = ?;";
			statement = con.prepareStatement(query);
			for (int i = lateProvinces.size() - 1; i >= 0; i--) {
				lastPaid = lateProvinces.get(i).time;
				secondsSince = (Monarchy.getUnixTime() - lastPaid);

				if (secondsSince > (Config.provinceRentExpire * 60 * 60)) {
					statement.setInt(1, lateProvinces.get(i).id);
					success = statement.executeUpdate();
					// plugin.info("Removed " + lateProvinces.get(i) +
					// " from db.");
					plugin.sendMessage(player, F("stLostProvinceClaim", worldName, lateProvinces.get(i).x * 16, lateProvinces.get(i).z * 16));
				} else {
					int due = (int) ((Config.provinceRentExpire * 60 * 60) - secondsSince) / 60 / 60;
					plugin.sendMessage(player, F("stProvinceRentIsLate", worldName, lateProvinces.get(i).x * 16, lateProvinces.get(i).z * 16, due));

				}
			}

			statement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
