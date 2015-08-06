package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.11.12
 * Time: 0:49
 */

public class PvpMerchant extends Quest
{
	// Клановые торговцы
	private static final int[] NPCs = {36479, 36480};

	public PvpMerchant()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -3001);
		addAskId(NPCs, -4001);
	}

	public static void main(String[] args)
	{
		new PvpMerchant();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(NPCs, npc.getNpcId()))
		{
			if(ask == -3001)
			{
				switch(reply)
				{
					case 1:
						if(player.getFame() > 0)
						{
							MultiSellData.getInstance().separateAndSend(638, player, npc);
							return null;
						}
						else
						{
							return npc.getServerName() + "002.htm";
						}
					case 2:
						if(player.getFame() > 0)
						{
							MultiSellData.getInstance().separateAndSend(639, player, npc);
							return null;
						}
						else
						{
							return npc.getServerName() + "002.htm";
						}
					case 3:
						if(player.getFame() > 0)
						{
							MultiSellData.getInstance().separateAndSend(640, player, npc);
							return null;
						}
						else
						{
							return npc.getServerName() + "002.htm";
						}
				}
			}
			else if(ask == -4001)
			{
				switch(reply)
				{
					case 1:
						if(player.getPkKills() > 0)
						{
							if(player.getFame() >= 5000)
							{
								if(player.getLevel() >= 40 && player.getClassId().level() >= ClassLevel.SECOND.ordinal())
								{
									player.setFame(player.getFame() - 5000);
									player.setPkKills(player.getPkKills() - 1);
									player.sendUserInfo();
									return npc.getServerName() + "007.htm";
								}
								else
								{
									return npc.getServerName() + "002.htm";
								}
							}
							else
							{
								return npc.getServerName() + "002.htm";
							}
						}
						else
						{
							return npc.getServerName() + "002.htm";
						}
					case 2:
						if(player.getClan() != null && player.getClan().getLevel() >= 5)
						{
							if(player.getFame() >= 1000)
							{
								if(player.getLevel() >= 40 && player.getClassId().level() >= ClassLevel.SECOND.ordinal())
								{
									player.setFame(player.getFame() - 1000);
									player.getClan().addReputationScore(50, true);
									player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
									player.sendPacket(SystemMessageId.ACQUIRED_50_CLAN_FAME_POINTS);
									return npc.getServerName() + "007.htm";
								}
								else
								{
									return npc.getServerName() + "002.htm";
								}
							}
							else
							{
								return npc.getServerName() + "002.htm";
							}
						}
						else
						{
							return npc.getServerName() + "002.htm";
						}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getFame() > 0)
		{
			return player.getLevel() >= 40 && player.getClassId().level() >= ClassLevel.SECOND.ordinal() ? npc.getServerName() + "001.htm" : npc.getServerName() + "002.htm";
		}
		else
		{
			return npc.getServerName() + "002.htm";
		}
	}
}