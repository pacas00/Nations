package com.arckenver.nations.task;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
import com.arckenver.nations.Utils;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.Zone;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public class RentCollectRunnable implements Runnable {

	@Override
	public void run() {

		if (NationsPlugin.getEcoService() == null)
		{
			NationsPlugin.getLogger().error(LanguageHandler.ERROR_NOECO);
			return;
		}

		EventContext context = EventContext.builder()
				.build();

		Cause cause = Cause.builder()
				.append(Sponge.getServer().getConsole())
				.append(NationsPlugin.getInstance())
				.build(context);

		Text rentTimeMessage = Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTTIME);

			MessageChannel.TO_CONSOLE.send(Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTTIME));
		if (NationsPlugin.getEcoService() == null) {
			MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
			return;
		}

		LocalDateTime nowHour = LocalDateTime.of(LocalDate.now(),LocalTime.of(LocalTime.now().getHour(), 0));

		for (Nation nation : DataHandler.getNations().values()) {

			LocalDateTime targetHour = nation.getLastRentCollectTime().plusHours(nation.getRentInterval());
			if(nowHour.isAfter(targetHour) || nowHour.isEqual(targetHour)) {

				if (nation.getPresident() != null) {
					Sponge.getServer().getPlayer(nation.getPresident()).ifPresent(p -> {
						p.sendMessage(rentTimeMessage);
					});
				}

				Optional<Account> nationAccount = NationsPlugin.getEcoService().getOrCreateAccount("nation-" + nation.getUUID());
				if (!nationAccount.isPresent()) {
					MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONONATION));
					continue;
				}

				for (Zone zone : nation.getZones().values()) {
					if (!zone.isForRent()) continue;
					if (!zone.isOwned()) continue;
					Optional<Player> owner = Sponge.getServer().getPlayer(zone.getOwner());
					if (!owner.isPresent())
						continue; //probably something went wrong
					owner.get().sendMessage(rentTimeMessage);
					BigDecimal rentPrice = zone.getRentalPrice();

					BigDecimal zoneBalance = BigDecimal.ZERO;
					Optional<Account> zoneAccount = NationsPlugin.getEcoService().getOrCreateAccount("zone-" + zone.getUUID());
					if (zoneAccount.isPresent()) {
						zoneBalance = zoneAccount.get().getBalance(NationsPlugin.getEcoService().getDefaultCurrency());
					} else {
						MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOZONE));
						continue;
					}

					Optional<UniqueAccount> ownerAccount = NationsPlugin.getEcoService().getOrCreateAccount(zone.getOwner());
					BigDecimal ownerBalance = BigDecimal.ZERO;
					if (ownerAccount.isPresent()) {
						ownerBalance = ownerAccount.get().getBalance(NationsPlugin.getEcoService().getDefaultCurrency());
					} else {
						MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
						continue;
					}
					if (zoneBalance.compareTo(rentPrice) >= 0) { //y didn't java devs just make actual operators for big decimals
						TransactionResult result = zoneAccount.get().transfer(nationAccount.get(), NationsPlugin.getEcoService().getDefaultCurrency(), rentPrice, cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						} else {
							String str = LanguageHandler.INFO_PAYRENTZONEBALANCE.replaceAll("\\{ZONE\\}", zone.getDisplayName());
							String split[] = str.split("\\{VALUE\\}");
							owner.get().sendMessage(Text.of(TextColors.AQUA, split[0], Utils.formatPrice(TextColors.YELLOW, rentPrice),split[1]));
						}
					} else if (zoneBalance.add(ownerBalance).compareTo(rentPrice) >= 0) {
						TransactionResult result = zoneAccount.get().transfer(nationAccount.get(), NationsPlugin.getEcoService().getDefaultCurrency(), zoneBalance, cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						} else if(zoneBalance.compareTo(BigDecimal.ZERO) > 0){ //only shows if it really paid with it, so it won't flood chat
							String str = LanguageHandler.INFO_PAYRENTZONEBALANCE.replaceAll("\\{ZONE\\}", zone.getDisplayName());
							String split[] = str.split("\\{VALUE\\}");
							owner.get().sendMessage(Text.of(TextColors.AQUA, split[0], Utils.formatPrice(TextColors.YELLOW, zoneBalance),split[1]));
						}
						result = ownerAccount.get().transfer(nationAccount.get(), NationsPlugin.getEcoService().getDefaultCurrency(), rentPrice.subtract(zoneBalance), cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						} else {
							String str = LanguageHandler.INFO_PAYRENTZONEPLAYER.replaceAll("\\{ZONE\\}", zone.getDisplayName());
							String split[] = str.split("\\{VALUE\\}");
							owner.get().sendMessage(Text.of(TextColors.AQUA, split[0], Utils.formatPrice(TextColors.YELLOW, rentPrice.subtract(zoneBalance)),split[1]));
						}
					} else { //return zone: make zone bal go to owner and make ownerless
						TransactionResult result = zoneAccount.get().transfer(ownerAccount.get(), NationsPlugin.getEcoService().getDefaultCurrency(), zoneBalance, cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						}
						String oldName = zone.getDisplayName();
						zone.resetCoowners();
						zone.setOwner(null);
						zone.setDisplayName(null);
						owner.get().sendMessage(Text.of(TextColors.RED, LanguageHandler.INFO_FAILEDRENT.replaceAll("\\{ZONE\\}}", oldName)));
						for (UUID player : nation.getCitizens()) {
							if (player != owner.get().getUniqueId()) {
								Sponge.getServer().getPlayer(player).ifPresent(p -> {
									p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_RETURNRENT
											.replaceAll("\\{PLAYER\\}", owner.get().getName())
											.replaceAll("\\{ZONE\\}", oldName)));
								});
							}
						}
					}
				}
				nation.setLastRentCollectTime(nowHour);
			}
		}
		//saves everything after operation
		DataHandler.save();
	}
}
