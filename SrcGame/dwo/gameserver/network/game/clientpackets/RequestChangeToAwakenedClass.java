package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.AwakeningManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO, Bacek, Keiichi
 * Date: 05.11.11
 * Time: 14:23
 */

public class RequestChangeToAwakenedClass extends L2GameClientPacket
{
	private int _ok;

	@Override
	protected void readImpl()
	{
		_ok = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player != null && _ok == 1)
		{
			if(!player.isAwakened() && player.getVariablesController().get("skillsDeleted-" + player.getClassIndex(), Boolean.class, false))
			{
				if(player.getLevel() >= 85 && (!player.isSubClassActive() || player.isSubClassActive() && player.getSubclass().isDualClass()) && player.getClassId().level() == ClassLevel.THIRD.ordinal())
				{
					int classId = player.getClassId().getId();
					if(player.isSubClassActive())
					{
						classId = player.getSubclass().getClassId();
					}

					int classToAwakening = Util.getAwakenRelativeClass(classId);

					if(classToAwakening < 0)
					{
						_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while getAwakenRelativeClass()! Player: [" + player.getName() + "] trying to awake from ID: [" + classId + "] SubclassIndex: [" + (player.isSubClassActive() ? player.getSubclass().getClassIndex() + "]" : "no subclass]"));
						return;
					}

					player.setClassId(classToAwakening);

					if(player.isSubClassActive())
					{
						player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClassId());
					}
					else
					{
						player.setBaseClassId(player.getActiveClassId());
					}

					AwakeningManager.getInstance().doAwake(player);

					//Удаляем переменную
					player.getVariablesController().unset("skillsDeleted-" + player.getClassIndex());
				}
				else
				{
					_log.log(Level.WARN, "Player " + player.getName() + " trying cheating with Awakening!");
				}
			}
			else
			{
				_log.log(Level.WARN, "Player " + player.getName() + " trying cheating with Awakening!");
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:A5 RequestChangeToAwakenedClass";
	}
}