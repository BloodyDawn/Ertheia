package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;

/**
 * User: Bacek
 * Date: 26.01.13
 * Time: 19:30
 */
public class Chandra extends Quest
{
	private static final int NPC = 32645;

	public Chandra()
	{
		addAskId(NPC, -7801);
	}

	public static void main(String[] args)
	{
		new Chandra();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -7801:
				if(reply == 1)
				{
					if(player.getParty() != null)
					{
						if(player.getParty().isLeader(player))
						{
							for(L2PcInstance member : player.getParty().getMembers())
							{
								if(!Util.checkIfInRange(1000, player, member, true))
								{
									player.getParty().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
									return null;
								}
							}

							for(L2PcInstance member : player.getParty().getMembers())
							{
								member.teleToLocation(173492, -112272, -5200);
							}
						}
						else
						{
							return "chandra003.htm";
						}
					}
					else
					{
						player.teleToLocation(173492, -112272, -5200);
					}
					return null;
				}
		}
		return null;
	}
}
