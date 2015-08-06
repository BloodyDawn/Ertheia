package dwo.scripts.ai.individual;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2FriendlyMobInstance;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.world.quest.Quest;
import javolution.util.FastList;

import java.util.List;
import java.util.concurrent.Future;

public class Sakum extends Quest
{
	private static final int SAKUM = 27453;
	private static final int RANGER = 19126;
	private static final int CAPTAIN = 19127;

	private List<L2Npc> _attackers = new FastList(4);
	private Future<?> _attackerCheckTask;
	private boolean _captainsSpawned;

	public Sakum()
	{
		addSpawnId(SAKUM);
	}

	public static void main(String[] args)
	{
		new Sakum();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		for(byte i = 0; i < 4; ++i)
		{
			L2Npc guard = addSpawn(RANGER, npc.getLoc(), 0, true, 0);
			_attackers.add(guard);

			if(guard instanceof L2GuardInstance)
			{
				((L2GuardInstance) guard).attackCharacter(npc);
			}
			else if(guard instanceof L2FriendlyMobInstance)
			{
				((L2FriendlyMobInstance) guard).attackCharacter(npc);
			}
		}

		_attackerCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			if(npc.isDead() || npc.isAlikeDead())
			{
				_attackerCheckTask.cancel(true);
				return;
			}

			for(L2Npc guard : _attackers)
			{
				if(guard != null && !guard.isDead())
				{
					return;
				}
			}

			_attackers.clear();
			int npcId = _captainsSpawned ? RANGER : CAPTAIN;

			for(byte i = 0; i < 4; ++i)
			{
				L2Npc newGuard = addSpawn(npcId, npc.getLoc(), 0, true, 0);

				_attackers.add(newGuard);

				if(newGuard instanceof L2GuardInstance)
				{
					((L2GuardInstance) newGuard).attackCharacter(npc);
				}
				else if(newGuard instanceof L2FriendlyMobInstance)
				{
					((L2FriendlyMobInstance) newGuard).attackCharacter(npc);
				}

				_captainsSpawned = !_captainsSpawned;
			}
		}, 10000, 10000);

		return "";
	}
}
