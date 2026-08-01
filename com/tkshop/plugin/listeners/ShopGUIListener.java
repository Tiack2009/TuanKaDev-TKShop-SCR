package com.tkshop.plugin.listeners;

import com.tkshop.plugin.TkShopPlugin;
import com.tkshop.plugin.shop.ShopCategory;
import com.tkshop.plugin.shop.ShopHolder;
import com.tkshop.plugin.shop.ShopItemDef;
import com.tkshop.plugin.shop.ShopManager;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class ShopGUIListener implements Listener {
   private final TkShopPlugin plugin;

   public ShopGUIListener(TkShopPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onDrag(InventoryDragEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof ShopHolder) {
         int topSize = event.getView().getTopInventory().getSize();
         boolean touchesShop = event.getRawSlots().stream().anyMatch((rawSlot) -> rawSlot < topSize);
         if (touchesShop) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      InventoryHolder var3 = event.getView().getTopInventory().getHolder();
      if (var3 instanceof ShopHolder holder) {
         if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof ShopHolder) {
            event.setCancelled(true);
            HumanEntity var4 = event.getWhoClicked();
            if (var4 instanceof Player) {
               Player player = (Player)var4;
               if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getInventory().getSize()) {
                  if (!this.plugin.shop().isOnCooldown(player.getUniqueId())) {
                     ShopManager shop = this.plugin.shop();
                     int slot = event.getRawSlot();
                     switch (holder.getType()) {
                        case MAIN -> this.handleMainClick(player, slot);
                        case CATEGORY -> this.handleCategoryClick(player, holder, slot);
                        case CONFIRM -> this.handleConfirmClick(player, holder, slot, event);
                     }

                  }
               }
            }
         }
      }
   }

   private void handleMainClick(Player player, int slot) {
      for(ShopCategory category : this.plugin.shop().getCategories().values()) {
         if (category.getSlot() == slot) {
            player.openInventory(this.plugin.shop().buildCategoryMenu(category.getId()));
            return;
         }
      }

   }

   private void handleCategoryClick(Player player, ShopHolder holder, int slot) {
      if (slot == this.plugin.shop().getControlSlot("back-item")) {
         player.openInventory(this.plugin.shop().buildMainMenu());
      } else {
         ShopCategory category = (ShopCategory)this.plugin.shop().getCategories().get(holder.getCategoryId());
         if (category != null) {
            for(ShopItemDef item : category.getItems().values()) {
               if (item.getSlot() == slot) {
                  this.plugin.shop().resetQuantity(player.getUniqueId(), item);
                  player.openInventory(this.plugin.shop().buildConfirmMenu(category.getId(), item.getId(), player.getUniqueId()));
                  return;
               }
            }

         }
      }
   }

   private void handleConfirmClick(Player player, ShopHolder holder, int slot, InventoryClickEvent event) {
      ShopManager shop = this.plugin.shop();
      ShopCategory category = (ShopCategory)shop.getCategories().get(holder.getCategoryId());
      if (category != null) {
         ShopItemDef item = (ShopItemDef)category.getItems().get(holder.getItemId());
         if (item != null) {
            if (slot != shop.getControlSlot("back-item") && slot != shop.getControlSlot("cancel-item")) {
               if (slot == shop.getControlSlot("confirm-item")) {
                  ShopManager.PurchaseResult result = shop.purchase(player, item);
                  if (result.success()) {
                     String priceText = result.price() == Math.floor(result.price()) ? String.valueOf((long)result.price()) : String.format("%.2f", result.price());
                     player.sendMessage(this.plugin.lang().get("purchase-success", "amount", String.valueOf(result.amount()), "item", item.displayName(), "price", priceText));
                     player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.4F);
                     shop.refreshConfirmMenu(event.getInventory(), category.getId(), item.getId(), player.getUniqueId());
                  } else {
                     player.sendMessage(this.plugin.lang().get(result.failReasonKey(), "currency", this.plugin.tkshards().currencyName()));
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                  }

               } else {
                  if (slot == shop.getControlSlot("remove64-item")) {
                     shop.adjustQuantity(player.getUniqueId(), item, -64);
                  } else if (slot == shop.getControlSlot("remove10-item")) {
                     shop.adjustQuantity(player.getUniqueId(), item, -10);
                  } else if (slot == shop.getControlSlot("remove1-item")) {
                     shop.adjustQuantity(player.getUniqueId(), item, -1);
                  } else if (slot == shop.getControlSlot("add1-item")) {
                     shop.adjustQuantity(player.getUniqueId(), item, 1);
                  } else if (slot == shop.getControlSlot("add10-item")) {
                     shop.adjustQuantity(player.getUniqueId(), item, 10);
                  } else {
                     if (slot != shop.getControlSlot("add64-item")) {
                        return;
                     }

                     shop.adjustQuantity(player.getUniqueId(), item, 64);
                  }

                  player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6F, 1.2F);
                  shop.refreshConfirmMenu(event.getInventory(), category.getId(), item.getId(), player.getUniqueId());
               }
            } else {
               shop.clearQuantity(player.getUniqueId());
               player.openInventory(shop.buildCategoryMenu(category.getId()));
            }
         }
      }
   }
}
