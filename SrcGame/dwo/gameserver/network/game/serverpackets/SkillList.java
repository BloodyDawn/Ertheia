package dwo.gameserver.network.game.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class SkillList extends L2GameServerPacket
{
	private final List<Skill> _skills;
	private int _newSkillId;

	public SkillList()
	{
		_skills = new ArrayList<>();
	}

	public void addSkill(int id, int level, boolean passive, boolean disabled, boolean enchanted, int replaceable)
	{
		_skills.add(new Skill(id, level, passive, disabled, enchanted, replaceable));
	}

	public void setNewSkillId(int id)
	{
		_newSkillId = id;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_skills.size());
		for(Skill temp : _skills)
		{
			writeD(temp.passive ? 1 : 0);
			writeD(temp.level);
			writeD(temp.id);
			writeD(temp.replaceable);
			writeC(temp.disabled ? 1 : 0);
			writeC(temp.enchanted ? 1 : 0);
		}

		writeD(_newSkillId); // Изучаемый скилл. Для моргания вкладки (активные, пассивные).
	}

	static class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;
		public int replaceable;

		Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted, int pReplaceable)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisabled;
			enchanted = pEnchanted;
			replaceable = pReplaceable;
		}
	}
}
