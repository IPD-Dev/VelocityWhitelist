package com.github.ipddev.velocitywhitelist.ashcon;

import java.util.UUID;
import lombok.Data;

@Data
public class IncompleteAshconPlayer {
	private final UUID uuid;
	private final String username;
}
