package dwo.gameserver.network.game.serverpackets.packet.beautyshop;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 17.11.12
 * Time: 12:24
 */
public class ExResponseResetList extends L2GameServerPacket
{
	private final long _ownAdena;
	private final int _hairStyle;
	private final int _faceStyle;
	private final int _hairColor;

	public ExResponseResetList(long ownAdena, int hairStyle, int faceStyle, int hairColor)
	{
		_ownAdena = ownAdena;
		_hairStyle = hairStyle;
		_faceStyle = faceStyle;
		_hairColor = hairColor;
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_ownAdena);   // _ownAdena
		writeQ(0x00);   // ownCoin

		writeD(_hairStyle);   // User__SetHairStyle
		writeD(_faceStyle);   // User__SetFaceStyle
		writeD(_hairColor);   // User__SetHairColor
	}
}
