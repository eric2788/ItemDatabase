package com.ericlam.item;

import com.ericlam.config.Config;
import com.ericlam.main.ItemDatabase;
import com.ericlam.mysql.MySQLManager;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ItemDatabaseManager {
    private Plugin plugin;
    private static ItemDatabaseManager instance;
    private MySQLManager mysql;
    private String table;

    private ItemDatabaseManager(){
        plugin = ItemDatabase.plugin;
        mysql = MySQLManager.getInstance();
        table = Config.table;
    }

    public static ItemDatabaseManager getInstance() {
        if (instance == null) instance = new ItemDatabaseManager();
        return instance;
    }

    private boolean hasItem(String itemName, Player player){
        try(Connection connection = mysql.getConneciton();PreparedStatement statement = connection.prepareStatement("SELECT `PlayerUUID` FROM `"+table+"` WHERE PlayerUUID=? AND ItemName=?")){
            statement.setString(1,player.getUniqueId().toString());
            statement.setString(2,itemName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean hasItem(String itemName){
        try(Connection connection = mysql.getConneciton();PreparedStatement statement = connection.prepareStatement("SELECT `PlayerUUID` FROM `"+table+"` WHERE ItemName=?")){
            statement.setString(1,itemName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> checkList(Player player){
        List<String> items = new ArrayList<>();
        try(Connection connection = mysql.getConneciton();PreparedStatement statement = connection.prepareStatement("SELECT `ItemName`,`ItemStack` FROM `"+table+"` WHERE PlayerUUID=?")){
            statement.setString(1,player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                ItemStack item = itemStackFromBase64(resultSet.getString("ItemStack"));
                items.add("- §e"+resultSet.getString("ItemName")+"§8: "+item.getType().toString()+" x"+item.getAmount()+"\n§7Name: §f"+item.getItemMeta().getDisplayName()+"\n§7Lore: §f"+item.getItemMeta().getLore());
            }
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean uploadItem(ItemStack itemStack, String itemName, Player player){
        if (hasItem(itemName)) {
            player.sendMessage(Config.exist);
            return false;
        }
        try(Connection connection = mysql.getConneciton(); PreparedStatement statement = connection.prepareStatement("INSERT INTO `"+ table +"` VALUES (?,?,?)")){
            statement.setString(1,player.getUniqueId().toString());
            statement.setString(2,itemStackToBase64(itemStack));
            statement.setString(3,itemName);
            statement.execute();
            player.getInventory().remove(itemStack);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean takeBackItem(String itemName, Player player){
        ItemStack item;
       try(Connection connection = mysql.getConneciton();
           PreparedStatement Get = connection.prepareStatement("SELECT `ItemStack` FROM `"+table+"` WHERE `PlayerUUID`=? AND `ItemName`=?");
           PreparedStatement Remove = connection.prepareStatement("DELETE FROM `"+table+"` WHERE `ItemName`=?")){
           Get.setString(1,player.getUniqueId().toString());
           Get.setString(2,itemName);
           ResultSet resultSet = Get.executeQuery();
           if (resultSet.next()) {
               item = itemStackFromBase64(resultSet.getString("ItemStack"));
           }else {
               player.sendMessage(Config.no_exist);
               return false;
           }
           if (item == null) return false;
           PlayerInventory bag = player.getInventory();
           for (ItemStack stack : bag) {
               if (stack == null || stack.isSimilar(new ItemStack(Material.AIR))) {
                   bag.addItem(item);
                   Remove.setString(1, itemName);
                   Remove.execute();
                   return true;
               }
           }
           player.sendMessage(Config.full_inv);
           return false;
       } catch (SQLException e) {
           e.printStackTrace();
           return false;
       }
    }

    private String itemStackArrayToBase64(ItemStack[] items){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }

    private String itemStackToBase64(ItemStack items){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Save element in the list
            dataOutput.writeObject(items);


            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    private ItemStack itemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item;

            // Read the serialized inventory
            item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return new ItemStack(Material.AIR);
        }
    }

}
