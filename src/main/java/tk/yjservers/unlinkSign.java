package tk.yjservers;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

import static tk.yjservers.BungledSigns.*;

public class unlinkSign implements CommandExecutor {

    BungledSigns bungledSigns;

    public unlinkSign() {
        bungledSigns = new BungledSigns();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            Block b = p.getTargetBlock(null, 5);
            if (bungledSigns.isLinkedSign(b)) {
                dataFileConfig.set(signslist.get(b).getValue0(), null);
                    signslist.remove(b);
                    try {
                        dataFileConfig.save(dataFile);
                        p.sendMessage(ChatColor.GREEN + "The sign has been unlinked!");
                    } catch (IOException e) {
                        p.sendMessage(ChatColor.RED + "Something went wrong while saving the file! The stack trace has been printed to the console.");
                        e.printStackTrace();
                    }
            } else {
                p.sendMessage(ChatColor.RED + "The block you are looking at is not a linked sign!");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
        }
        return true;
    }
}
