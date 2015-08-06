package dwo.gameserver.network.game.serverpackets.packet.henna;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class HennaUnequipInfo extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private L2HennaInstance _henna;

	public HennaUnequipInfo(L2HennaInstance henna, L2PcInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_henna.getSymbolId()); //symbol Id
		writeD(_henna.getItemIdDye()); //item id of dye
		writeQ(0x00); // total amount of dye require
		writeQ(_henna.getPrice() / 5); //total amount of aden require to remove symbol
		writeD(1); //able to remove or not 0 is false and 1 is true
		writeQ(_activeChar.getAdenaCount());
		writeD(_activeChar.getINT()); //current INT
		writeC(_activeChar.getINT() - _henna.getStatINT()); //equip INT
		writeD(_activeChar.getSTR()); //current STR
		writeC(_activeChar.getSTR() - _henna.getStatSTR()); //equip STR
		writeD(_activeChar.getCON()); //current CON
		writeC(_activeChar.getCON() - _henna.getStatCON()); //equip CON
		writeD(_activeChar.getMEN()); //current MEM
		writeC(_activeChar.getMEN() - _henna.getStatMEN());    //equip MEM
		writeD(_activeChar.getDEX()); //current DEX
		writeC(_activeChar.getDEX() - _henna.getStatDEX());    //equip DEX
		writeD(_activeChar.getWIT()); //current WIT
		writeC(_activeChar.getWIT() - _henna.getStatWIT());    //equip WIT
        writeD(_activeChar.getLUC()); // current LUC
        writeC(_activeChar.getLUC() - _henna.getStatLUC()); // equip LUC
        writeD(_activeChar.getCHA()); // current CHA
        writeC(_activeChar.getCHA() - _henna.getStatCHA());

        writeD(0x00);  //todo xz
    }
}
