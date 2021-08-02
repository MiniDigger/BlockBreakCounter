package dev.benndorf.blockbreakcounter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class BlockBreakCounter extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    this.getServer().getPluginManager().registerEvents(this, this);
    Objects.requireNonNull(this.getCommand("blockbreakcounter")).setExecutor(this);
    this.saveDefaultConfig();
    // throw hard error on enable
    for(final String material : this.getConfig().getStringList("materials")) {
      Material.valueOf(material);
    }
  }

  @EventHandler
  public void onBlockBreak(final @NotNull BlockBreakEvent event) {
    final String name = event.getPlayer().getName();
    if (this.setScore(name, this.getScore(name) + 1) % this.getConfig().getInt("interval", 100) == 0) {
      final Block targetBlock = event.getPlayer().getTargetBlock(20);
      if (targetBlock != null) {
        final Block relative = targetBlock.getRelative(BlockFace.UP);
        final List<String> list = this.getConfig().getStringList("materials");
        final int index = ThreadLocalRandom.current().nextInt(list.size());
        relative.setType(Material.valueOf(list.get(index)));
      }
    }
  }

  public Objective getObjective() {
    final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
    if (objective == null) {
      objective = scoreboard.registerNewObjective("BlockBreakC", "dummy", text("BlockBreakCounter", YELLOW), RenderType.INTEGER);
      objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    return objective;
  }

  public int setScore(final String name, final int value) {
    this.getObjective().getScore(name).setScore(value);
    return value;
  }

  public int getScore(final String name) {
    return this.getObjective().getScore(name).getScore();
  }

  @Override
  public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, final @NotNull String[] args) {
    if (command.getName().equalsIgnoreCase("blockbreakcounter")) {
      if (args.length < 2) {
        sender.sendMessage(text("BlockBreakCounter v1.0 by MiniDigger. Written while drunk (like always)"));
        sender.sendMessage(text("Commands: /blockbreakcounter get|set|add|remove <player> [amount]"));
      } else {
        switch(args[0]) {
          case "get" -> sender.sendMessage(text("Player " + args[1] + " has a score of " + this.getScore(args[1])));
          case "set" -> {
            final int score = this.parse(args, sender);
            if (score != -1) {
              sender.sendMessage(text("Player " + args[1] + " now has a score of " + this.setScore(args[1], score)));
            }
          }
          case "add" -> {
            final int score = this.parse(args, sender);
            if (score != -1) {
              sender.sendMessage(text("Player " + args[1] + " now has a score of " + this.setScore(args[1], this.getScore(args[1]) + score)));
            }
          }
          case "remove" -> {
            final int score = this.parse(args, sender);
            if (score != -1) {
              sender.sendMessage(text("Player " + args[1] + " now has a score of " + this.setScore(args[1], this.getScore(args[1]) - score)));
            }
          }
          default -> sender.sendMessage(text("Usage: /blockbreakcounter get|set|add|remove <player> <amount>", RED));
        }
      }
      return true;
    }
    return false;
  }

  private int parse(final String[] args, final CommandSender sender) {
    if (args.length == 3) {
      try {
        return Integer.parseInt(args[2]);
      } catch(final NumberFormatException ignored) {

      }
    }
    sender.sendMessage(text("Usage: /blockbreakcounter get|set|add|remove <player> <amount>", RED));
    return -1;
  }
}
