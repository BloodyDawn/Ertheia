package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

public class NewCharacterSuccess extends L2GameServerPacket
{
    private List<L2PcTemplate> _chars = new FastList<>();

    public void addChar(L2PcTemplate template)
    {
        _chars.add(template);
    }

    @Override
    protected void writeImpl()
    {
        writeD(_chars.size());

        for(L2PcTemplate temp : _chars)
        {
            if(temp == null)
            {
                continue;
            }

            writeD(temp.getRace().ordinal());
            writeD(temp.getClassId().getId());
            writeD(0x63);
            writeD(temp.getBaseSTR());
            writeD(0x01);
            writeD(0x63);
            writeD(temp.getBaseDEX());
            writeD(0x01);
            writeD(0x63);
            writeD(temp.getBaseCON());
            writeD(0x01);
            writeD(0x63);
            writeD(temp.getBaseINT());
            writeD(0x01);
            writeD(0x63);
            writeD(temp.getBaseWIT());
            writeD(0x01);
            writeD(0x63);
            writeD(temp.getBaseMEN());
            writeD(0x01);
        }
    }
}
