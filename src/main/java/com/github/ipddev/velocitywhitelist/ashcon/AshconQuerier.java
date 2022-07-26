package com.github.ipddev.velocitywhitelist.ashcon;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;

public class AshconQuerier {
	private static final Gson GSON = new Gson();
	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	public static CompletableFuture<IncompleteAshconPlayer> query(String identifier) {
		final CompletableFuture<IncompleteAshconPlayer> future = new CompletableFuture<>();

		executorService.submit(new QueryThread(future, identifier));

		return future;
	}

	@AllArgsConstructor
	private static class QueryThread extends Thread {
		private static final String QUERY_URL = "https://api.ashcon.app/mojang/v2/user/%s";
		private final CompletableFuture<IncompleteAshconPlayer> future;
		private final String identifier;
		private final HttpClient httpClient = HttpClient.newHttpClient();

		@Override
		public void run() {
			super.run();

			final URI uri = URI.create(QUERY_URL.formatted(identifier));

			final HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(uri)
				.build();

			final HttpResponse<String> httpResponse;

			try {
				 httpResponse = httpClient.send(request, BodyHandlers.ofString());
			} catch (Exception e) {
				future.completeExceptionally(e);
				return;
			}

			if (httpResponse.statusCode() != 200) {
				future.completeExceptionally(new RuntimeException("Invalid status code"));
				return;
			}

			final IncompleteAshconPlayer incompleteAshconPlayer = GSON.fromJson(httpResponse.body(), IncompleteAshconPlayer.class);
			future.complete(incompleteAshconPlayer);
		}
	}
}
