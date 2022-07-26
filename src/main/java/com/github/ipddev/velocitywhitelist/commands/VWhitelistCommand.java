package com.github.ipddev.velocitywhitelist.commands;

import com.github.ipddev.velocitywhitelist.Main;
import com.github.ipddev.velocitywhitelist.data.Configuration;
import com.github.ipddev.velocitywhitelist.data.Whitelist;
import com.github.ipddev.velocitywhitelist.data.Whitelist.WhitelistedPlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class VWhitelistCommand {
    private final Configuration configuration;
    private final Whitelist whitelist;

    public VWhitelistCommand(Main plugin) {
        this.configuration = plugin.getConfiguration();
        this.whitelist = plugin.getWhitelist();
    }

    public LiteralCommandNode<CommandSource> createNode() {
        return LiteralArgumentBuilder.<CommandSource>literal("vwhitelist")
            .requires(s -> s.hasPermission("whitelist.command"))
            .then(LiteralArgumentBuilder.<CommandSource>literal("on")
                .requires(s -> s.hasPermission("whitelist.command.enable"))
                .executes(this::on)
                .build())
            .then(LiteralArgumentBuilder.<CommandSource>literal("off")
                .requires(s -> s.hasPermission("whitelist.command.enable"))
                .executes(this::off)
                .build())
            .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                .requires(s -> s.hasPermission("whitelist.command.add"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.string())
                    .executes(this::add))
                .build())
            .then(LiteralArgumentBuilder.<CommandSource>literal("remove")
                .requires(s -> s.hasPermission("whitelist.command.remove"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.string())
                    .executes(this::remove))
                .executes(this::remove)
                .build())
            .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                .requires(s -> s.hasPermission("whitelist.command.list"))
                .executes(this::list)
                .build()
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                .requires(s -> s.hasPermission("whitelist.command.reload"))
                .executes(this::reload)
                .build())
            .build();
    }

    private int on(CommandContext<CommandSource> context) {
        if (configuration.isWhitelistEnabled()) {
            context.getSource().sendMessage(Component.translatable("commands.whitelist.alreadyOn", NamedTextColor.RED));
            return 0;
        }

        configuration.setWhitelistEnabled(true);
        configuration.save();
        whitelist.update();
        context.getSource().sendMessage(Component.translatable("commands.whitelist.enabled"));

        return 1;
    }

    private int off(CommandContext<CommandSource> context) {
        if (!configuration.isWhitelistEnabled()) {
            context.getSource().sendMessage(Component.translatable("commands.whitelist.alreadyOff", NamedTextColor.RED));
            return 0;
        }

        configuration.setWhitelistEnabled(false);
        configuration.save();
        whitelist.update();
        context.getSource().sendMessage(Component.translatable("commands.whitelist.disabled"));

        return 1;
    }

    private int add(CommandContext<CommandSource> context) {
        final String identifier = StringArgumentType.getString(context, "player");

        try {
            final boolean val;
            if (identifier.contains("-")) {
                val = whitelist.addPlayer(UUID.fromString(identifier));

            } else {
                val = whitelist.addPlayer(identifier);

            }

            if(!val) {
                throw new RuntimeException();
            }

            context.getSource().sendMessage(Component.translatable("commands.whitelist.add.success").args(Component.text(identifier)));
        } catch (Exception e) {
            context.getSource().sendMessage(Component.translatable("commands.whitelist.add.failed", NamedTextColor.RED));
            return 0;
        }

        return 1;
    }

    private int remove(CommandContext<CommandSource> context) {
        final String identifier = StringArgumentType.getString(context, "player");

        try {
            final boolean val;

            if (identifier.contains("-")) {
                val = whitelist.removePlayer(UUID.fromString(identifier));
            } else {
                val = whitelist.removePlayer(identifier);
            }

            if(!val) {
                throw new RuntimeException();
            }

            context.getSource().sendMessage(Component.translatable("commands.whitelist.remove.success").args(Component.text(identifier)));
        } catch (Exception e) {
            context.getSource().sendMessage(Component.translatable("commands.whitelist.remove.failed", NamedTextColor.RED));
            return 0;
        }

        return 1;
    }

    private int list(CommandContext<CommandSource> context) {
        final List<WhitelistedPlayer> players = whitelist.getPlayers();
        final String playerList = String.join(", ", players.stream()
            .map(WhitelistedPlayer::getUsername)
            .toList());

        context.getSource().sendMessage(Component.translatable("commands.whitelist.list")
            .args(Component.text(players.size()), Component.text(playerList)));

        return 1;
    }

    private int reload(CommandContext<CommandSource> context) {
        whitelist.load();

        context.getSource().sendMessage(Component.translatable("commands.whitelist.reloaded"));

        return 1;
    }
}
