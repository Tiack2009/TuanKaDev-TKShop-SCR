package com.tkshop.plugin.shop;

import java.util.List;

public class ShopItemDef {
   private final String id;
   private final String title;
   private final String material;
   private final boolean customDisplayNameEnabled;
   private final String customDisplayName;
   private final boolean executeCommand;
   private final List<String> commands;
   private final List<String> enchantments;
   private final int amount;
   private final int maxAmount;
   private final double price;
   private final int slot;
   private final String economy;

   public ShopItemDef(String id, String title, String material, boolean customDisplayNameEnabled, String customDisplayName, boolean executeCommand, List<String> commands, List<String> enchantments, int amount, int maxAmount, double price, int slot, String economy) {
      this.id = id;
      this.title = title;
      this.material = material;
      this.customDisplayNameEnabled = customDisplayNameEnabled;
      this.customDisplayName = customDisplayName;
      this.executeCommand = executeCommand;
      this.commands = commands;
      this.enchantments = enchantments;
      this.amount = Math.max(amount, 1);
      this.maxAmount = Math.max(maxAmount, this.amount);
      this.price = price;
      this.slot = slot;
      this.economy = economy;
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

   public boolean isCustomDisplayNameEnabled() {
      return this.customDisplayNameEnabled;
   }

   public String getCustomDisplayName() {
      return this.customDisplayName;
   }

   public boolean isExecuteCommand() {
      return this.executeCommand;
   }

   public List<String> getCommands() {
      return this.commands;
   }

   public List<String> getEnchantments() {
      return this.enchantments;
   }

   public int getAmount() {
      return this.amount;
   }

   public int getMaxAmount() {
      return this.maxAmount;
   }

   public double getPrice() {
      return this.price;
   }

   public int getSlot() {
      return this.slot;
   }

   public String getEconomy() {
      return this.economy;
   }

   public String displayName() {
      return this.customDisplayNameEnabled && this.customDisplayName != null && !this.customDisplayName.isEmpty() ? this.customDisplayName : this.title;
   }

   public double unitPrice() {
      return this.amount <= 0 ? this.price : this.price / (double)this.amount;
   }
}
