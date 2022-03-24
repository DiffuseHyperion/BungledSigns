package tk.yjservers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static tk.yjservers.BungledSigns.*;

public class linkSign implements CommandExecutor{

    BungledSigns bungledSigns;
    BungeeMessageAPI bcapi;

    public linkSign() {
        bungledSigns = new BungledSigns();
        bcapi = new BungeeMessageAPI(Bukkit.getPluginManager().getPlugin("BungledSigns"));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (args.length == 1) {
                Block b = p.getTargetBlock(null, 5);
                if (bungledSigns.isSign(b)) {
                    bcapi.GetServers().whenCompleteAsync((result, error) -> {
                        List<String> nameList = new ArrayList<>(Arrays.asList(result));
                        if (nameList.contains(args[0])) {
                            Vector v = b.getLocation().toVector();
                            String servername = args[0];
                            if (bungledSigns.isLinkedBlock(b)) {
                                p.sendMessage(ChatColor.YELLOW + "This sign is already linked to another server. Unlink it first by doing /unlinksign, before redoing this command.");
                            } else {
                                String uuid = UUID.randomUUID().toString();

                                dataFileConfig.createSection(uuid);
                                dataFileConfig.createSection(uuid + ".vector.x");
                                dataFileConfig.createSection(uuid + ".vector.y");
                                dataFileConfig.createSection(uuid + ".vector.z");
                                dataFileConfig.createSection(uuid + ".server");
                                dataFileConfig.createSection(uuid + ".world");

                                dataFileConfig.set(uuid + ".vector.x", v.getBlockX());
                                dataFileConfig.set(uuid + ".vector.y", v.getBlockY());
                                dataFileConfig.set(uuid + ".vector.z", v.getBlockZ());
                                dataFileConfig.set(uuid + ".server", servername);
                                dataFileConfig.set(uuid + ".world", b.getWorld().getName());

                                bungledSigns.createSignUpdater(b);
                                signslist.put(b, new Pair<>(uuid, true));
                                try {
                                    dataFileConfig.save(dataFile);
                                    p.sendMessage(ChatColor.GREEN + "Sign at " + v.getBlockX() + ", "+ v.getBlockY() + ", "+ v.getBlockZ() + " has been linked to " + servername + "!");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    p.sendMessage(ChatColor.RED + "Something went wrong while saving the file! The stack trace has been printed to the console.");
                                }
                            }
                        } else {
                            p.sendMessage(ChatColor.YELLOW + "There are no bungeecord servers called '" + args[0] + "', are you sure it's in your bungeecord server's config.yml?");
                        }
                    });
                    return true;
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You need to be looking at a nearby sign to link it!");
                }
            } else {
                p.sendMessage(ChatColor.YELLOW + "Input the bungeecord server name after the command!");
            }
        } else {
            commandSender.sendMessage(ChatColor.YELLOW + "This command can only be executed by a player!");
        }
        return true;
    }
}
