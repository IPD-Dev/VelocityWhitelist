package com.github.ipddev.velocitywhitelist.data;

import com.github.ipddev.velocitywhitelist.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.cache.SQLiteCache;
import org.enginehub.squirrelid.resolver.CacheForwardingService;
import org.enginehub.squirrelid.resolver.HttpRepositoryService;
import org.slf4j.Logger;

@Data
public class Whitelist {
	private final CacheForwardingService resolver;
	private final SQLiteCache cache;
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.create();
	private static final Path WHITELIST_FILE = Path.of(".", "whitelist.json");
	private final Main plugin;
	private final ProxyServer proxy;
	private final Configuration configuration;
	private final Logger logger;

	private final List<WhitelistedPlayer> players;

	@SneakyThrows
	public Whitelist(Main plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.proxy = plugin.getProxy();
		this.configuration = plugin.getConfiguration();
		this.players = new ArrayList<>();

		final File cache = plugin.getDataDirectory().resolve("cache.sqlite").toFile();

		this.cache = new SQLiteCache(cache);
		this.resolver = new CacheForwardingService(HttpRepositoryService.forMinecraft(), this.cache);

		load();
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	public void load() {
		if(!Files.exists(WHITELIST_FILE)) {
			players.clear();

			this.save();
		}

		final String fileContents = Files.readString(WHITELIST_FILE);

		final List<LinkedTreeMap<String, String>> fromFile = GSON.fromJson(fileContents, List.class);

		for (LinkedTreeMap<String, String> map : fromFile) {
			final String username = map.get("username");
			final UUID uuid = UUID.fromString(map.get("uuid"));

			players.add(new WhitelistedPlayer(uuid, username));
		}
	}

	@SneakyThrows
	public void save() {
		final String jsonified = GSON.toJson(players);

		Files.writeString(WHITELIST_FILE, jsonified, StandardCharsets.UTF_8);
	}

	public void update() {
		save();

		if (!configuration.isWhitelistEnabled()) {
			return;
		}

		if(!configuration.isWhitelistEnforced()) {
			return;
		}

		final Component unWhitelistedMessage = configuration.resolveUnWhitelistedMessage();

		for (Player player : proxy.getAllPlayers()) {
			if(isPlayerPresent(player)) {
				continue;
			}

			logger.info("Dropped {} from proxy due to whitelist update!", player.getUsername());

			player.disconnect(unWhitelistedMessage);
		}
	}

	public boolean isPlayerPresent(Player player) {
		return players.stream()
			.anyMatch(p -> p.getUuid().equals(player.getUniqueId()));
	}

	public boolean isWhitelistedPlayerPresent(WhitelistedPlayer player) {
		return players.stream()
			.anyMatch(p -> p.getUuid().equals(player.getUuid()));
	}

	public void checkForUsernameUpdate(Player player) {
		final WhitelistedPlayer whitelistedPlayer = players.stream()
			.filter(p -> p.getUuid().equals(player.getUniqueId()))
			.findFirst()
			.orElseThrow();

		final String playerName = player.getUsername();
		final String whitelistedName = whitelistedPlayer.getUsername();

		if (whitelistedName.equals(playerName)) {
			return;
		}

		whitelistedPlayer.setUsername(playerName);

		update();
	}

	public List<WhitelistedPlayer> getPlayers() {
		return List.copyOf(this.players);
	}

	public boolean removePlayer(String username) {
		final List<WhitelistedPlayer> toRemove = new ArrayList<>();

		for (WhitelistedPlayer player : players) {
			if (player.getUsername().equalsIgnoreCase(username)) {
				toRemove.add(player);
				break;
			}
		}

		players.removeAll(toRemove);

		update();

		return toRemove.size() > 0;
	}

	public boolean removePlayer(UUID uuid) {
		final List<WhitelistedPlayer> toRemove = new ArrayList<>();

		for (WhitelistedPlayer player : players) {
			if (player.getUuid().equals(uuid)) {
				toRemove.add(player);
				break;
			}
		}

		update();

		return toRemove.size() > 0;
	}

	public void addPlayer(Player player) {
		if (isPlayerPresent(player)) {
			throw new RuntimeException("Player already resent in whitelist!");
		}

		players.add(new WhitelistedPlayer(player.getUniqueId(), player.getUsername()));

		update();
	}

	public void addPlayer(UUID uuid, String username) {
		final WhitelistedPlayer whitelistedPlayer = new WhitelistedPlayer(uuid, username);

		if (isWhitelistedPlayerPresent(whitelistedPlayer)) {
			throw new RuntimeException("Player already resent in whitelist!");
		}

		players.add(new WhitelistedPlayer(uuid, username));

		update();
	}

	@SneakyThrows
	public boolean addPlayer(String username) {
		final Optional<Player> playerOptional = proxy.getPlayer(username);

		if (playerOptional.isEmpty()) {
			final Profile profile = resolver.findByName(username);

			if (profile == null) {
				return false;
			}

			this.addPlayer(profile.getUniqueId(), profile.getName());

			return true;
		}

		final Player player = playerOptional.get();

		this.addPlayer(player);

		return true;
	}

	@SneakyThrows
	public boolean addPlayer(UUID uuid) {
		final Optional<Player> playerOptional = proxy.getPlayer(uuid);

		if (playerOptional.isEmpty()) {
			final Profile profile = resolver.findByUuid(uuid);

			if (profile == null) {
				return false;
			}

			this.addPlayer(uuid, profile.getName());

			return true;
		}

		final Player player = playerOptional.get();

		this.addPlayer(player);

		return true;
	}

	@Data
	@AllArgsConstructor
	public static class WhitelistedPlayer {
		private final UUID uuid;
		private String username;
	}
}
