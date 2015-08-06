package dwo.scripts.ai.group_template;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: Bacek, ANZO
 * Date: 25.07.11
 * Time: 11:55
 */

public class FairyWispes extends Quest
{
	private static final int[] Wispes = {32915, 32916};
	private static final SkillHolder healSkill = new SkillHolder(14064, 1);
	private static final SkillHolder healSkillBig = new SkillHolder(14065, 1);

	public FairyWispes(int id, String name, String descr)
	{
		super(id, name, descr);
		addSpawnId(Wispes);
		addEventId(HookType.ON_SEE_PLAYER);
	}

	public static void main(String[] args)
	{
		new FairyWispes(-1, "FairyWispes", "ai");
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		npc.setIsNoAttackingBack(true);
		npc.getKnownList().setDistanceToWatch(300); // TODO: Unhardcode
		npc.startWatcherTask(300);
		return super.onSpawn(npc);
	}

	@Override
	public void onSeePlayer(L2Npc watcher, L2PcInstance player)
	{
		if(watcher == null || watcher.isDead() || player == null)
		{
			return;
		}
		if(!player.isSpawnProtected() && player.getFirstEffect(12001) == null // Скилл, который вешает Кристалл по 10301 квесту - дает возможность убить духа
			&& !watcher.isCastingNow() && !watcher.isCastingSimultaneouslyNow())
		{
			switch(watcher.getNpcId())
			{
				case 32915:
					ThreadPoolManager.getInstance().scheduleAi(new Cast(healSkill, watcher, player), 100);
					break;
				case 32916:
					ThreadPoolManager.getInstance().scheduleAi(new Cast(healSkillBig, watcher, player), 100);
					break;
			}
		}
	}

	private static class Cast implements Runnable
	{
		SkillHolder _skill;
		L2Character _npc;
		L2PcInstance _player;

		public Cast(SkillHolder skill, L2Character npc, L2PcInstance player)
		{
			_skill = skill;
			_npc = npc;
			_player = player;
		}

		@Override
		public void run()
		{
			if(Util.checkIfInRange(300, _npc, _player, true) && _player != null && !_player.isDead() && !_player.isAlikeDead())
			{
				if(_npc != null && !_npc.isDead() && !_npc.isCastingNow())
				{
					_npc.setTarget(_player);
					_npc.setIsCastingNow(true);
					_npc.doCast(_skill.getSkill());
				}
			}
			else
			{
				_npc.setIsCastingNow(false);
			}
		}
	}
}