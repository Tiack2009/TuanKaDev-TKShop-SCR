package com.tkshop.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class MessageUtil {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   private MessageUtil() {
   }

   public static String color(String input) {
      if (input != null && !input.isEmpty()) {
         Matcher matcher = HEX_PATTERN.matcher(input);
         StringBuilder buffer = new StringBuilder();

         while(matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
         }

         matcher.appendTail(buffer);
         return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
      } else {
         return "";
      }
   }
}
