package com.arckenver.nations.cmdexecutor.zone;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class ZoneReturnExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.zone.rent") //if can rent, also can return or else would be extortion
				.arguments()
				.executor(new ZoneRentExecutor())
				.build(), "return", "release"); //release pun with lease and also has a meaning like this
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			Player player = (Player) src;
			Nation nation = DataHandler.getNation(player.getLocation());
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDNATION));
				return CommandResult.success();
			}
			Zone zone = nation.getZone(player.getLocation());
			if (zone == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDZONESELF));
				return CommandResult.success();
			}
			if (!zone.isOwner(player.getUniqueId()) && !nation.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if(!zone.isForRent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ISBOUGHT));
				return CommandResult.success();
			}
			//give back money to owner
			if (NationsPlugin.getEcoService() == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
			Optional<Account> optAccount = NationsPlugin.getEcoService().getOrCreateAccount("zone-" + zone.getUUID().toString());
			if (!optAccount.isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOZONE));
				return CommandResult.success();
			}
			Optional<UniqueAccount> receiver = NationsPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());
			if (!receiver.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
			BigDecimal balance = optAccount.get().getBalance(NationsPlugin.getEcoService().getDefaultCurrency());
			TransactionResult result = optAccount.get().transfer(receiver.get(), NationsPlugin.getEcoService().getDefaultCurrency(), balance, NationsPlugin.getCause());
			if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			String oldName = zone.getDisplayName();
			//then make ownerless
			zone.resetCoowners();
			zone.setOwner(null);
			zone.setDisplayName(null);
			DataHandler.saveNation(nation.getUUID());
			String str = LanguageHandler.INFO_RETURNRENT.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{ZONE\\}", oldName);
			nation.getChannel().send(Text.of(TextColors.AQUA, str));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
