package dwo.gameserver.network.game.serverpackets.packet.enchant.skill;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

public class ExEnchantSkillList extends L2GameServerPacket
{
	private final EnchantSkillType _type;
	private final List<Skill> _skills;

	public ExEnchantSkillList(EnchantSkillType type)
	{
		_type = type;
		_skills = new FastList<>();
	}

	public void addSkill(int id, int level)
	{
		_skills.add(new Skill(id, level));
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type.ordinal());
		writeD(_skills.size());
		for(Skill sk : _skills)
		{
			writeD(sk.id);
			writeD(sk.nextLevel);
		}
	}

	public enum EnchantSkillType
	{
		NORMAL,
		SAFE,
		UNTRAIN,
		CHANGE_ROUTE,
	}

	static class Skill
	{
		public int id;
		public int nextLevel;

		Skill(int pId, int pNextLevel)
		{
			id = pId;
			nextLevel = pNextLevel;
		}
	}
}