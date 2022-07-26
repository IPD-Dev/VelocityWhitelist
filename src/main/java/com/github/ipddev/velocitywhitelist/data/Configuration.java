package com.github.ipddev.velocitywhitelist.data;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.github.ipddev.velocitywhitelist.Main;
import com.google.common.io.Resources;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Data
public class Configuration {
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private final ClassLoader classLoader = this.getClass().getClassLoader();
	private final URL defaultConfigurationUrl = classLoader.getResource("config.toml");

	private final Main plugin;
	private final Path configurationPath;

	// Actual configurable bits
	private String unWhitelistedMessage;

	private boolean whitelistEnabled;
	private boolean whitelistEnforced;
	// ...

	private final FileConfig fileConfig;

	@SneakyThrows
	@SuppressWarnings("UnstableApiUsage")
	public Configuration(Main plugin) {
		assert defaultConfigurationUrl != null;

		this.plugin = plugin;

		final Path dataDirectory = this.plugin.getDataDirectory();

		if(!Files.exists(dataDirectory)) {
			Files.createDirectory(dataDirectory);
		}

		this.configurationPath = dataDirectory.resolve("config.toml");

		if (!Files.exists(configurationPath)) {
			Files.writeString(configurationPath, Resources.toString(defaultConfigurationUrl, StandardCharsets.UTF_8));
		}

		this.fileConfig = FileConfig.of(configurationPath);
		fileConfig.load();

		unWhitelistedMessage = fileConfig.get("messages.unWhitelistedMessage");

		whitelistEnabled = fileConfig.get("whitelist.enabled");
		whitelistEnforced = fileConfig.get("whitelist.enforced");
	}

	public void save() {
		fileConfig.set("messages.unWhitelistedMessage", unWhitelistedMessage);
		fileConfig.set("whitelist.enabled", whitelistEnabled);
		fileConfig.set("whitelist.enforced", whitelistEnforced);

		fileConfig.save();
	}

	public Component resolveUnWhitelistedMessage() {
		return MINI_MESSAGE.deserialize(unWhitelistedMessage);
	}
}
