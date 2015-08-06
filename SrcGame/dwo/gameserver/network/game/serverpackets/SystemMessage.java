package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.network.game.components.SystemMessageId;

import java.util.Arrays;

public class SystemMessage extends L2GameServerPacket
{
    private static final SMParam[] EMPTY_PARAM_ARRAY = new SMParam[0];
    private static final byte TYPE_SHOW_WINDOW_DAMAGE = 16;
    private static final byte TYPE_CLASS_ID = 15;
    private static final byte TYPE_SYSTEM_STRING = 13;
    private static final byte TYPE_PLAYER_NAME = 12;
    // id 11 - unknown
    private static final byte TYPE_INSTANCE_NAME = 10;
    private static final byte TYPE_ELEMENT_NAME = 9;
    // id 8 - same as 3
    private static final byte TYPE_ZONE_NAME = 7;
    private static final byte TYPE_ITEM_NUMBER = 6;
    private static final byte TYPE_CASTLE_NAME = 5;
    private static final byte TYPE_SKILL_NAME = 4;
    private static final byte TYPE_ITEM_NAME = 3;
    private static final byte TYPE_NPC_NAME = 2;
    private static final byte TYPE_NUMBER = 1;
    private static final byte TYPE_TEXT = 0;
    private static int _objectId;
    private static int _targetObjId;
    private final SystemMessageId _smId;
    private SMParam[] _params;
    private int _paramIndex;

    private SystemMessage(SystemMessageId smId)
    {
        int paramCount = smId.getParamCount();
        _smId = smId;
        _params = paramCount != 0 ? new SMParam[paramCount] : EMPTY_PARAM_ARRAY;
    }

    public static SystemMessage sendString(String text)
    {
        if(text == null)
        {
            throw new NullPointerException();
        }

        SystemMessage sm = getSystemMessage(SystemMessageId.S1);
        sm.addString(text);
        return sm;
    }

    public static SystemMessage getSystemMessage(SystemMessageId smId)
    {
        SystemMessage sm = smId.getStaticSystemMessage();
        if(sm != null)
        {
            return sm;
        }

        sm = new SystemMessage(smId);
        if(smId.getParamCount() == 0)
        {
            smId.setStaticSystemMessage(sm);
        }

        return sm;
    }

    /**
     * Рекомендуется использовать {@link #getSystemMessage(SystemMessageId)} там, где это возможно
     * @param id ID системного сообщения
     * @return SystemMessage с указанным ID
     */
    public static SystemMessage getSystemMessage(int id)
    {
        return getSystemMessage(SystemMessageId.getSystemMessageId(id));
    }

    private void append(SMParam param)
    {
        // В некоторых новых Sysmessages приходят дополнительные данные
        // такие как objId targetObjId и т.п. Временно убираем проверку,
        // возможно больше ине понадобится
        if(_paramIndex >= _params.length)
        {
            _params = Arrays.copyOf(_params, _paramIndex + 1);
            _smId.setParamCount(_paramIndex + 1);
        }

        _params[_paramIndex++] = param;
    }

    public SystemMessage addString(String text)
    {
        append(new SMParam(TYPE_TEXT, text, null, null));
        return this;
    }

    /**
     * 0-9 Castle names
     * 21-64 CH names
     * 81-89 Territory names
     * 101-121 Fortress names
     * @param number ID резиденции
     * @return имя резиденции из Castlename-e.dat
     */
    public SystemMessage addCastleId(int number)
    {
        append(new SMParam(TYPE_CASTLE_NAME, number, null, null));
        return this;
    }

    public SystemMessage addNumber(int number)
    {
        append(new SMParam(TYPE_NUMBER, number, null, null));
        return this;
    }

    public SystemMessage addShowWindowDamage(L2Character cha, int targetObjId, int dmg)
    {
        _objectId = cha.getObjectId();
        _targetObjId = targetObjId;

		/* Для отображения дамага по цели, значение dmg должно приходить отрицательное! */
        append(new SMParam(TYPE_SHOW_WINDOW_DAMAGE, _targetObjId, _objectId, -dmg));
        return this;
    }

    public SystemMessage addItemNumber(long number)
    {
        append(new SMParam(TYPE_ITEM_NUMBER, number, null, null));
        return this;
    }

    public SystemMessage addCharName(L2Character cha)
    {
        if(cha instanceof L2Npc)
        {
            return ((L2Npc) cha).getTemplate().isServerSideName() ? addString(((L2Npc) cha).getTemplate().getName()) : addNpcName((L2Npc) cha);
        }
        if(cha instanceof L2PcInstance)
        {
            return addPcName((L2PcInstance) cha);
        }
        if(cha instanceof L2Summon)
        {
            return ((L2Summon) cha).getTemplate().isServerSideName() ? addString(((L2Summon) cha).getTemplate().getName()) : addNpcName((L2Summon) cha);
        }
        return addString(cha.getName());
    }

    public SystemMessage addPcName(L2PcInstance pc)
    {
        append(new SMParam(TYPE_PLAYER_NAME, pc.getAppearance().getVisibleName(), null, null));
        return this;
    }

    public SystemMessage addNpcName(L2Npc npc)
    {
        return addNpcName(npc.getTemplate());
    }

    public SystemMessage addNpcName(L2Summon npc)
    {
        return addNpcName(npc.getNpcId());
    }

    public SystemMessage addNpcName(L2NpcTemplate template)
    {
        if(template.isCustom())
        {
            return addString(template.getName());
        }
        return addNpcName(template.getNpcId());
    }

    public SystemMessage addNpcName(int id)
    {
        append(new SMParam(TYPE_NPC_NAME, 1000000 + id, null, null));
        return this;
    }

    public SystemMessage addItemName(L2ItemInstance item)
    {
        return addItemName(item.getItem().getItemId());
    }

    public SystemMessage addItemName(L2Item item)
    {
        return addItemName(item.getItemId());
    }

    public SystemMessage addItemName(int id)
    {
        append(new SMParam(TYPE_ITEM_NAME, id, null, null));
        return this;
    }

    public SystemMessage addZoneName(int x, int y, int z)
    {
        append(new SMParam(TYPE_ZONE_NAME, new int[]{x, y, z}, null, null));
        return this;
    }

    public SystemMessage addSkillName(L2Effect effect)
    {
        return addSkillName(effect.getSkill());
    }

    public SystemMessage addSkillName(L2Skill skill)
    {
        if(skill.getId() != skill.getDisplayId()) // custom skill - need nameId or smth like this.
        {
            return addString(skill.getName());
        }
        return addSkillName(skill.getId(), skill.getLevel());
    }

    public SystemMessage addSkillName(int id)
    {
        return addSkillName(id, 1);
    }

    public SystemMessage addSkillName(int id, int lvl)
    {
        append(new SMParam(TYPE_SKILL_NAME, new int[]{id, lvl}, null, null));
        return this;
    }

    /**
     * @param type
     * @return Elemental name - 0(Fire) ...
     */
    public SystemMessage addElemental(int type)
    {
        append(new SMParam(TYPE_ELEMENT_NAME, type, null, null));
        return this;
    }

    /**
     * @param type
     * @return ID from sysstring-e.dat
     */
    public SystemMessage addSystemString(int type)
    {
        append(new SMParam(TYPE_SYSTEM_STRING, type, null, null));
        return this;
    }

    /**
     * @param type
     * @return ID from ClassInfo-e.dat
     */
    public SystemMessage addClassId(int type)
    {
        append(new SMParam(TYPE_CLASS_ID, type, null, null));
        return this;
    }

    /**
     * @param type id of instance
     * @return Instance name from instantzonedata-e.dat
     */
    public SystemMessage addInstanceName(int type)
    {
        append(new SMParam(TYPE_INSTANCE_NAME, type, null, null));
        return this;
    }

    public static SystemMessage removeItems(final int itemId, final long count) {
        if (count > 1) {
            return new SystemMessage(SystemMessageId.S1_S2_DISAPPEARED).addItemName(itemId).addItemNumber(count);
        }
        return new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(itemId);
    }
    
    public SystemMessageId getSystemMessageId()
    {
        return _smId;
    }

    @Override
    protected final void writeImpl()
    {
        writeH(_smId.getId());
        writeC(_paramIndex);

        SMParam param;
        for(int i = 0; i < _paramIndex; i++)
        {
            param = _params[i];
            writeC(param.getType());

            switch(param.getType())
            {
                case TYPE_TEXT:
                case TYPE_PLAYER_NAME:
                {
                    writeS(param.getStringValue());
                    break;
                }

                case TYPE_ITEM_NUMBER:
                {
                    writeQ(param.getLongValue());
                    break;
                }

                case TYPE_ITEM_NAME:
                case TYPE_CASTLE_NAME:
                case TYPE_NUMBER:
                case TYPE_NPC_NAME:
                case TYPE_ELEMENT_NAME:
                case TYPE_SYSTEM_STRING:
                case TYPE_INSTANCE_NAME:
                case TYPE_CLASS_ID:
                {
                    writeD(param.getIntValue());
                    break;
                }

                case TYPE_SKILL_NAME:
                {
                    final int[] array = param.getIntArrayValue();
                    writeD(array[0]); // SkillId
                    writeH(array[1]); // SkillLevel
                    break;
                }

                case TYPE_ZONE_NAME:
                {
                    final int[] array = param.getIntArrayValue();
                    writeD(array[0]); // x
                    writeD(array[1]); // y
                    writeD(array[2]); // z
                    break;
                }

                case TYPE_SHOW_WINDOW_DAMAGE:
                {
                    writeD(param.getTargetObjId());
                    writeD(param.getObjId());
                    writeD(param.getDmg());
                    break;
                }
            }
        }
    }

    private static class SMParam
    {
        private final byte _type;
        private final Object _value;
        private final Object _twovalue;
        private final Object _threevalue;

        public SMParam(byte type, Object value, Object twovalue, Object threevalue)
        {
            _type = type;
            _value = value;
            _twovalue = twovalue;
            _threevalue = threevalue;
        }

        public byte getType()
        {
            return _type;
        }

        public Object getValue()
        {
            return _value;
        }

        public int getTargetObjId()
        {
            return (Integer) _value;
        }

        public int getObjId()
        {
            return (Integer) _twovalue;
        }

        public int getDmg()
        {
            return (Integer) _threevalue;
        }

        public String getStringValue()
        {
            return (String) _value;
        }

        public int getIntValue()
        {
            return (Integer) _value;
        }

        public long getLongValue()
        {
            return (Long) _value;
        }

        public int[] getIntArrayValue()
        {
            return (int[]) _value;
        }
    }
}
