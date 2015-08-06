package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 22.10.11
 * Time: 19:55
 */

public class RaikelTeleport extends Quest
{
	private static final int Raikel = 33123;

	public RaikelTeleport()
	{
		addAskId(Raikel, -6098);
		addAskId(Raikel, -6099);
		addAskId(Raikel, -6100);
		addAskId(Raikel, -6102);
		addAskId(Raikel, -6103);
		addFirstTalkId(Raikel);
	}

	public static void main(String[] args)
	{
		new RaikelTeleport();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -6098:
				player.teleToLocation(-107881, 248658, -3224);
				return null;
			case -6099:
				player.teleToLocation(-119592, 246398, -1232);
				return null;
			case -6100:
				return "si_illusion_people31003.htm";
			case -6102:
				player.teleToLocation(-114986, 226633, -2864);
				return null;
			case -6103:
				player.teleToLocation(-109300, 237498, -2944);
				return null;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return !player.getVariablesController().get(getClass().getSimpleName(), Boolean.class, false) ? "si_illusion_people31001.htm" : "si_illusion_people31002.htm";
	}
}