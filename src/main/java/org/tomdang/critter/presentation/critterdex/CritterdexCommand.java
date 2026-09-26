package org.tomdang.critter.presentation.critterdex;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomdang.guiframework.GuiService;

public final class CritterdexCommand implements CommandExecutor {
    private final GuiService menus;
    public CritterdexCommand(GuiService menus) { this.menus = menus; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the Critterdex.");
            return true;
        }
        menus.open(player, CritterdexIndexScreen.ID);
        return true;
    }
}
