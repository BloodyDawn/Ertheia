package dwo.scripts.npc.castle;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.12
 * Time: 13:13
 */

public class CastleDefenceTeleporter extends Quest
{
	private static final int[] DefenceTeleporters = {
		35261, 35262, 35263, 35264, 35265,  // Аден
		35497, 35498, 35499
	}; // Руна (TODO: 35550, 35551 есть так же и в CastleDoorman)

	private static final int[] DefenceTeleportersLikeGludio = {
		35092, 35093, 35094, // Глудио
		35134, 35135, 35136, // Дион
		35176, 35177, 35178, // Гиран
		35218, 35219, 35220, // Орен
		35308, 35309, 35310, // Иннадрил
		35352, 35353, 35354, // Годдарт
		35544, 35545, 35546
	};// Штуттгарт

	public CastleDefenceTeleporter()
	{
		addAskId(DefenceTeleportersLikeGludio, -5);
		addAskId(DefenceTeleporters, -5);
		addFirstTalkId(DefenceTeleportersLikeGludio);
		addFirstTalkId(DefenceTeleporters);
	}

	public static void main(String[] args)
	{
		new CastleDefenceTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 0)
		{
			if(Rnd.get(100) < 33)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(1));
			}
			else if(Rnd.get(100) < 66)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(2));
			}
			else
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(3));
			}
		}
		else if(reply == 1)
		{
			if(Rnd.get(100) < 33)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(11));
			}
			else if(Rnd.get(100) < 66)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(12));
			}
			else
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(13));
			}
		}
		else if(reply == 2)
		{
			if(Rnd.get(100) < 33)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(21));
			}
			else if(Rnd.get(100) < 66)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(22));
			}
			else
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(23));
			}
		}
		else if(reply == 3)
		{
			if(Rnd.get(100) < 33)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(31));
			}
			else if(Rnd.get(100) < 66)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(32));
			}
			else
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(33));
			}
		}
		else if(reply == 4)
		{
			if(Rnd.get(100) < 33)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(41));
			}
			else if(Rnd.get(100) < 66)
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(42));
			}
			else
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(43));
			}
		}
		else if(reply == 5)
		{
			if(npc.isMyLord(player, false))
			{
				player.teleToLocation(npc.getTemplate().getTelePosition(51));
			}
			else
			{
				return "noAuthority.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(!npc.isMyLord(player, false))
		{
			if(ArrayUtils.contains(DefenceTeleportersLikeGludio, npc.getNpcId()))
			{
				return "gludio_defend_teleporter" + npc.getServerName().substring(npc.getServerName().length() - 1) + "002.htm";
			}
			return npc.getServerName() + "001.htm";
		}
		if(ArrayUtils.contains(DefenceTeleportersLikeGludio, npc.getNpcId()))
		{
			return "gludio_defend_teleporter" + npc.getServerName().substring(npc.getServerName().length() - 1) + "001.htm";
		}
		return npc.getServerName() + "001.htm";
	}
}