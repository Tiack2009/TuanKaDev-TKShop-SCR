package com.tkshop.plugin.commands;

import com.tkshop.plugin.TkShopPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TkShopCommand implements CommandExecutor {
   private final TkShopPlugin plugin;

   public TkShopCommand(TkShopPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
         if (!sender.hasPermission("tkshop.admin")) {
            sender.sendMessage(this.plugin.lang().get("no-permission"));
            return true;
         } else {
            try {
               this.plugin.reload();
               sender.sendMessage(this.plugin.lang().get("reload-success"));
            } catch (Exception e) {
               sender.sendMessage(this.plugin.lang().get("reload-failed", "error", e.getMessage()));
            }

            return true;
         }
      } else if (sender instanceof Player) {
         Player player = (Player)sender;
         if (!player.hasPermission("tkshop.use")) {
            player.sendMessage(this.plugin.lang().get("no-permission"));
            return true;
         } else if (this.plugin.shop().getCategories().isEmpty()) {
            player.sendMessage(this.plugin.lang().get("shop-empty"));
            return true;
         } else {
            player.openInventory(this.plugin.shop().buildMainMenu());
            return true;
         }
      } else {
         sender.sendMessage(this.plugin.lang().get("players-only"));
         return true;
      }
   }
}
