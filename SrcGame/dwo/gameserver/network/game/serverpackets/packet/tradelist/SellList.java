package dwo.gameserver.network.game.serverpackets.packet.tradelist;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

public class SellList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2Npc _lease;
	private long _money;
	private List<L2ItemInstance> _selllist = new FastList<>();

	public SellList(L2PcInstance player)
	{
		_activeChar = player;
		_lease = null;
		_money = _activeChar.getAdenaCount();
		doLease();
	}

	public SellList(L2PcInstance player, L2Npc lease)
	{
		_activeChar = player;
		_lease = lease;
		_money = _activeChar.getAdenaCount();
		doLease();
	}

	private void doLease()
	{
		if(_lease == null)
		{
			for(L2ItemInstance item : _activeChar.getInventory().getItems())
			{
				boolean isPetControlItem = false;
				if(!_activeChar.getPets().isEmpty())
				{
					for(L2Summon pet : _activeChar.getPets())
					{
						if(pet.getControlObjectId() == item.getObjectId())
						{
							isPetControlItem = true;
						}
					}
				}
				if(!item.isEquipped() &&            // Not equipped
					item.isSellable() &&        // Item is sellable
					!isPetControlItem)            // Pet is summoned and not the item that summoned the pet
				{
					_selllist.add(item);
					if(Config.DEBUG)
					{
						_log.log(Level.DEBUG, "item added to selllist: " + item.getItem().getName());
					}
				}
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_money);
		writeD(_lease == null ? 0x00 : 1000000 + _lease.getTemplate().getNpcId());
		writeH(_selllist.size());

		for(L2ItemInstance item : _selllist)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.isEquipped() ? 0x01 : 0x00);
			writeD((int) item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(item.getCustomType2());
			writeQ(item.getItem().getReferencePrice() / 2);

			// T1
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for(byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			for(int i = 0; i < 3; i++)
			{
				writeH(item.getEnchantEffect()[i]);
			}
			writeD(item.getSkin());
		}
	}
}
