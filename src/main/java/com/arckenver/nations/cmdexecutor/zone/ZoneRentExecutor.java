package com.arckenver.nations.cmdexecutor.zone;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
import com.arckenver.nations.Utils;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.Zone;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class ZoneRentExecutor implements CommandExecutor {
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.zone.rent")
				.arguments()
				.executor(new ZoneRentExecutor())
				.build(), "rent", "lease");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {
			Player player = (Player) src;
			Nation nation = DataHandler.getNation(player.getLocation());
			if (nation == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDNATION));
				return CommandResult.success();
			}
			Zone zone = nation.getZone(player.getLocation());
			if (zone == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDZONE));
				return CommandResult.success();
			}
			Nation playerNation = DataHandler.getNationOfPlayer(player.getUniqueId());
			if (!nation.isAdmin() && !zone.getFlag("public") && (playerNation == null || !nation.getUUID().equals(playerNation.getUUID()))) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_ZONERENT));
				return CommandResult.success();
			}
			if (!zone.isForRent() || zone.isOwned()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ZONENFR));
				return CommandResult.success();
			}
			if (NationsPlugin.getEcoService() == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
			Optional<UniqueAccount> optAccount = NationsPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());
			if (!optAccount.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
			Account receiver;
			Optional<Account> optReceiver = NationsPlugin.getEcoService().getOrCreateAccount("nation-" + nation.getUUID());
			if (!optReceiver.isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONONATION));
				return CommandResult.success();
			}
			receiver = optReceiver.get();
			BigDecimal price = zone.getRentalPrice();
			TransactionResult result = optAccount.get().transfer(receiver, NationsPlugin.getEcoService().getDefaultCurrency(), price, NationsPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.YELLOW, price))
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			} else if (result.getResult() != ResultType.SUCCESS) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			zone.setOwner(player.getUniqueId());
			zone.setRentalPrice(zone.getRentalPrice());
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_SETOWNER.replaceAll("\\{ZONE\\}", zone.getName())));
			String str = LanguageHandler.INFO_ZONERENT.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{ZONE\\}", zone.getDisplayName());
			nation.getChannel().send(Text.of(TextColors.AQUA, str));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
