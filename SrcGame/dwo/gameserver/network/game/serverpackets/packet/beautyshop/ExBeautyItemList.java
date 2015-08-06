/*package dwo.gameserver.network.game.serverpackets.packet.beautyshop;

import dwo.gameserver.datatables.xml.BeautyShopData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: GenCloud
 * Date: 20.01.2015
 * Team: La2Era Team

public class ExBeautyItemList extends L2GameServerPacket
{
    private int _colorCount;
    private final BeautyShopData.BeautyShopList _beautyData;
    private final Map<Integer, List<BeautyShopData.BeautyShopList>> _colorData = new HashMap<>();
    private static final int HAIR_TYPE = 0;
    private static final int FACE_TYPE = 1;
    private static final int COLOR_TYPE = 2;

    public ExBeautyItemList(L2PcInstance activeChar)
    {
        _beautyData = BeautyShopData.getInstance().getBeautyById(activeChar.getRace(), activeChar.getAppearance().getSex());

        for (BeautyShopData.BeautyShopList hair : _beautyData._colorList)
        {
            List<BeautyShopData.BeautyShopList> colors = new ArrayList<>();
            for (BeautyShopData.BeautyShopList color : hair.getColors().values())
            {
                colors.add(color);
                _colorCount++;
            }
            _colorData.put(hair.getId(), colors);
        }
    }

    @Override
    protected void writeImpl()
    {
        writeD(HAIR_TYPE);
        writeD(_beautyData.getHairList().size());
        for (BeautyItem hair : _beautyData.getHairList().values())
        {
            writeD(0); // ?
            writeD(hair.getId());
            writeD(hair.getAdena());
            writeD(hair.getResetAdena());
            writeD(hair.getBeautyShopTicket());
            writeD(1); // Limit
        }

        writeD(FACE_TYPE);
        writeD(_beautyData.getFaceList().size());
        for (BeautyItem face : _beautyData.getFaceList().values())
        {
            writeD(0); // ?
            writeD(face.getId());
            writeD(face.getAdena());
            writeD(face.getResetAdena());
            writeD(face.getBeautyShopTicket());
            writeD(1); // Limit
        }

        writeD(COLOR_TYPE);
        writeD(_colorCount);
        for (int hairId : _colorData.keySet())
        {
            for (BeautyItem color : _colorData.get(hairId))
            {
                writeD(hairId);
                writeD(color.getId());
                writeD(color.getAdena());
                writeD(color.getResetAdena());
                writeD(color.getBeautyShopTicket());
                writeD(1);
            }
        }
    }
}
 */