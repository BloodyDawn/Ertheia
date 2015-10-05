package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.List;

/*
*	Tauti 440 - 441
*	StatusUpdate c dddd dd (ddd) ?
*/

public class StatusUpdate extends L2GameServerPacket
{
    public static final int LEVEL = 0x01;
    public static final int EXP = 0x02;
    public static final int STR = 0x03;
    public static final int DEX = 0x04;
    public static final int CON = 0x05;
    public static final int INT = 0x06;
    public static final int WIT = 0x07;
    public static final int MEN = 0x08;

    public static final int CUR_HP = 0x09;
    public static final int MAX_HP = 0x0A;
    public static final int CUR_MP = 0x0B;
    public static final int MAX_MP = 0x0C;

    public static final int P_ATK = 0x10;
    public static final int ATK_SPD = 0x11;
    public static final int P_DEF = 0x12;
    public static final int EVASION = 0x13;
    public static final int ACCURACY = 0x14;
    public static final int CRITICAL = 0x15;
    public static final int M_ATK = 0x16;
    public static final int CAST_SPD = 0x17;
    public static final int M_DEF = 0x18;
    public static final int PVP_FLAG = 0x19;
    public static final int KARMA = 0x1A;

    public static final int CUR_CP = 0x21;
    public static final int MAX_CP = 0x22;

    private int _objectId;
    private int _attackObjectId;
    private boolean _isPlayer;
    private int _isVisible;
    private int _maxHp = -1;
    private List<Attribute> _attributes;

    /**
     * Create  packet for given {@link L2Object}.
     * @param object
     */
    public StatusUpdate(L2Object object)
    {
        _attributes = new ArrayList<>();
        _objectId = object.getObjectId();

        if(object instanceof L2Attackable)
        {
            _maxHp = ((L2Character) object).getMaxVisibleHp();
        }

        if(object instanceof L2PcInstance)
        {
            _isPlayer = true;
        }
    }

    public void addAttribute(int id, int level)
    {
        // TODO: Проверить, действительно ли это тут надо...
//		if(_maxHp != -1)
//		{
//			if(id == MAX_HP)
//			{
//				level = HP_MOD;
//			}
//			else if(id == CUR_HP)
//			{
//				level = (int) (level / (float) _maxHp * HP_MOD);
//			}
//		}
        _attributes.add(new Attribute(id, level));

        if (_isPlayer) {
            switch (id) {
                case CUR_HP:
                case CUR_MP:
                case CUR_CP:
                    setVisibleStats(_objectId);
                    break;
            }
        }
    }

    // Паказывает регенерацию _objectId у objectId2
    private void setVisibleStats(int objectId2)
    {
        _attackObjectId = objectId2;
        _isVisible = 1;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_objectId);
        writeD(_attackObjectId);
        writeC(_isVisible);
        writeC(_attributes.size());

        for(Attribute temp : _attributes)
        {
            writeC(temp.id);
            writeD(temp.value);
        }
    }

    static class Attribute
    {
        public int id;
        public int value;

        Attribute(int pId, int pValue)
        {
            id = pId;
            value = pValue;
        }
    }
}
