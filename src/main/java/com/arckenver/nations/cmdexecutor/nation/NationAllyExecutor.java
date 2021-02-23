package com.arckenver.nations.cmdexecutor.nation;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.Utils;
import com.arckenver.nations.cmdelement.NationNameElement;
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

public class NationAllyExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nation.info")
				.arguments(GenericArguments.optional(new NationNameElement(Text.of("nation"))))
				.executor(new NationAllyExecutor())
				.build(), "ally");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{

		Nation nation;
		Nation playerNation;
		if (ctx.<String>getOne("nation").isPresent())
		{
			nation = DataHandler.getNation(ctx.<String>getOne("nation").get());
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADNATIONNAME));
				return CommandResult.success();
			}
			if (src instanceof Player)
			{
				Player player = (Player) src;
				playerNation = DataHandler.getNationOfPlayer(player.getUniqueId());
				if (playerNation == null)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
					return CommandResult.success();
				}

				if (!playerNation.isPresident(player.getUniqueId())) {
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONPRES));
					return CommandResult.success();
				}

			} else {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
				return CommandResult.success();
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDNATIONNAME));
			return CommandResult.success();
		}


		//Ok! We are good here!

		boolean added = false;
		if (!playerNation.isAlly(nation.getUUID())) {
			playerNation.addAlly(nation.getUUID());
			added = true;
		} else {
			playerNation.removeAlly(nation.getUUID());
		}
		DataHandler.saveNation(playerNation.getUUID());

		//CHANGE THIS
		if (added) {
			src.sendMessage(Text.of(TextColors.GREEN, "Added " + nation.getDisplayName() + " as an ally of " + playerNation.getDisplayName() + "."));
		} else {
			src.sendMessage(Text.of(TextColors.GREEN, nation.getDisplayName() + " is no longer an ally of " + playerNation.getDisplayName() + "."));
		}

		return CommandResult.success();
	}

}
