package com.tkshop.plugin.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultBridge {
   private Economy economy;

   public VaultBridge() {
      if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
         RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
         if (provider != null) {
            this.economy = (Economy)provider.getProvider();
         }

      }
   }

   public boolean isAvailable() {
      return this.economy != null;
   }

   public double getBalance(OfflinePlayer player) {
      return this.isAvailable() ? this.economy.getBalance(player) : (double)0.0F;
   }

   public boolean withdraw(OfflinePlayer player, double amount) {
      if (!this.isAvailable()) {
         return false;
      } else {
         return this.economy.getBalance(player) < amount ? false : this.economy.withdrawPlayer(player, amount).transactionSuccess();
      }
   }
}
