package tk.yjservers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import static tk.yjservers.BungledSigns.*;


public class signInteraction implements Listener {

    BungledSigns bungledSigns;

    public signInteraction() {
        bungledSigns = new BungledSigns();
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        Block clickedblock = e.getClickedBlock();
        Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_BLOCK && bungledSigns.isLinkedSign(clickedblock)) {
            for (Block b : signslist.keySet()) {
                if (b.equals(clickedblock)) {
                    Player p = e.getPlayer();
                    String server = dataFileConfig.getString(signslist.get(b) + ".server");
                    Bukkit.getLogger().info(p.getDisplayName() + " clicked on a linked sign at " + clickedblock.getLocation().toVector().toString() + ", and is being sent to " + server + ".");
                    p.sendMessage(ChatColor.GREEN+ "Sending you to " + server + "!");

                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                    Plugin plugin = Bukkit.getPluginManager().getPlugin("BungledSigns");
                    p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                }
            }
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (bungledSigns.isLinkedSign(b)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.isOp()) {
                p.sendMessage(ChatColor.GRAY + "This sign is a linked sign! To break this, do /unlinksign first.");
            } else {
                p.sendMessage(ChatColor.GRAY + "You lack the permissions to break this sign.");
            }
        }
    }
}
