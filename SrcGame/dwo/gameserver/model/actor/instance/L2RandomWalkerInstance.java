package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;

import java.util.concurrent.Future;

public class L2RandomWalkerInstance extends L2Attackable
{
	protected Future<?> _randomWalkTask;
	int randomX;
	int randomY;
	int spawnX;
	int spawnY;

	public L2RandomWalkerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsNoAttackingBack(true);
		setAutoAttackable(true);
		_randomWalkTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RandomWalkTask(), 100, Rnd.get(3000, 7000));
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		spawnX = getX();
		spawnY = getY();
	}

	public class RandomWalkTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInActiveRegion() || isDead())
			{
				return;
			}
			randomX = spawnX + Rnd.get(2 * 80) - 50;
			randomY = spawnY + Rnd.get(2 * 80) - 50;
			setRunning();
			if(randomX != getX() && randomY != getY())
			{
				Location destiny = GeoEngine.getInstance().moveCheck(getX(), getY(), getZ(), randomX, randomY, getZ(), getInstanceId());
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, destiny, 0);
			}
		}
	}
}