package dwo.scripts.npc.teleporter;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 19:35
 */

public class FantasyIslandTeleporter extends Quest
{
	private static final int NPC = 32378;

	public FantasyIslandTeleporter()
	{
		addAskId(NPC, -1816);
	}

	public static void main(String[] args)
	{
		new FantasyIslandTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1816)
		{
			switch(reply)
			{
				case 1:
					int i0 = player.getVariablesController().get(TownTeleporter.class.getSimpleName(), Integer.class, 0);
					int i1 = i0 / 1000000;
					if(i1 >= 100)
					{
						i1 = 100;
					}
					if(i1 < 0)
					{
						i1 = 0;
					}
					if(i1 == 0)
					{
						player.teleToLocation(43835, -47749, -792);
					}
					switch(i1)
					{
						case 1:
							player.teleToLocation(-12787, 122779, -3114);
							break;
						case 2:
							player.teleToLocation(-80684, 149770, -3043);
							break;
						case 3:
							player.teleToLocation(15472, 142880, -2699);
							break;
						case 4:
							player.teleToLocation(83551, 147945, -3400);
							break;
						case 5:
							player.teleToLocation(82971, 53207, -1470);
							break;
						case 6:
							player.teleToLocation(117088, 76931, -2670);
							break;
						case 7:
							player.teleToLocation(146783, 25808, -2000);
							break;
						case 8:
							player.teleToLocation(111455, 219400, -3546);
							break;
						case 9:
							player.teleToLocation(46911, 49441, -3056);
							break;
						case 10:
							player.teleToLocation(148024, -55281, -2728);
							break;
						case 11:
							player.teleToLocation(43835, -47749, -792);
							break;
						case 12:
							player.teleToLocation(87126, -143520, -1288);
							break;
						case 13:
							player.teleToLocation(-84752, 243122, -3728);
							break;
						case 14:
							player.teleToLocation(11179, 15848, -4584);
							break;
						case 15:
							player.teleToLocation(17441, 170434, -3504);
							break;
						case 16:
							player.teleToLocation(-44132, -113766, -240);
							break;
						case 17:
							player.teleToLocation(114976, -178774, -856);
							break;
						case 18:
							player.teleToLocation(-119377, 47000, 360);
							break;
						default:
							npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.IF_YOUR_MEANS_OF_ARRIVAL_WAS_A_BIT_UNCONVENTIONAL_THEN_ILL_BE_SENDING_YOU_BACK_TO_RUNE_TOWNSHIP_WHICH_IS_THE_NEAREST_TOWN));
							break;
					}
					i1 *= 1000000;
					player.getVariablesController().set(getClass().getSimpleName(), i0 - i1);
					return null;
				case 2:
					if(Rnd.get(4) < 1)
					{
						player.teleToLocation(-81896, -49589, -10352);
					}
					else if(Rnd.get(3) < 1)
					{
						player.teleToLocation(-82271, -49196, -10352);
					}
					else if(Rnd.get(2) < 1)
					{
						player.teleToLocation(-81886, -48784, -10352);
					}
					else
					{
						player.teleToLocation(-81490, -49167, -10352);
					}
					return null;
				case 3:
					int i3 = Rnd.get(3) + 1;
					if(i3 == 1)
					{
						player.teleToLocation(-70411, -70958, -1416);
					}
					else if(i3 == 2)
					{
						player.teleToLocation(-70522, -71026, -1416);
					}
					else
					{
						player.teleToLocation(-70293, -71029, -1416);
					}
					return null;
				case 4:
					MultiSellData.getInstance().separateAndSend(643, player, npc);
					return null;
				case 5:
					player.teleToLocation(-57328, -60566, -2320);
					return null;
			}
		}
		return null;
	}
}