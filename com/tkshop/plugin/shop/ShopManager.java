package com.tkshop.plugin.shop;

import com.tkshop.plugin.TkShopPlugin;
import com.tkshop.plugin.util.MessageUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopManager {
   private final TkShopPlugin plugin;
   private YamlConfiguration cfg;
   private final Map<String, ShopCategory> categories = new LinkedHashMap();
   private final Map<UUID, Integer> pendingQuantity = new ConcurrentHashMap();
   private final Map<UUID, Long> lastClick = new ConcurrentHashMap();
   private static final int PREVIEW_SLOT = 12;

   public ShopManager(TkShopPlugin plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      File file = new File(this.plugin.getDataFolder(), "shops.yml");
      this.cfg = YamlConfiguration.loadConfiguration(file);
      this.categories.clear();
      ConfigurationSection catSection = this.cfg.getConfigurationSection("categories");
      if (catSection != null) {
         for(String catId : catSection.getKeys(false)) {
            ConfigurationSection cat = catSection.getConfigurationSection(catId);
            if (cat != null) {
               Map<String, ShopItemDef> items = new LinkedHashMap();

               for(String key : cat.getKeys(false)) {
                  if (!List.of("title", "material", "displayName", "lore", "slot").contains(key)) {
                     ConfigurationSection itemSec = cat.getConfigurationSection(key);
                     if (itemSec != null) {
                        items.put(key, new ShopItemDef(key, itemSec.getString("title", key), itemSec.getString("material", "STONE"), itemSec.getBoolean("custom-displayname-enabled", false), itemSec.getString("custom-displayname", ""), itemSec.getBoolean("execute-command", false), itemSec.getStringList("command"), itemSec.getStringList("enchantments"), itemSec.getInt("amount", 1), itemSec.getInt("max-amount", 64), itemSec.getDouble("price", (double)0.0F), itemSec.getInt("slot", 0), itemSec.getString("economy", this.plugin.getConfig().getString("default-currency", "vault"))));
                     }
                  }
               }

               this.categories.put(catId, new ShopCategory(catId, cat.getString("title", catId), cat.getString("material", "CHEST"), cat.getString("displayName", catId), cat.getStringList("lore"), cat.getInt("slot", 0), items));
            }
         }

      }
   }

   public Map<String, ShopCategory> getCategories() {
      return this.categories;
   }

   public boolean isOnCooldown(UUID uuid) {
      long cooldown = this.plugin.getConfig().getLong("gui-click-cooldown-ms", 150L);
      long now = System.currentTimeMillis();
      Long last = (Long)this.lastClick.get(uuid);
      if (last != null && now - last < cooldown) {
         return true;
      } else {
         this.lastClick.put(uuid, now);
         return false;
      }
   }

   public Inventory buildMainMenu() {
      ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN, (String)null, (String)null);
      String title = this.plugin.getConfig().getString("main-menu-title", "&8Shop TkShop");
      int size = this.plugin.getConfig().getInt("main-menu-size", 27);
      Inventory inv = this.plugin.getServer().createInventory(holder, size, MessageUtil.color(title));
      holder.setInventory(inv);

      for(ShopCategory category : this.categories.values()) {
         Material material = Material.matchMaterial(category.getMaterial());
         if (material != null) {
            ItemStack stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
               meta.setDisplayName(MessageUtil.color(category.getDisplayName()));
               meta.setLore(category.getLore().stream().map(MessageUtil::color).toList());
               stack.setItemMeta(meta);
            }

            if (category.getSlot() >= 0 && category.getSlot() < size) {
               inv.setItem(category.getSlot(), stack);
            }
         }
      }

      return inv;
   }

   public Inventory buildCategoryMenu(String categoryId) {
      ShopCategory category = (ShopCategory)this.categories.get(categoryId);
      if (category == null) {
         return null;
      } else {
         ShopHolder holder = new ShopHolder(ShopHolder.Type.CATEGORY, categoryId, (String)null);
         Inventory inv = this.plugin.getServer().createInventory(holder, 27, MessageUtil.color(category.getTitle()));
         holder.setInventory(inv);

         for(ShopItemDef item : category.getItems().values()) {
            Material material = Material.matchMaterial(item.getMaterial());
            if (material != null) {
               ItemStack stack = this.buildDisplayItem(item, material, item.getAmount());
               if (item.getSlot() >= 0 && item.getSlot() < 27) {
                  inv.setItem(item.getSlot(), stack);
               }
            }
         }

         this.putControlItem(inv, "back-item");
         return inv;
      }
   }

   public Inventory buildConfirmMenu(String categoryId, String itemId, UUID buyer) {
      ShopCategory category = (ShopCategory)this.categories.get(categoryId);
      if (category == null) {
         return null;
      } else {
         ShopItemDef item = (ShopItemDef)category.getItems().get(itemId);
         if (item == null) {
            return null;
         } else {
            ShopHolder holder = new ShopHolder(ShopHolder.Type.CONFIRM, categoryId, itemId);
            Inventory inv = this.plugin.getServer().createInventory(holder, 27, MessageUtil.color(item.getTitle()));
            holder.setInventory(inv);
            this.fillConfirmMenu(inv, category, item, buyer);
            return inv;
         }
      }
   }

   public void refreshConfirmMenu(Inventory inv, String categoryId, String itemId, UUID buyer) {
      ShopCategory category = (ShopCategory)this.categories.get(categoryId);
      if (category != null) {
         ShopItemDef item = (ShopItemDef)category.getItems().get(itemId);
         if (item != null) {
            this.fillConfirmMenu(inv, category, item, buyer);
         }
      }
   }

   private void fillConfirmMenu(Inventory inv, ShopCategory category, ShopItemDef item, UUID buyer) {
      int quantity = (Integer)this.pendingQuantity.computeIfAbsent(buyer, (k) -> item.getAmount());
      Material material = Material.matchMaterial(item.getMaterial());
      if (material == null) {
         material = Material.STONE;
      }

      ItemStack preview = this.buildDisplayItem(item, material, quantity);
      inv.setItem(12, preview);
      this.putControlItem(inv, "remove64-item");
      this.putControlItem(inv, "remove10-item");
      this.putControlItem(inv, "remove1-item");
      this.putControlItem(inv, "add1-item");
      this.putControlItem(inv, "add10-item");
      this.putControlItem(inv, "add64-item");
      this.putControlItem(inv, "confirm-item");
      this.putControlItem(inv, "cancel-item");
      this.putControlItem(inv, "back-item");
   }

   public int getPreviewSlot() {
      return 12;
   }

   private void putControlItem(Inventory inv, String key) {
      this.putControlItem(inv, key, 0);
   }

   private void putControlItem(Inventory inv, String key, int maxPlaceholder) {
      ConfigurationSection sec = this.cfg.getConfigurationSection(key);
      if (sec != null) {
         Material material = Material.matchMaterial(sec.getString("material", "STONE"));
         if (material == null) {
            material = Material.STONE;
         }

         ItemStack stack = new ItemStack(material);
         ItemMeta meta = stack.getItemMeta();
         if (meta != null) {
            String name = sec.getString("displayName", key).replace("{max}", String.valueOf(maxPlaceholder));
            meta.setDisplayName(MessageUtil.color(name));
            meta.setLore(sec.getStringList("lore").stream().map((l) -> l.replace("{max}", String.valueOf(maxPlaceholder))).map(MessageUtil::color).toList());
            stack.setItemMeta(meta);
         }

         int slot = sec.getInt("slot", 0);
         if (slot >= 0 && slot < inv.getSize()) {
            inv.setItem(slot, stack);
         }

      }
   }

   private ItemStack buildDisplayItem(ShopItemDef item, Material material, int quantity) {
      ItemStack stack = new ItemStack(material, Math.max(1, Math.min(quantity, 64)));
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(MessageUtil.color(item.displayName()));
         List<String> lore = new ArrayList();
         double price = item.unitPrice() * (double)quantity;
         List<String> template = "tkshards".equalsIgnoreCase(item.getEconomy()) ? this.cfg.getStringList("shards-items-lore") : this.cfg.getStringList("items-lore");
         String currency = "tkshards".equalsIgnoreCase(item.getEconomy()) ? this.plugin.tkshards().currencyName() : "$";

         for(String line : template) {
            lore.add(line.replace("{price}", this.formatPrice(price)).replace("{currency}", currency));
         }

         lore.add("&7So luong: &f" + quantity);
         meta.setLore(lore.stream().map(MessageUtil::color).toList());

         for(String ench : item.getEnchantments()) {
            this.applyEnchant(meta, ench);
         }

         stack.setItemMeta(meta);
      }

      return stack;
   }

   private void applyEnchant(ItemMeta meta, String raw) {
      String[] parts = raw.split(":");
      if (parts.length == 2) {
         Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
         if (enchantment != null) {
            try {
               int level = Integer.parseInt(parts[1]);
               meta.addEnchant(enchantment, level, true);
            } catch (NumberFormatException var6) {
            }

         }
      }
   }

   private String formatPrice(double price) {
      return price == Math.floor(price) ? String.valueOf((long)price) : String.format("%.2f", price);
   }

   public int getControlSlot(String key) {
      ConfigurationSection sec = this.cfg.getConfigurationSection(key);
      return sec == null ? -1 : sec.getInt("slot", -1);
   }

   public int getQuantity(UUID uuid, ShopItemDef item) {
      return (Integer)this.pendingQuantity.computeIfAbsent(uuid, (k) -> item.getAmount());
   }

   public void adjustQuantity(UUID uuid, ShopItemDef item, int delta) {
      int current = this.getQuantity(uuid, item);
      int updated = Math.max(item.getAmount(), Math.min(item.getMaxAmount(), current + delta));
      this.pendingQuantity.put(uuid, updated);
   }

   public void setQuantity(UUID uuid, int value) {
      this.pendingQuantity.put(uuid, value);
   }

   public void resetQuantity(UUID uuid, ShopItemDef item) {
      this.pendingQuantity.put(uuid, item.getAmount());
   }

   public void clearQuantity(UUID uuid) {
      this.pendingQuantity.remove(uuid);
   }

   public PurchaseResult purchase(Player player, ShopItemDef item) {
      int quantity = this.getQuantity(player.getUniqueId(), item);
      double totalPrice = item.unitPrice() * (double)quantity;
      if ("tkshards".equalsIgnoreCase(item.getEconomy())) {
         if (!this.plugin.tkshards().isAvailable()) {
            return ShopManager.PurchaseResult.fail("shards-unavailable");
         }

         long price = Math.round(totalPrice);
         if (!this.plugin.tkshards().removeBalance(player.getUniqueId(), price)) {
            return ShopManager.PurchaseResult.fail("not-enough-shards");
         }
      } else {
         if (!this.plugin.vault().isAvailable()) {
            return ShopManager.PurchaseResult.fail("vault-unavailable");
         }

         if (!this.plugin.vault().withdraw(player, totalPrice)) {
            return ShopManager.PurchaseResult.fail("not-enough-money");
         }
      }

      if (item.isExecuteCommand()) {
         for(String command : item.getCommands()) {
            String resolved = command.replace("{player}", player.getName()).replace("{amount}", String.valueOf(quantity));
            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), resolved);
         }
      } else {
         Material material = Material.matchMaterial(item.getMaterial());
         if (material != null) {
            ItemStack give = new ItemStack(material, quantity);
            player.getInventory().addItem(new ItemStack[]{give});
         }
      }

      return ShopManager.PurchaseResult.success(quantity, totalPrice);
   }

   public static record PurchaseResult(boolean success, String failReasonKey, int amount, double price) {
      public static PurchaseResult success(int amount, double price) {
         return new PurchaseResult(true, (String)null, amount, price);
      }

      public static PurchaseResult fail(String reasonKey) {
         return new PurchaseResult(false, reasonKey, 0, (double)0.0F);
      }
   }
}
