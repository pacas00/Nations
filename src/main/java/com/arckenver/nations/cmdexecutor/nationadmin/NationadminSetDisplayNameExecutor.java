package com.arckenver.nations.cmdexecutor.nationadmin;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.cmdelement.NationNameElement;
import com.arckenver.nations.object.Nation;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NationadminSetDisplayNameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nationadmin.setdisplayname")
				.arguments(
						new NationNameElement(Text.of("nation")),
						GenericArguments.optional(GenericArguments.string(Text.of("displayName"))))
				.executor(new NationadminSetDisplayNameExecutor())
				.build(), "setdisplayname", "setdname", "setdisplay");
	}

	@Nonnull
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String> getOne("nation").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/na setdisplayname <nation> [tag]"));
			return CommandResult.success();
		}
		String newDisplayName = null;
		if (ctx.<String> getOne("displayName").isPresent())
			newDisplayName = ctx.<String> getOne("displayName").get();
		Nation nation = DataHandler.getNation(ctx.<String> getOne("nation").get());
		if (nation == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
			return CommandResult.success();
		}
		if (newDisplayName != null && (newDisplayName.length() < ConfigHandler.getNode("others", "minNationDisplayLength").getInt()
				|| newDisplayName.length() > ConfigHandler.getNode("others", "maxNationDisplayLength").getInt()))
		{
			src.sendMessage(Text.of(TextColors.RED,
					LanguageHandler.ERROR_DISPLAYLENGTH
							.replaceAll("\\{MIN\\}",
									ConfigHandler.getNode("others", "minNationDisplayLength").getString())
							.replaceAll("\\{MAX\\}",
									ConfigHandler.getNode("others", "maxNationDisplayLength").getString())));
			return CommandResult.success();
		}
		String oldName = nation.getDisplayName();
		nation.setDisplayName(newDisplayName);
		DataHandler.saveNation(nation.getUUID());
		MessageChannel.TO_ALL.send(Text.of(TextColors.AQUA,
				LanguageHandler.INFO_DISPLAY.replaceAll("\\{NAME\\}", nation.getName()).replaceAll("\\{OLDNAME\\}", oldName).replaceAll("\\{NEWNAME\\}", nation.getDisplayName())));
		return CommandResult.success();
	}
}

