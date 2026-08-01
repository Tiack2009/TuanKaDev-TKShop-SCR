package com.tkshop.plugin.economy;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TKShardsBridge {
   private final Plugin tkshardsPlugin = Bukkit.getPluginManager().getPlugin("TKShards");
   private Object currencyManager;
   private Method getBalanceMethod;
   private Method addBalanceMethod;
   private Method removeBalanceMethod;
   private String currencyName = "TKShards";

   public TKShardsBridge() {
      if (this.tkshardsPlugin != null) {
         try {
            Method currencyAccessor = this.tkshardsPlugin.getClass().getMethod("currency");
            this.currencyManager = currencyAccessor.invoke(this.tkshardsPlugin);
            this.getBalanceMethod = this.currencyManager.getClass().getMethod("getBalance", UUID.class);
            this.addBalanceMethod = this.currencyManager.getClass().getMethod("addBalance", UUID.class, Long.TYPE);
            this.removeBalanceMethod = this.currencyManager.getClass().getMethod("removeBalance", UUID.class, Long.TYPE);
            Method langAccessor = this.tkshardsPlugin.getClass().getMethod("lang");
            Object lang = langAccessor.invoke(this.tkshardsPlugin);
            Method currencyNameMethod = lang.getClass().getMethod("currency");
            this.currencyName = (String)currencyNameMethod.invoke(lang);
         } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[TkShop] Khong the ket noi voi TKShards: " + e.getMessage());
         }

      }
   }

   public boolean isAvailable() {
      return this.tkshardsPlugin != null && this.currencyManager != null;
   }

   public String currencyName() {
      return this.currencyName;
   }

   public long getBalance(UUID uuid) {
      if (!this.isAvailable()) {
         return 0L;
      } else {
         try {
            return (Long)this.getBalanceMethod.invoke(this.currencyManager, uuid);
         } catch (Exception var3) {
            return 0L;
         }
      }
   }

   public boolean removeBalance(UUID uuid, long amount) {
      if (!this.isAvailable()) {
         return false;
      } else {
         try {
            return (Boolean)this.removeBalanceMethod.invoke(this.currencyManager, uuid, amount);
         } catch (Exception var5) {
            return false;
         }
      }
   }

   public void addBalance(UUID uuid, long amount) {
      if (this.isAvailable()) {
         try {
            this.addBalanceMethod.invoke(this.currencyManager, uuid, amount);
         } catch (Exception var5) {
         }

      }
   }
}
