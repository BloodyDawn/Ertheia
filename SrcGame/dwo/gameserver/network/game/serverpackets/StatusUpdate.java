package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.instancemanager.WorldManager;
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
    public static final int LEVEL = 1;
    public static final int EXP = 2;
    public static final int STR = 3;
    public static final int DEX = 4;
    public static final int CON = 5;
    public static final int INT = 6;
    public static final int WIT = 7;
    public static final int MEN = 8;
    public static final int CUR_HP = 9;
    public static final int MAX_HP = 10;
    public static final int CUR_MP = 11;
    public static final int MAX_MP = 12;
    public static final int SP = 13;
    public static final int CUR_LOAD = 14;
    public static final int MAX_LOAD = 15;
    public static final int P_ATK = 17;
    public static final int ATK_SPD = 18;
    public static final int P_DEF = 19;
    public static final int EVASION = 20;
    public static final int ACCURACY = 21;
    public static final int CRITICAL = 22;
    public static final int M_ATK = 23;
    public static final int CAST_SPD = 24;
    public static final int M_DEF = 25;
    public static final int PVP_FLAG = 26;
    public static final int REPUTATION = 27;
    public static final int CUR_CP = 33;
    public static final int MAX_CP = 34;
    private static final int HP_MOD = 10000000;
    private int _objectId;
    private int _attackObjectId;
    private boolean _isPlayer;
    private int _isVisible;
    private int _maxHp = -1;
    private List<Attribute> _attributes;

    /**
     * If you have access to object itself use {@link StatusUpdate#StatusUpdate(L2Object)}.
     * @param objectId
     */
    public StatusUpdate(int objectId)
    {
        _attributes = new ArrayList<>();
        _objectId = objectId;
        L2Object obj = WorldManager.getInstance().findObject(objectId);

        if(obj != null)
        {
            if(obj instanceof L2Attackable)
            {
                _maxHp = ((L2Character) obj).getMaxVisibleHp();
            }
            if(obj instanceof L2PcInstance)
            {
                _isPlayer = true;
            }
        }
    }

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

//        if(_isPlayer)
//        {
//            switch(id)
//            {
//                case CUR_HP:
//                case CUR_MP:
//                case CUR_CP:
//                    setVisibleStats(_objectId);
//                    break;
//            }
//        }
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
        writeD(0x00); // Tauti ->  targetObjectId  ?? // если тут приходит ID атакующего то  на npc\pc будут отображаться параметры типа сколько у него хп\ сколько регенит хп, сколько дамаги нанем и т.д. В зависимости от атрибутов приходящих далее. Нужно разобраться когда и при каких условиях приходят данные.

        // Судя по дампу работает только для CUR_HP CUR_MP CUR_CP   0 1 4
        writeC(0x00); // Опция показывать на экране всплывающие надписи при потере или получении любого из атрибута CP, HP, MP и др... 0 -  не показывать, 1 - показывать.
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
