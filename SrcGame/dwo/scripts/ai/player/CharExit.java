package dwo.scripts.ai.player;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExSuttleGetOff;

/**
 * L2GOD Team
 * @author Bacek
 * Date: 28.05.13.
 * Time: 19:01
 * Выполняется при выходе / вылете из игры
 */
public class CharExit extends Quest
{
	public CharExit(String name, String desc)
	{
		super(name, desc);
		addEventId(HookType.ON_DELETEME);
	}

	public static void main(String[] args)
	{
		new CharExit("CharExit", "ai");
	}

	@Override
	public void onDeleteMe(L2PcInstance player)
	{
		// Если игрок вышел / вылетел на олимпиаде останавливаем и выкидываем из инста.
		if(player.getOlympiadController().getGameId() != -1)
		{
			player.getOlympiadController().returnPlayer();

			if((OlympiadManager.getInstance().isRegistered(player) ||
				player.getOlympiadController().isParticipating() ||
				player.getOlympiadController().isPlayingNow()) && player.getOlympiadController().getGameId() != -1)
			{
				OlympiadManager.getInstance().removeDisconnectedCompetitor(player);
			}

			player.getOlympiadController().stopGame();
		}

		// Если игрок выше / вылетел во время движения лифта телепортируем его.
		if(player.isInShuttle())
		{
			Location loc = null;
			switch(player.getShuttle().getId())
			{
				case 1:
					loc = new Location(205580, 81213, 394, 473);  // ShuttleArcanaRight
					break;
				case 2:
					loc = new Location(211367, 82356, 159, 0);  // ShuttleArcanaLeft
					break;
				case 3:
					loc = new Location(17728, 115139, -11752, 473);  // ShuttleCrumaTower
					break;
			}

			player.getShuttle().removePassenger(player);
			int shuttleId = player.getShuttle().getId();
			player.setVehicle(null);
			player.setInVehiclePosition(null);

			if(loc != null)
			{
				player.broadcastPacket(new ExSuttleGetOff(player.getObjectId(), shuttleId, loc.getX(), loc.getY(), loc.getZ()));
				player.teleToLocation(loc);
			}
		}
	}
}
