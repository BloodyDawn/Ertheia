package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.09.12
 * Time: 21:37
 */

public class VoucherTrader extends Quest
{
	private static final int[] VoucherTraders = {33385, 33386, 33387, 33388};

	// Знаки путешественников
	private static final int SignOfAllegiance = 17739;
	private static final int SignOfPledge = 17740;
	private static final int SignOfSincerity = 17741;
	private static final int SignOfWill = 17742;

	// Печати
	private static final int SealOfAllegiance = 17743;
	private static final int SealOfPledge = 17744;
	private static final int SealOfSincerity = 17745;
	private static final int SealOfWill = 17746;

	public VoucherTrader()
	{
		addAskId(VoucherTraders, -928);
	}

	public static void main(String[] args)
	{
		new VoucherTrader();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(VoucherTraders, npc.getNpcId()))
		{
			switch(reply)
			{
				case 0: // Обменять Знак Путешественника на Печать
					int sign = -1;
					int seal = -1;
					int exp = -1;
					switch(npc.getNpcId())
					{
						case 33385:
							sign = SignOfAllegiance;
							seal = SealOfAllegiance;
							exp = 60000000;
							break;
						case 33386:
							sign = SignOfPledge;
							seal = SealOfPledge;
							exp = 66000000;
							break;
						case 33387:
							sign = SignOfSincerity;
							seal = SealOfSincerity;
							exp = 68000000;
							break;
						case 33388:
							sign = SignOfWill;
							seal = SealOfWill;
							exp = 76000000;
							break;
					}

					if(player.getInventory().getCountOf(sign) == 0)
					{
						return npc.getServerName() + "000b.htm";
					}
					else if(player.getVariablesController().get(npc.getServerName() + "_trade") != null && player.getVariablesController().get(npc.getServerName() + "_trade", Long.class, 0L) > System.currentTimeMillis())
					{
						return npc.getServerName() + "002.htm";
					}
					else
					{
						player.getVariablesController().set(npc.getServerName() + "_trade", System.currentTimeMillis() + 24 * 60 * 60000);
						player.exchangeItemsById(ProcessType.NPC, npc, sign, 1, seal, 20, true);
						player.addExpAndSp(exp, 0);
						return npc.getServerName() + "003a.htm";
					}
				case 1: // Посмотреть список вещей, на которые можно обменять на Печать
					switch(npc.getNpcId())
					{
						case 33385:
							MultiSellData.getInstance().separateAndSend(720, player, npc);
							break;
						case 33386:
							MultiSellData.getInstance().separateAndSend(721, player, npc);
							break;
						case 33387:
							MultiSellData.getInstance().separateAndSend(722, player, npc);
							break;
						case 33388:
							MultiSellData.getInstance().separateAndSend(723, player, npc);
							break;
					}
					return null;
				case 2: // Спросить, что такое Знак Путешественника
					return "voucher_trader001.htm";
				case 3: // Попрощаться
					Instance instance = InstanceManager.getInstance().getInstance(player.getInstanceId());
					if(instance != null)
					{
						instance.ejectPlayer(player.getObjectId());
					}
					return null;
				case 4: // Обменять Печать Верности на Талисман
					return npc.getServerName() + "004.htm";
				case 11:
					return npc.getServerName() + "005.htm";
				case 12:
				{
					if(!player.isAwakened())
					{
						return "voucher_trader1007.htm";
					}
					int npcOffset = npc.getNpcId() - 33385 << 3;
					switch(player.getClassId().getGeneralIdForAwaken())
					{
						case 139:
							MultiSellData.getInstance().separateAndSend(735 + npcOffset, player, npc);
							break;
						case 140:
							MultiSellData.getInstance().separateAndSend(736 + npcOffset, player, npc);
							break;
						case 141:
							MultiSellData.getInstance().separateAndSend(737 + npcOffset, player, npc);
							break;
						case 142:
							MultiSellData.getInstance().separateAndSend(738 + npcOffset, player, npc);
							break;
						case 143:
							MultiSellData.getInstance().separateAndSend(739 + npcOffset, player, npc);
							break;
						case 144:
							MultiSellData.getInstance().separateAndSend(740 + npcOffset, player, npc);
							break;
						case 145:
							MultiSellData.getInstance().separateAndSend(741 + npcOffset, player, npc);
							break;
						case 146:
							MultiSellData.getInstance().separateAndSend(742 + npcOffset, player, npc);
							break;
					}
					return null;
				}
				case 733:
				case 729:
					if(!player.isAwakened())
					{
						return "voucher_trader1007.htm";
					}
					int npcOffset = npc.getNpcId() - 33385 << 3;
					MultiSellData.getInstance().separateAndSend(reply + npcOffset, player, npc);
					return null;
			}
		}
		return null;
	}
}