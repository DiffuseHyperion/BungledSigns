package tk.yjservers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BungledSigns extends JavaPlugin implements Listener {

    protected static File dataFile;
    protected static FileConfiguration dataFileConfig;
    protected static Map<Block, String> signslist = new HashMap<>();

    @Override
    public void onEnable(){
        initData();
        this.getCommand("linksign").setExecutor(new linkSign());
        this.getCommand("unlinksign").setExecutor(new unlinkSign());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new signInteraction(), this);
    }

    @Override
    public void onDisable(){
        try {
            dataFileConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    private void initData() {
        //create dataFile
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveResource("data.yml", true);
        }

        //load dataFile
        dataFileConfig = new YamlConfiguration();
        try {
            dataFileConfig.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        //populate signslist
        if (dataFileConfig.getKeys(false).isEmpty()) {
            Bukkit.getLogger().warning("No signs found in data.yml, this may be an error!");
        } else {
            for (String s: dataFileConfig.getKeys(false)) {
                if (getServer().getWorld(dataFileConfig.getString(s + ".world")) == null) {
                    getLogger().severe("Something went wrong while reading data.yml, was it changed manually?");
                    getLogger().severe("Check if there is a section called : " + s + ", by using CTRL+F with your editor.");
                    getLogger().severe("This plugin will now disable itself to prevent further errors.");
                    getPluginLoader().disablePlugin(getServer().getPluginManager().getPlugin("BungledSigns"));
                    return;
                }
                int x = dataFileConfig.getInt(s + ".vector.x");
                int y = dataFileConfig.getInt(s + ".vector.y");
                int z = dataFileConfig.getInt(s + ".vector.z");
                Block b = getServer().getWorld(dataFileConfig.getString(s + ".world")).getBlockAt(x, y, z);

                if (!isSign(b)) {
                    getLogger().severe("The coordinates in " + s + " leads to a non-sign block! Was data.yml changed manually?");
                    getLogger().severe("The block is in the world: " + b.getWorld().getName());
                    getLogger().severe("Its x, y, z is: " + x + ", " + y + ", " + z);
                    getLogger().severe("It currently leads to: " + b.getType().toString());
                    getLogger().severe("This sign will appear as unlinked to commands, until this is resolved.");
                    return;
                }
                signslist.put(b, s);
            }
        }
    }


    protected boolean isSign(Block b) {
        return b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN;
    }

    protected boolean isLinkedBlock(Block b) {
        return signslist.containsKey(b);
    }

    protected boolean isLinkedSign(Block b) {
        return isLinkedBlock(b) && isSign(b);
    }
}
