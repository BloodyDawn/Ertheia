package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 20:03
 */

public class RaceTrackTeleporter extends Quest
{
	private static final int NPC = 30995;

	public RaceTrackTeleporter()
	{
		addAskId(NPC, 255);
		addFirstTalkId(NPC);
	}

	public static void main(String[] args)
	{
		new RaceTrackTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 255)
		{
			if(reply == 1)
			{
				int i1 = player.getVariablesController().get(TownTeleporter.class.getSimpleName(), Integer.class, 0);
				if(i1 >= 95 && i1 < 195)
				{
					player.teleToLocation(-12782, 122862, -3114);
					return null;
				}
				if(i1 >= 195 && i1 < 295)
				{
					player.teleToLocation(-80684, 149770, -3043);
					return null;
				}
				if(i1 >= 295 && i1 < 395)
				{
					player.teleToLocation(15744, 142928, -2704);
					return null;
				}
				if(i1 >= 395 && i1 < 495)
				{
					player.teleToLocation(83475, 147966, -3404);
					return null;
				}
				if(i1 >= 495 && i1 < 595)
				{
					player.teleToLocation(82971, 53207, -1470);
					return null;
				}
				if(i1 >= 595 && i1 < 695)
				{
					player.teleToLocation(117110, 76883, -2670);
					return null;
				}
				if(i1 >= 695 && i1 < 795)
				{
					player.teleToLocation(146705, 25840, -2000);
					return null;
				}
				if(i1 >= 795 && i1 < 895)
				{
					player.teleToLocation(111333, 219345, -3546);
					return null;
				}
				if(i1 >= 895 && i1 < 995)
				{
					player.teleToLocation(12919, 181038, -3560);
					return null;
				}
				if(i1 >= 995 && i1 < 1095)
				{
					player.teleToLocation(147870, -55380, -2728);
					return null;
				}
				if(i1 >= 1095 && i1 < 1195)
				{
					player.teleToLocation(43845, -47820, -792);
					return null;
				}
				if(i1 >= 1195 && i1 < 1295)
				{
					player.teleToLocation(87099, -143426, -1288);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "mr_keeper.htm";
	}
}