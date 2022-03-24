package tk.yjservers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static tk.yjservers.BungledSigns.dataFileConfig;
import static tk.yjservers.BungledSigns.signslist;


public class signInteraction implements Listener {

    BungledSigns bungledSigns;
    BungeeMessageAPI bcapi;

    public signInteraction() {
        bungledSigns = new BungledSigns();
        bcapi = new BungeeMessageAPI(Bukkit.getPluginManager().getPlugin("BungledSigns"));
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        Block clickedblock = e.getClickedBlock();
        Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_BLOCK && bungledSigns.isLinkedSign(clickedblock)) {
            for (Block b : signslist.keySet()) {
                if (b.equals(clickedblock)) {
                    String server = dataFileConfig.getString(signslist.get(b).getValue0() + ".server");
                    Bukkit.getLogger().info("Server: " + server);
                    e.getPlayer().sendMessage(ChatColor.GREEN+ "Sending you to " + server + "!");

                    bcapi.Connect(e.getPlayer(), server);
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
