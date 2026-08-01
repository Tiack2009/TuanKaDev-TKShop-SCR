package com.tkshop.plugin;

import com.tkshop.plugin.util.MessageUtil;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
   private final TkShopPlugin plugin;
   private YamlConfiguration messages;
   private String prefix;

   public Lang(TkShopPlugin plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      File file = new File(this.plugin.getDataFolder(), "messages.yml");
      if (!file.exists()) {
         this.plugin.saveResource("messages.yml", false);
      }

      this.messages = YamlConfiguration.loadConfiguration(file);
      this.prefix = this.messages.getString("prefix", "");
   }

   public String get(String path, String... replacements) {
      String raw = this.messages.getString(path, "");
      if (raw.isEmpty()) {
         return "";
      } else {
         raw = raw.replace("{prefix}", this.prefix);

         for(int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
         }

         return MessageUtil.color(raw);
      }
   }
}
