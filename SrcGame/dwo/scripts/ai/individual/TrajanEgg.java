package dwo.scripts.ai.individual;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.10.11
 * Time: 13:22
 */
// TODO: onSee эвент слишком медленный, надо сделать кастом-таск
public class TrajanEgg extends Quest
{
	// Яйцо траджана
	public static final int TRAJAN_EGG = 18997;
	public static final int TRAJAN_RAID_EGG = 19023;

	// Вылупляющиеся из яиц монстры
	public static final int[] EGG_MONSTERS = {18993, 18994, 18995};

	public TrajanEgg()
	{
		addSpawnId(TRAJAN_EGG);
		addEventId(HookType.ON_SEE_PLAYER);
	}

	public static void main(String[] args)
	{
		new TrajanEgg();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == TRAJAN_EGG)
		{
			npc.setIsOverloaded(true);
			npc.setIsNoAttackingBack(true);
			npc.getKnownList().setDistanceToWatch(200); // TODO: Unhardcode
			npc.startWatcherTask(200);
		}
		return super.onSpawn(npc);
	}

	@Override
	public void onSeePlayer(L2Npc watcher, L2PcInstance player)
	{
		if(watcher == null || watcher.isDead() || player == null)
		{
			return;
		}

		if(watcher.getNpcId() == TRAJAN_EGG && watcher.getDisplayEffect() == 0)
		{
			watcher.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			watcher.setDisplayEffect(2);
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnMonster(watcher, player), 15000);
		}
	}

	private class SpawnMonster implements Runnable
	{
		L2Npc _npc;
		L2PcInstance _player;

		public SpawnMonster(L2Npc npc, L2PcInstance player)
		{
			_npc = npc;
			_player = player;
		}

		@Override
		public void run()
		{
			if(_npc != null && !_npc.isDead())
			{
				if(_player != null)
				{
					_npc.getLocationController().decay();
					L2Attackable monster = (L2Attackable) addSpawn(EGG_MONSTERS[Rnd.get(EGG_MONSTERS.length)], _npc.getX(), _npc.getY(), _npc.getZ(), 0, false, 0, false, _npc.getInstanceId());
                    if (monster != null) {
                        monster.getAttackable().attackCharacter(_player);
                    }
				}
				else
				{
					_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					_npc.setDisplayEffect(4);
				}
			}
		}
	}
}