package com.tkshop.plugin.shop;

import java.util.List;
import java.util.Map;

public class ShopCategory {
   private final String id;
   private final String title;
   private final String material;
   private final String displayName;
   private final List<String> lore;
   private final int slot;
   private final Map<String, ShopItemDef> items;

   public ShopCategory(String id, String title, String material, String displayName, List<String> lore, int slot, Map<String, ShopItemDef> items) {
      this.id = id;
      this.title = title;
      this.material = material;
      this.displayName = displayName;
      this.lore = lore;
      this.slot = slot;
      this.items = items;
   }

   public String getId() {
      return this.id;
   }

   public String getTitle() {
      return this.title;
   }

   public String getMaterial() {
      return this.material;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public List<String> getLore() {
      return this.lore;
   }

   public int getSlot() {
      return this.slot;
   }

   public Map<String, ShopItemDef> getItems() {
      return this.items;
   }
}
