package dwo.gameserver.network.game.serverpackets.packet.beautyshop;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 17.11.12
 * Time: 12:24
 */
public class ExResponseBeautyRegistReset extends L2GameServerPacket
{
	private final long _ownAdena;
	private final int _hairStyle;
	private final int _faceStyle;
	private final int _hairColor;
	private final int _isSuccess;
	private final int _shopType;

	public ExResponseBeautyRegistReset(long ownAdena, int isSuccess, int hairStyle, int faceStyle, int hairColor, int shopType)
	{
		_ownAdena = ownAdena;
		_isSuccess = isSuccess;
		_hairStyle = hairStyle;
		_faceStyle = faceStyle;
		_hairColor = hairColor;
		_shopType = shopType;
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_ownAdena);   // _ownAdena
		writeQ(0x00);   // ownCoin
		writeD(_shopType);   // shopType
		writeD(_isSuccess);   // isSuccess

		writeD(_hairStyle);   // User__SetHairStyle
		writeD(_faceStyle);   // User__SetFaceStyle
		writeD(_hairColor);   // User__SetHairColor
	}
}
