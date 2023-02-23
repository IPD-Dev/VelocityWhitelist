package com.github.ipddev.velocitywhitelist;

import com.github.ipddev.velocitywhitelist.commands.VWhitelistCommand;
import com.github.ipddev.velocitywhitelist.data.Configuration;
import com.github.ipddev.velocitywhitelist.data.Whitelist;
import com.google.inject.Inject;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

@Plugin(
        id = "whitelist",
        name = "Whitelist",
        version = "1.0-SNAPSHOT",
        description = "Whitelist for your Velocity proxy",
        authors = {"Allink"}
)
public class Main {
    private boolean enabled = false;
    @Inject
    @Getter
    public Logger logger;

    @Inject
    @Getter
    private ProxyServer proxy;

    @Inject
    @DataDirectory
    @Getter
    private Path dataDirectory;

    @Getter
    private Whitelist whitelist;
    @Getter
    private Configuration configuration;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!proxy.getConfiguration().isOnlineMode()) {
            logger.error("Whitelists are useless in offline mode!");

            return;
        }

        enabled = true;
        configuration = new Configuration(this);
        whitelist = new Whitelist(this);

        final CommandManager commandManager = proxy.getCommandManager();
        final CommandMeta commandMeta = commandManager.metaBuilder("vwhitelist")
                .aliases("vw", "velocitywhitelist")
                .plugin(this)
                .build();

        final VWhitelistCommand whitelistComamnd = new VWhitelistCommand(this);
        final LiteralCommandNode<CommandSource> node = whitelistComamnd.createNode();
        final BrigadierCommand brigadierCommand = new BrigadierCommand(node);

        commandManager.register(commandMeta, brigadierCommand);
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (!enabled) {
            return;
        }

        if (!configuration.isWhitelistEnabled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (whitelist.isPlayerPresent(player)) {
            return;
        }

        for (Player onlinePlayer : proxy.getAllPlayers()) {
            if (!onlinePlayer.hasPermission("whitelist.notify")) {
                continue;
            }

            onlinePlayer.sendMessage(Component.text("[Velocity Whitelist] ", NamedTextColor.AQUA)
                .append(Component.text(player.getUsername()))
                .append(Component.text(" tried to join but was not whitelisted!"))
            );
        }

        event.setResult(ComponentResult.denied(configuration.resolveUnWhitelistedMessage()));
    }
}