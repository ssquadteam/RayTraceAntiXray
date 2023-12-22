package com.vanillage.raytraceantixray.commands;

import com.google.common.base.Stopwatch;
import com.vanillage.raytraceantixray.RayTraceAntiXray;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class RayTraceAntiXrayTabExecutor implements TabExecutor {
    private final RayTraceAntiXray plugin;

    public RayTraceAntiXrayTabExecutor(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        LinkedList<String> completions = new LinkedList<>();

        if (args.length == 0) {
            if ("raytraceantixray".startsWith(label.toLowerCase(Locale.ROOT))) {
                completions.add("raytraceantixray");
            }
        } else if (command.getName().toLowerCase(Locale.ROOT).equals("raytraceantixray")) {
            if (args.length == 1) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings") && "timings".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add("timings");
                }
                if (sender.hasPermission("raytraceantixray.command.reload") && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add("reload");
                }
                if (sender.hasPermission("raytraceantixray.command.reloadchunks") && "reloadchunks".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add("reloadchunks");
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("timings")) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")) {
                    if (args.length == 2) {
                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.on") && "on".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                            completions.add("on");
                        }

                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.off") && "off".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                            completions.add("off");
                        }
                    }
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("reloadchunks")) {
                if (sender.hasPermission("raytraceantixray.command.reloadchunks")) {
                    if (args.length > 1) {
                        if (!args[1].equalsIgnoreCase("*")) {
                            completions.addAll(Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .filter(n -> n.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                    .toList());
                        }
                        if (args.length == 2 && "*".startsWith(args[1])) {
                            completions.add("*");
                        }
                    }
                }
            }
        }

        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().toLowerCase(Locale.ROOT).equals("raytraceantixray")) {
            if (args.length == 0) {

            } else if (args[0].toLowerCase(Locale.ROOT).equals("timings")) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")) {
                    if (args.length == 1) {

                    } else if (args[1].toLowerCase(Locale.ROOT).equals("on")) {
                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.on")) {
                            if (args.length == 2) {
                                plugin.setTimingsEnabled(true);
                                sender.sendMessage("Timings turned on.");
                                return true;
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions.");
                            return true;
                        }
                    } else if (args[1].toLowerCase(Locale.ROOT).equals("off")) {
                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.off")) {
                            if (args.length == 2) {
                                plugin.setTimingsEnabled(false);
                                sender.sendMessage("Timings turned off.");
                                return true;
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions.");
                            return true;
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions.");
                    return true;
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("reload")) {
                if (sender.hasPermission("raytraceantixray.raytraceantixray.command.reload")) {
                    Stopwatch w = Stopwatch.createStarted();
                    plugin.reload();
                    sender.sendMessage("Reloaded in " + w.elapsed(TimeUnit.MILLISECONDS) + "ms");
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions.");
                }
                return true;
            } else if (args[0].toLowerCase(Locale.ROOT).equals("reloadchunks")) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.reloadchunks")) {
                    HashSet<Player> players = new HashSet<>();
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("*")) {
                            players.addAll(Bukkit.getOnlinePlayers());
                        } else {
                            for (int i = 1; i < args.length; i++) {
                                Player target = Bukkit.getPlayerExact(args[i]);
                                if (target == null) {
                                    sender.sendMessage(ChatColor.RED + "No player by the name of \"" + args[i] + "\" was found.");
                                    return true;
                                } else {
                                    players.add(target);
                                }
                            }
                        }
                    } else {
                        if (sender instanceof Player) {
                            players.add((Player) sender);
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must specify a player.");
                            return false;
                        }
                    }
                    sender.sendMessage("Reloading chunks...");
                    plugin.reloadChunks(players);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions.");
                }
            }
        }

        return false;
    }
}
