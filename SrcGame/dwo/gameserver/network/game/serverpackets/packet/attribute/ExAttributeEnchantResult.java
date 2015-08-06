package dwo.gameserver.network.game.serverpackets.packet.attribute;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExAttributeEnchantResult extends L2GameServerPacket
{
	private int _result;
	private boolean _isWeapon;
	private int _attrType;
	private int _beforeAttrValue;
	private int _afterAttrValue;
	private long _successCount;

	public ExAttributeEnchantResult(int result, boolean isWeapon, int attrType, int beforeAttrValue, int afterAttrValue, long successCount)
	{
		_result = result;
		_isWeapon = isWeapon;
		_attrType = attrType;
		_beforeAttrValue = beforeAttrValue;
		_afterAttrValue = afterAttrValue;
		_successCount = successCount;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeC(_isWeapon ? 1 : 0);
		writeH(_attrType);
		writeH(_beforeAttrValue);
		writeH(_afterAttrValue);
		writeQ(_successCount);
	}
}
