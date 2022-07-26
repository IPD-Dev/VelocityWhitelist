package com.github.ipddev.velocitywhitelist;

import com.github.ipddev.velocitywhitelist.commands.VWhitelistCommand;
import com.github.ipddev.velocitywhitelist.data.Configuration;
import com.github.ipddev.velocitywhitelist.data.Whitelist;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
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
        configuration = new Configuration(this);
        whitelist = new Whitelist(this);

        final CommandManager commandManager = proxy.getCommandManager();

        commandManager.register(new BrigadierCommand(new VWhitelistCommand(this).createNode()));
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
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













