package com.arckenver.nations.cmdexecutor.nationadmin;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.cmdelement.NationNameElement;
import com.arckenver.nations.cmdexecutor.nation.NationBuyextraExecutor;
import com.arckenver.nations.object.Nation;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NationadminSetRentIntervalExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nationadmin.setrentinterval")
				.arguments(
						new NationNameElement(Text.of("nation")),
						GenericArguments.optional(GenericArguments.integer(Text.of("interval"))))
				.executor(new NationadminSetRentIntervalExecutor())
				.build(), "setrentinterval", "rentinterval");
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		Nation nation = DataHandler.getNation(ctx.<String>getOne("nation").get());
		if (nation == null) {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDNATIONNAME));
			return CommandResult.success();
		}
		int n = ConfigHandler.getNode("nations", "defaultRentInterval").getInt();
		if (ctx.<String>getOne("interval").isPresent()) {
			n = ctx.<Integer>getOne("interval").get();
		}
		if(n < 0) {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEGATIVEINTERVAL));
			return CommandResult.success();
		}
		nation.setRentInterval(n);
		DataHandler.saveNation(nation.getUUID());
		src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTINTERVAL.replaceAll("\\{NUMBER\\}", n + "")));
		return CommandResult.success();
	}
}
