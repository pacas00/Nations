package com.arckenver.nations.cmdexecutor.nationadmin;

import com.arckenver.nations.cmdelement.NationNameElement;
import com.arckenver.nations.cmdexecutor.zone.ZoneListExecutor;
import com.arckenver.nations.task.RentCollectRunnable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class NationadminCollectRentExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nationadmin.collectrent")
				.arguments()
				.executor(new NationadminCollectRentExecutor())
				.build(), "collectrent", "crent", "rentpay");
	}


	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		new RentCollectRunnable().run();
		return CommandResult.success();
	}


}
