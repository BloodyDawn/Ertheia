package dwo.gameserver.network.game.serverpackets.packet.trade;

import dwo.config.Config;
import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class TradeStart extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
    private L2PcInstance _partner;
	private L2ItemInstance[] _itemList;
    private int _mask = 0;

	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
        _partner = player.getActiveTradeList().getPartner();
		_itemList = _activeChar.getInventory().getAvailableItems(true, _activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS, false);

        if (_partner != null)
        {
            if (RelationListManager.getInstance().getFriendList(_partner.getObjectId()).size() > 0)
            {
                _mask |= 0x01;
            }
            if ((player.getClanId() > 0) && (_partner.getClanId() == _partner.getClanId()))
            {
                _mask |= 0x02;
            }
            if ((MentorManager.getInstance().getMentee(player.getObjectId(), _partner.getObjectId()) != null) || (MentorManager.getInstance().getMentee(_partner.getObjectId(), player.getObjectId()) != null))
            {
                _mask |= 0x04;
            }
            if ((player.getAllyId() > 0) && (player.getAllyId() == _partner.getAllyId()))
            {
                _mask |= 0x08;
            }

            // Does not shows level
            if (_partner.isGM())
            {
                _mask |= 0x10;
            }
        }
    }

	@Override
	protected void writeImpl()
	{
		if(_activeChar.getActiveTradeList() == null || _activeChar.getActiveTradeList().getPartner() == null)
		{
			return;
		}

		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
        
        writeC(_mask);
        if ((_mask & 0x10) == 0)
        {
            writeC(_partner.getLevel());
        }
        
		writeH(_itemList.length);
		for(L2ItemInstance item : _itemList)
		{
			writeItemInfo(item);
		}
	}
}
