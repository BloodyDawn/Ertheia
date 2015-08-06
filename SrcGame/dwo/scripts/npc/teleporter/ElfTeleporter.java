package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.12.12
 * Time: 22:09
 */

public class ElfTeleporter extends Quest
{
	private static final int NPC = 33073;

	public ElfTeleporter()
	{
		addAskId(NPC, -511);
		addAskId(NPC, -512);
		addAskId(NPC, -513);
		addAskId(NPC, -514);
		addAskId(NPC, -515);
		addAskId(NPC, -516);
		addAskId(NPC, -517);
		addAskId(NPC, -518);
	}

	public static void main(String[] args)
	{
		new ElfTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -511:
				player.teleToLocation(-17151, 208992, -3664);
				return null;
			case -512:
				player.teleToLocation(-16832, 194491, -4208);
				return null;
			case -513:
				player.teleToLocation(-9570, 176327, -4144);
				return null;
			case -514:
				player.teleToLocation(-16415, 174003, -3304);
				return null;
			case -515:
				player.teleToLocation(-16516, 182801, -3872);
				return null;
			case -516:
				player.teleToLocation(-53027, 172265, -3680);
				return null;
			case -517:
				player.teleToLocation(-31348, 169552, -3840);
				return null;
			case -518:
				player.teleToLocation(-25536, 183672, -3814);
				return null;
		}
		return null;
	}
}