package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.HennaTreeTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.henna.HennaEquipList;
import dwo.gameserver.network.game.serverpackets.packet.henna.HennaUnequipList;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.09.12
 * Time: 14:41
 */

public class SymbolMaker extends Quest
{
	private static final int[] SymbolMakers = {
		31046, 31047, 31048, 31049, 31050, 31051, 31052, 31053, 31264, 31308, 31953
	};

	public SymbolMaker()
	{
		addAskId(SymbolMakers, -16);
	}

	public static void main(String[] args)
	{
		new SymbolMaker();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			player.sendPacket(new HennaEquipList(player, HennaTreeTable.getInstance().getAvailableHenna(player.getClassId().getId())));
		}
		else if(reply == 2)
		{
			boolean hasHennas = false;
			for(int i = 1; i <= 3; i++)
			{
				L2HennaInstance henna = player.getHenna(i);

				if(henna != null)
				{
					hasHennas = true;
				}
			}
			if(hasHennas)
			{
				player.sendPacket(new HennaUnequipList(player));
			}
		}
		return null;
	}
}