package dwo.scripts.npc.hellbound;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.11.12
 * Time: 22:23
 */

public class CaravanHude extends Quest
{
	// Квестовый персонаж
	private static final int Hude = 32298;

	// Сертификаты
	private static final int BASIC_CERT = 9850;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;

	// Предметы для обмена на сертификаты
	private static final int MARK_OF_BETRAYAL = 9676;
	private static final int STINGER = 10012;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	private static final int MAP = 9994;

	public CaravanHude()
	{
		addFirstTalkId(Hude);
		addAskId(Hude, -1006);
		addAskId(Hude, -303);
	}

	public static void main(String[] args)
	{
		new CaravanHude();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == Hude)
		{
			if(ask == -1006)
			{
				switch(reply)
				{
					case 2:
						if(player.getItemsCount(BASIC_CERT) >= 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
						{
							if(player.getItemsCount(STINGER) >= 60 && player.getItemsCount(MARK_OF_BETRAYAL) >= 30)
							{
								player.addItem(ProcessType.NPC, STANDART_CERT, 1, npc, true);
								player.destroyItemByItemId(ProcessType.NPC, BASIC_CERT, 1, npc, true);
								player.destroyItemByItemId(ProcessType.NPC, STINGER, 60, npc, true);
								player.destroyItemByItemId(ProcessType.NPC, MARK_OF_BETRAYAL, 30, npc, true);
								return "caravan_hude004a.htm";
							}
							else
							{
								return "caravan_hude004b.htm";
							}
						}
						break;
					case 3:
						if(player.getItemsCount(STANDART_CERT) >= 1 && player.getItemsCount(PREMIUM_CERT) < 1)
						{
							if(player.getItemsCount(LIFE_FORCE) >= 56 && player.getItemsCount(CONTAINED_LIFE_FORCE) >= 14)
							{
								player.addItem(ProcessType.NPC, PREMIUM_CERT, 1, npc, true);
								player.addItem(ProcessType.NPC, MAP, 1, npc, true);
								player.destroyItemByItemId(ProcessType.NPC, STANDART_CERT, 1, npc, true);
								player.destroyItemByItemId(ProcessType.NPC, LIFE_FORCE, 56, npc, true);
								player.destroyItemByItemId(ProcessType.NPC, CONTAINED_LIFE_FORCE, 14, npc, true);
								return "caravan_hude006a.htm";
							}
							else
							{
								return "caravan_hude006b.htm";
							}
						}
						break;
				}
			}
			else if(ask == -303)
			{
				if(reply == 612)
				{
					if(player.getItemsCount(STANDART_CERT) >= 1 || player.getItemsCount(PREMIUM_CERT) >= 1)
					{
						MultiSellData.getInstance().separateAndSend(reply, player, npc);
						return null;
					}
				}
				else if(reply == 623)
				{
					if(player.getItemsCount(PREMIUM_CERT) >= 1)
					{
						MultiSellData.getInstance().separateAndSend(reply, player, npc);
						return null;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getItemsCount(BASIC_CERT) < 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
		{
			return "caravan_hude001.htm";
		}
		if(player.getItemsCount(BASIC_CERT) >= 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
		{
			return "caravan_hude003.htm";
		}
		if(player.getItemsCount(STANDART_CERT) >= 1 && player.getItemsCount(PREMIUM_CERT) < 1)
		{
			return "caravan_hude005.htm";
		}
		if(player.getItemsCount(PREMIUM_CERT) >= 1)
		{
			return "caravan_hude007.htm";
		}
		return null;
	}
}