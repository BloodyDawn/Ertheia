package dwo.gameserver.network.game.serverpackets.packet.gmview;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

public class ExGMViewQuestItemList extends L2GameServerPacket
{
	private FastList<L2ItemInstance> _questItems = new FastList();

	public ExGMViewQuestItemList(L2PcInstance player)
	{
		for(L2ItemInstance item : player.getInventory().getItems())
		{
			if(item.getItem() != null && item.isQuestItem())
			{
				_questItems.add(item); // add to questinv
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeH(_questItems.size());
		_questItems.stream().filter(L2ItemInstance::isQuestItem).forEach(this::writeItemInfo);
	}
}
