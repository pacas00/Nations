package com.arckenver.nations;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.arckenver.nations.task.RentCollectRunnable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;

import com.arckenver.nations.cmdexecutor.NationCmds;
import com.arckenver.nations.listener.BuildPermListener;
import com.arckenver.nations.listener.ChatListener;
import com.arckenver.nations.listener.ExplosionListener;
import com.arckenver.nations.listener.FireListener;
import com.arckenver.nations.listener.GoldenAxeListener;
import com.arckenver.nations.listener.InteractPermListener;
import com.arckenver.nations.listener.MobSpawningListener;
import com.arckenver.nations.listener.PlayerConnectionListener;
import com.arckenver.nations.listener.PlayerMoveListener;
import com.arckenver.nations.listener.PvpListener;
import com.arckenver.nations.service.NationsService;
import com.arckenver.nations.task.TaxesCollectRunnable;
import com.google.inject.Inject;

@Plugin(id = "nations-updated", name = "Nations", authors = {"Arckenver", "Carrot"}, url = "https://github.com/Arckenver/Nations", version = "2.11.1")
public class NationsPlugin {
	private File rootDir;

	private static NationsPlugin plugin;

	@Inject
	private Logger logger;

	@Inject
	@ConfigDir(sharedRoot = true)
	private File defaultConfigDir;

	private EconomyService economyService = null;

	@Listener
	public void onInit(GameInitializationEvent event) {
		plugin = this;

		rootDir = new File(defaultConfigDir, "nations");

		LanguageHandler.init(rootDir);
		ConfigHandler.init(rootDir);
		DataHandler.init(rootDir);

		Sponge.getServiceManager().setProvider(this, NationsService.class, new NationsService());
	}

	@Listener
	public void onStart(GameStartedServerEvent event) {
		LanguageHandler.load();
		ConfigHandler.load();
		DataHandler.load();

		Sponge.getServiceManager()
				.getRegistration(EconomyService.class)
				.ifPresent(prov -> economyService = prov.getProvider());


		NationCmds.create(this);

		Sponge.getEventManager().registerListeners(this, new PlayerConnectionListener());
		Sponge.getEventManager().registerListeners(this, new PlayerMoveListener());
		Sponge.getEventManager().registerListeners(this, new GoldenAxeListener());
		Sponge.getEventManager().registerListeners(this, new PvpListener());
		Sponge.getEventManager().registerListeners(this, new FireListener());
		Sponge.getEventManager().registerListeners(this, new ExplosionListener());
		Sponge.getEventManager().registerListeners(this, new MobSpawningListener());
		Sponge.getEventManager().registerListeners(this, new BuildPermListener());
		Sponge.getEventManager().registerListeners(this, new InteractPermListener());
		Sponge.getEventManager().registerListeners(this, new ChatListener());

		LocalDateTime localNow = LocalDateTime.now();
		ZonedDateTime zonedNow = ZonedDateTime.of(localNow, ZoneId.systemDefault());
		ZonedDateTime zonedNext = zonedNow.withHour(12).withMinute(0).withSecond(0);
		if (zonedNow.compareTo(zonedNext) > 0)
			zonedNext = zonedNext.plusDays(1);
		long initialDelay = Duration.between(zonedNow, zonedNext).getSeconds();

		ZonedDateTime zonedNextHour = zonedNow.plusHours(1).withMinute(0).withSecond(0);
		long rentDelay = Duration.between(zonedNow, zonedNextHour).getSeconds();

		Sponge.getScheduler()
				.createTaskBuilder()
				.execute(new TaxesCollectRunnable())
				.delay(initialDelay, TimeUnit.SECONDS)
				.interval(1, TimeUnit.DAYS)
				.async()
				.submit(this);

		Sponge.getScheduler()
				.createTaskBuilder()
				.execute(new RentCollectRunnable())
				.delay(rentDelay, TimeUnit.SECONDS)
				.interval(1, TimeUnit.HOURS)
				.async()
				.submit(this);

		logger.info("Plugin ready");
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent event) {
		logger.info("Saving data");
		ConfigHandler.save();
		DataHandler.save();
		logger.info("Plugin stopped");
	}

	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
			economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}

	public static NationsPlugin getInstance() {
		return plugin;
	}

	public static Logger getLogger() {
		return getInstance().logger;
	}

	public static EconomyService getEcoService() {
		return getInstance().economyService;
	}

	public static Cause getCause() {
		return Sponge.getCauseStackManager().getCurrentCause();

	}

	public File getDefaultConfigDir() {
		return defaultConfigDir;
	}

}
