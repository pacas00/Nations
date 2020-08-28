package com.arckenver.nations.cmdexecutor.zone;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ZoneSetDisplayNameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.zone.setdisplayname")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new ZoneSetDisplayNameExecutor())
				.build(), "setdisplayname", "setdname", "setdisplay");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			String zoneName = null;
			if (ctx.<String>getOne("name").isPresent())
			{
				zoneName = ctx.<String>getOne("name").get();
			}
			Player player = (Player) src;
			Nation nation = DataHandler.getNation(player.getLocation());
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDNATION));
				return CommandResult.success();
			}
			Zone currentZone = nation.getZone(player.getLocation());
			if (currentZone == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDZONESELF));
				return CommandResult.success();
			}//if zone owner, co owner or rather nation staff
			if (!currentZone.isCoowner(player.getUniqueId()) && !currentZone.isOwner(player.getUniqueId()) && !nation.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			currentZone.setDisplayName(zoneName);
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_ZONERENAME.replaceAll("\\{ZONE\\}", currentZone.getDisplayName())));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
