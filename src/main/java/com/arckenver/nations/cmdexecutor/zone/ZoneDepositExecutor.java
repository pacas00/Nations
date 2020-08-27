package com.arckenver.nations.cmdexecutor.zone;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
import com.arckenver.nations.Utils;
import com.arckenver.nations.cmdexecutor.nationadmin.NationadminCollectRentExecutor;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.Zone;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ZoneDepositExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.zone.deposit")
				.arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
				.executor(new ZoneDepositExecutor())
				.build(), "deposit");
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {
			if (!ctx.<Double>getOne("amount").isPresent()) {
				src.sendMessage(Text.of(TextColors.YELLOW, "/z deposit <amount>"));
				return CommandResult.success();
			}
			Player player = (Player) src;
			BigDecimal amount = BigDecimal.valueOf(ctx.<Double>getOne("amount").get());
			if (amount.compareTo(BigDecimal.ZERO) <= 0) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_P));
				return CommandResult.success();
			}
			Nation nation = DataHandler.getNation(player.getLocation());
			if (nation == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDNATION));
				return CommandResult.success();
			}
			Zone currentZone = nation.getZone(player.getLocation());
			if (currentZone == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDZONESELF));
				return CommandResult.success();
			}//if zone owner, co owner or rather nation staff
			if (!currentZone.isCoowner(player.getUniqueId()) && !currentZone.isOwner(player.getUniqueId())) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if (!currentZone.isForRent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTRENTING));
				return CommandResult.success();
			}

			if (NationsPlugin.getEcoService() == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
			Optional<UniqueAccount> ownerAccount = NationsPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());
			if (!ownerAccount.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
			Optional<Account> zoneAccount = NationsPlugin.getEcoService().getOrCreateAccount("zone-" + currentZone.getUUID());
			if (!zoneAccount.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOZONE));
				return CommandResult.success();
			}
			TransactionResult result = ownerAccount.get().transfer(zoneAccount.get(), NationsPlugin.getEcoService().getDefaultCurrency(), amount, NationsPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOENOUGHMONEY));
				return CommandResult.success();
			} else if (result.getResult() != ResultType.SUCCESS) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			} else {
				String[] s1 = LanguageHandler.SUCCESS_WITHDRAW_ZONE.split("\\{AMOUNT\\}");
				Text.Builder builder = Text.builder();
				if (s1[0].contains("{BALANCE}")) {
					String[] split = s1[0].split("\\{BALANCE\\}");
					builder.append(Text.of(TextColors.GREEN, (split.length > 0) ? split[0] : ""))
							.append(Utils.formatPrice(TextColors.YELLOW, zoneAccount.get().getBalance(NationsPlugin.getEcoService().getDefaultCurrency())))
							.append(Text.of(TextColors.GREEN, (split.length > 1) ? split[1] : ""));
				} else {
					builder.append(Text.of(TextColors.GREEN, s1[0]));
				}
				builder.append(Utils.formatPrice(TextColors.YELLOW, amount));
				if (s1[1].contains("{BALANCE}")) {
					String[] split = s1[1].split("\\{BALANCE\\}");
					builder.append(Text.of(TextColors.GREEN, (split.length > 0) ? split[0] : ""))
							.append(Utils.formatPrice(TextColors.YELLOW, zoneAccount.get().getBalance(NationsPlugin.getEcoService().getDefaultCurrency())))
							.append(Text.of(TextColors.GREEN, (split.length > 1) ? split[1] : ""));
				} else {
					builder.append(Text.of(TextColors.GREEN, s1[1]));
				}
				src.sendMessage(builder.build());
				return CommandResult.success();
			}
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
