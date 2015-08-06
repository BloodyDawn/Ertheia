package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

/**
 * TODO: Доразобрать unk параметры
 * 0002 d  isDoubleCasting: 0 (0x00000000)
 * 0006 d  charID: 1222661449 (0x48E05549)
 * 000A d  targetID: 1222661449 (0x48E05549)
 * 000E c  unk: 0 (0x00)
 * 000F d  skillID: Огонь по Площади ID:10781 (0x2A1D)
 * 0013 d  skillLvl: 1 (0x00000001)
 * 0017 d  hitTime: 2200 (0x00000898)
 * 001B d  replaceableSkill: -1 (0xFFFFFFFF)
 * 001F d  reuseDelay: 600000 (0x000927C0)
 * 0023 d  X: -117149 (0xFFFE3663)
 * 0027 d  Y: 248402 (0x0003CA52)
 * 002B d  Z: -1984 (0xFFFFF840)
 * ------ Обычно == 0, но значение ниже приходит при юзе TARGET_GROUND Скиллов (м.б. heading)
 * 002F d  unk2: 65536 (0x00010000)
 * ---------------------------------------------------------------------------
 * 0033 d  tx: -117546 (0xFFFE34D6)
 * 0037 d  ty: 248257 (0x0003C9C1)
 * 003B d  tz: -2032 (0xFFFFF810)
 * ------ Хвост, если TARGET_GROUND(проверить)--------------------------------
 * 003F d  X2: -117149 (0xFFFE3663)
 * 0043 d  Y2: 248402 (0x0003CA52)
 * 0047 d  Z2: -1984 (0xFFFFF840)
 */

public class MagicSkillUse extends L2GameServerPacket
{
	L2Object[] _skillz;
	private int _targetId;
	private int _tx;
	private int _ty;
	private int _tz;
	private int _skillId;
	private int _skillLevel;
	private int _hitTime;
	private int _reuseDelay;
	private int _charObjId;
	private int _x;
	private int _y;
	private int _z;
	private int _actionTag;
	private int _actionId;
	private int _replaceable;
	private boolean _isDoubleCasting;
	private boolean _isGroundSkill;

	public MagicSkillUse(L2Character cha, L2Character target, L2Skill skill)
	{
		if(skill.gettActionId() > 0)
		{
			_actionTag = 1;
			_actionId = skill.gettActionId();
		}
		_charObjId = cha.getObjectId();
		_replaceable = skill.getReplaceableSkillId();
		_targetId = target.getObjectId();
		_skillz = skill.getTargetList(cha);
		_skillId = skill.getId();
		_skillLevel = skill.getLevel();
		_hitTime = skill.getHitTime();
		_reuseDelay = skill.getReuseDelay();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		_isDoubleCasting = cha.isDoubleCastingNow();
		_isGroundSkill = skill.getTargetType() == L2TargetType.TARGET_GROUND;
	}

	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if(skill.gettActionId() > 0)
		{
			_actionTag = 1;
			_actionId = skill.gettActionId();
		}
		_charObjId = cha.getObjectId();
		_replaceable = skill.getReplaceableSkillId();
		_targetId = target.getObjectId();
		_skillz = skill.getTargetList(cha);
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		_isDoubleCasting = cha.isDoubleCastingNow();
		_isGroundSkill = skill.getTargetType() == L2TargetType.TARGET_GROUND;
	}

	public MagicSkillUse(L2Character cha, SkillHolder skillHolder, int hitTime, int reuseDelay)
	{
		L2Skill skill = skillHolder.getSkill();
		if(skill.gettActionId() > 0)
		{
			_actionTag = 1;
			_actionId = skill.gettActionId();
		}
		_charObjId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillz = skill.getTargetList(cha);
		_skillId = skill.getId();
		_replaceable = skill.getReplaceableSkillId();
		_skillLevel = skill.getLevel();
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = cha.getX();
		_ty = cha.getY();
		_tz = cha.getZ();
		_isDoubleCasting = cha.isDoubleCastingNow();
		_isGroundSkill = skill.getTargetType() == L2TargetType.TARGET_GROUND;
	}

	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if(skill.gettActionId() > 0)
		{
			_actionTag = 1;
			_actionId = skill.gettActionId();
		}
		_charObjId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillz = skill.getTargetList(cha);
		_skillId = skillId;
		_replaceable = skill.getReplaceableSkillId();
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = cha.getX();
		_ty = cha.getY();
		_tz = cha.getZ();
		_isDoubleCasting = cha.isDoubleCastingNow();
		_isGroundSkill = skill.getTargetType() == L2TargetType.TARGET_GROUND;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_isDoubleCasting ? 0x01 : 0x00); // тип SetupGauge(0 первый скил, 1 следующий скил)
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_replaceable);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeD(0x00);

        if(_isGroundSkill)
        {
            writeD(_x);
            writeD(_y);
            writeD(_z);
        }

		writeD(_tx);
		writeD(_ty);
		writeD(_tz);

		writeD(_actionTag);  // tag
		writeD(_actionId);  // actionId
	}
}