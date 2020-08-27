package com.arckenver.nations.cmdexecutor.nation;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.object.Nation;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NationSetRentIntervalExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nation.setrentinterval")
				.arguments(GenericArguments.optional(GenericArguments.integer(Text.of("interval"))))
				.executor(new NationSetRentIntervalExecutor())
				.build(), "setrentinterval", "rentinterval");
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {

			Player player = (Player) src;
			Nation nation = DataHandler.getNationOfPlayer(player.getUniqueId());
			if (nation == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
				return CommandResult.success();
			}
			if (!nation.isStaff(player.getUniqueId())) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONPRES));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("interval").isPresent()) {
				src.sendMessage(Text.of(TextColors.YELLOW, "/n setrentinterval <interval>"));
				return CommandResult.success();
			}
			int n = ctx.<Integer>getOne("interval").get();
			if(n < 0) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEGATIVEINTERVAL));
				return CommandResult.success();
			}
			nation.setRentInterval(n);
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTINTERVAL.replaceAll("\\{NUMBER\\}", n + "")));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
