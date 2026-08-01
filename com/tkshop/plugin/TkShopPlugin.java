package com.tkshop.plugin;

import com.tkshop.plugin.commands.TkShopCommand;
import com.tkshop.plugin.economy.TKShardsBridge;
import com.tkshop.plugin.economy.VaultBridge;
import com.tkshop.plugin.listeners.ShopGUIListener;
import com.tkshop.plugin.shop.ShopManager;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TkShopPlugin extends JavaPlugin {
   private Lang lang;
   private ShopManager shopManager;
   private VaultBridge vaultBridge;
   private TKShardsBridge tkShardsBridge;

   public void onEnable() {
      this.saveDefaultConfig();
      this.saveResourceIfMissing("messages.yml");
      this.saveResourceIfMissing("shops.yml");
      this.lang = new Lang(this);
      this.vaultBridge = new VaultBridge();
      this.tkShardsBridge = new TKShardsBridge();
      this.shopManager = new ShopManager(this);
      this.getCommand("tkshop").setExecutor(new TkShopCommand(this));
      Bukkit.getPluginManager().registerEvents(new ShopGUIListener(this), this);
      if (!this.vaultBridge.isAvailable()) {
         this.getLogger().warning("Khong tim thay Vault - item dung economy 'vault' se khong mua duoc.");
      }

      if (!this.tkShardsBridge.isAvailable()) {
         this.getLogger().warning("Khong tim thay plugin TKShards - item dung economy 'tkshards' se khong mua duoc.");
      }

      this.getLogger().info("TkShop da bat.");
   }

   private void saveResourceIfMissing(String path) {
      File file = new File(this.getDataFolder(), path);
      if (!file.exists()) {
         this.saveResource(path, false);
      }

   }

   public void reload() {
      this.reloadConfig();
      this.lang.reload();
      this.shopManager.reload();
      this.tkShardsBridge = new TKShardsBridge();
      this.vaultBridge = new VaultBridge();
   }

   public Lang lang() {
      return this.lang;
   }

   public ShopManager shop() {
      return this.shopManager;
   }

   public VaultBridge vault() {
      return this.vaultBridge;
   }

   public TKShardsBridge tkshards() {
      return this.tkShardsBridge;
   }
}
