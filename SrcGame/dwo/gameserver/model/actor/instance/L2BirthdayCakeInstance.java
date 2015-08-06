package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;

import java.util.concurrent.ScheduledFuture;

public class L2BirthdayCakeInstance extends L2Npc
{
	private static final int BIRTHDAY_CAKE_24 = 106;
	private static final int BIRTHDAY_CAKE = 139;
	private static L2Skill _skill;
	private final ScheduledFuture<?> _aiTask;
	private int _masterId;

	public L2BirthdayCakeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		if(template.getNpcId() == BIRTHDAY_CAKE_24)
		{
			_skill = SkillTable.getInstance().getInfo(22035, 1);
		}
		else if(template.getNpcId() == BIRTHDAY_CAKE)
		{
			_skill = SkillTable.getInstance().getInfo(22250, 1);
		}

		_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BuffTask(this), 1000, 1000);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean onDelete()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		return super.onDelete();
	}

	private class BuffTask implements Runnable
	{
		private final L2BirthdayCakeInstance _cake;

		protected BuffTask(L2BirthdayCakeInstance cake)
		{
			_cake = cake;
		}

		@Override
		public void run()
		{
			if(!isInsideZone(ZONE_PEACE))
			{
				if(_cake.getNpcId() == BIRTHDAY_CAKE_24)
				{
					for(L2PcInstance player : _cake.getKnownList().getKnownPlayersInRadius(_skill.getSkillRadius()))
					{
						_skill.getEffects(_cake, player);
					}
				}
				else if(_cake.getNpcId() == BIRTHDAY_CAKE)
				{
					L2PcInstance player = (L2PcInstance) _cake.getOwner();
					if(player == null)
					{
						return;
					}

					L2Party party = player.getParty();
					if(party == null)
					{
						if(player.isInsideRadius(_cake, _skill.getSkillRadius(), true, true))
						{
							_skill.getEffects(_cake, player);
						}
					}
					else
					{
						party.getMembers().stream().filter(member -> member != null && member.isInsideRadius(_cake, _skill.getSkillRadius(), true, true)).forEach(member -> _skill.getEffects(_cake, member));
					}
				}
			}
		}
	}
}
