package com.tkshop.plugin.shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShopHolder implements InventoryHolder {
   private final Type type;
   private final String categoryId;
   private final String itemId;
   private Inventory inventory;

   public ShopHolder(Type type, String categoryId, String itemId) {
      this.type = type;
      this.categoryId = categoryId;
      this.itemId = itemId;
   }

   public Type getType() {
      return this.type;
   }

   public String getCategoryId() {
      return this.categoryId;
   }

   public String getItemId() {
      return this.itemId;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public static enum Type {
      MAIN,
      CATEGORY,
      CONFIRM;

      // $FF: synthetic method
      private static Type[] $values() {
         return new Type[]{MAIN, CATEGORY, CONFIRM};
      }
   }
}
