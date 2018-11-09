package com.ericlam.main;

import com.ericlam.config.Config;
import com.ericlam.item.ItemDatabaseManager;
import com.ericlam.mysql.MySQLManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ItemDatabase extends JavaPlugin {
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Config cf = Config.getInstance();
        if (!cf.getConfig().getBoolean("enabled")){
            this.getLogger().info("You haven't correctly set your sql data config, plz change enabled: true after you have set it correctly.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try(Connection connection = MySQLManager.getInstance().getConneciton(); PreparedStatement statement = connection.prepareStatement
                ("CREATE TABLE IF NOT EXISTS `"+ Config.table +"` (`PlayerUUID` VARCHAR(40) NOT NULL ,  `ItemStack` LONGTEXT NOT NULL, `ItemName` VARCHAR(100) NOT NULL PRIMARY KEY)")
        ){
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("you are not player");
            return false;
        }

        if (!command.getName().equalsIgnoreCase("itembase")){
            return false;
        }

        ItemDatabaseManager databaseManager = ItemDatabaseManager.getInstance();
        Player player = (Player) sender;
        String cmd = args[0];

        if (cmd.equalsIgnoreCase("list")){
            player.sendMessage(Config.list);
            player.sendMessage(databaseManager.checkList(player).toArray(new String[0]));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Config.few_arug);
            player.sendMessage(Config.help);
            return false;
        }

        String itemName = args[1];

        if (cmd.equalsIgnoreCase("upload")){
            ItemStack uploadItem = player.getInventory().getItemInMainHand();

            if(uploadItem == null || uploadItem.getType() == Material.AIR){
                player.sendMessage(Config.air);
                return false;
            }

            boolean upload = databaseManager.uploadItem(uploadItem,itemName,player);
            player.sendMessage((upload ? Config.upload_success : Config.upload_fail));
            return upload;
        }

        if (cmd.equalsIgnoreCase("take")){
            boolean take = databaseManager.takeBackItem(itemName,player);
            player.sendMessage((take ? Config.take_success : Config.take_fail));
            return take;
        }

        player.sendMessage(Config.help);
        return false;
    }
}
