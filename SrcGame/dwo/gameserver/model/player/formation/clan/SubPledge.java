package dwo.gameserver.model.player.formation.clan;

import dwo.gameserver.model.skills.base.L2Skill;
import javolution.util.FastMap;

import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.02.12
 * Time: 22:28
 */

public class SubPledge
{
	private final int _id;
	private final Map<Integer, L2Skill> _subPledgeSkills = new FastMap<>();
	private String _subPledgeName;
	private int _leaderId;

	public SubPledge(int id, String name, int leaderId)
	{
		_id = id;
		_subPledgeName = name;
		_leaderId = leaderId;
	}

	public int getTypeId()
	{
		return _id;
	}

	public String getName()
	{
		return _subPledgeName;
	}

	public void setName(String name)
	{
		_subPledgeName = name;
	}

	public int getLeaderId()
	{
		return _leaderId;
	}

	public void setLeaderId(int leaderId)
	{
		_leaderId = leaderId;
	}

	public L2Skill addNewSkill(L2Skill skill)
	{
		return _subPledgeSkills.put(skill.getId(), skill);
	}

	public Map<Integer, L2Skill> getSkills()
	{
		return _subPledgeSkills;
	}
}
