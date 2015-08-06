package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.xml.ObsceneFilterTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;

/**
 * @author KenM, Gnacik
 */

public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final int[] COLORS = {
		0x9393FF,    // Pink
		0x7C49FC,    // Rose Pink
		0x97F8FC,    // Lemon Yellow
		0xFA9AEE,    // Lilac
		0xFF5D93,    // Cobalt Violet
		0x00FCA0,    // Mint Green
		0xA0A601,    // Peacock Green
		0x7898AF,    // Yellow Ochre
		0x486295,    // Chocolate
		0x999999    // Silver
	};

	private int _colorNum;
	private int _itemObjectId;
	private String _title;

	@Override
	protected void readImpl()
	{
		_colorNum = readD();
		_title = readS();
		_itemObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_colorNum < 0 || _colorNum >= COLORS.length)
		{
			return;
		}

		if(ObsceneFilterTable.getInstance().isObsceneWord(_title))
		{
			activeChar.sendMessage("Ваш ник или титул содержит нецензурную лексику. Выберите что-нибудь более достойное.");
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if(item == null || !item.isEtcItem() || item.getEtcItem().getHandlerName() == null || !item.getEtcItem().getHandlerName().equalsIgnoreCase("NicknameColor"))
		{
			return;
		}

		if(activeChar.destroyItem(ProcessType.CONSUMEWITHOUTTRACE, item, 1, null, true))
		{
			activeChar.setTitle(_title);
			activeChar.getAppearance().setTitleColor(COLORS[_colorNum]);
			activeChar.broadcastUserInfo();
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:4F RequestChangeNicknameColor";
	}
}