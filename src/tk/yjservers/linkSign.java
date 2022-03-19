package tk.yjservers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.bukkit.Bukkit.getLogger;
import static tk.yjservers.BungledSigns.*;

public class linkSign implements CommandExecutor, PluginMessageListener {

    public static String[] serverList;
    private static CountDownLatch latch;

    BungledSigns bungledSigns;

    public linkSign() {
        bungledSigns = new BungledSigns();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (args.length == 1) {
                Block b = p.getTargetBlock(null, 5);
                if (bungledSigns.isSign(b)) {

                    // send plugin msg to bungeecord on server list
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("GetServers");
                    Plugin plugin = Bukkit.getPluginManager().getPlugin("BungledSigns");
                    p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                    latch = new CountDownLatch(1);
                    getLogger().info("Request for bungeecord's server list has been sent!");

                    BukkitRunnable task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            String[] oldservlist = serverList;
                            while (serverList == oldservlist) { // protect from spurious wakeup
                                try {
                                    latch.await();
                                } catch (InterruptedException e) {
                                    p.sendMessage(ChatColor.RED + "Hmmm, something happened while the plugin was waiting for a response from bungeecord. The stack trace has been printed to the console.");
                                    e.printStackTrace();
                                }
                            }
                            List<String> nameList = new ArrayList<>(Arrays.asList(serverList));
                            getLogger().info("Part 2 of plugin message received! There are " + nameList.size() + " entries, they are:");
                            for (String s : nameList) {
                                getLogger().info(s);
                            }
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

                                    signslist.put(b, uuid);
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
                        }
                    };
                    task.runTaskAsynchronously(plugin);
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

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        getLogger().info("Part 1 of plugin message received!");
        if (!channel.equals("BungeeCord")) {
            getLogger().info("Wrong channel!");
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServers")) {
            serverList = in.readUTF().split(", ");
            latch.countDown();
        } else {
            getLogger().info("Wrong subchannel!");
        }
    }
}
