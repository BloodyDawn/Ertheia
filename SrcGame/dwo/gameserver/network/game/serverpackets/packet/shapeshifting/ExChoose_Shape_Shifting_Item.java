package dwo.gameserver.network.game.serverpackets.packet.shapeshifting;

import dwo.gameserver.model.items.shapeshift.ShapeShiftingWindowType;
import dwo.gameserver.model.items.shapeshift.ShapeWindowType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:03
 */
public class ExChoose_Shape_Shifting_Item extends L2GameServerPacket
{
	private int _shapeShiftingWindow;
	private int _shapeWindow;
	private int _itemId;

	public ExChoose_Shape_Shifting_Item(ShapeWindowType shapeWindow, ShapeShiftingWindowType shapeShiftingWindow, int itemId)
	{
		_shapeWindow = shapeWindow.ordinal();
		_shapeShiftingWindow = shapeShiftingWindow.ordinal();
		_itemId = itemId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_shapeWindow);
		writeD(_shapeShiftingWindow);
		writeD(_itemId);
	}
}