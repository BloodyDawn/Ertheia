package dwo.gameserver.taskmanager.tasks;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExWorldChatCnt;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class TaskDailyWorldChatPointReset extends Task
{
    private static final String NAME = "daily_world_chat_reset";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void onTimeElapsed(TaskManager.ExecutedTask task)
    {
        try
        {
            L2PcInstance[] _player = WorldManager.getInstance().getAllPlayersArray();

            for (L2PcInstance p : _player) {
                p.setWorldChatPoints(10);
                p.sendPacket(new ExWorldChatCnt(p));
            }
        }
        catch (Exception e)
        {
            _log.error(getClass().getSimpleName() + ": Could not reset daily world chat points: " + e);
        }
        _log.info("Daily world chat points has been resetted.");
    }

    @Override
    public void initializate()
    {
        TaskManager.addUniqueTask(getName(), TaskTypes.TYPE_GLOBAL_TASK, "1", "6:30:00", "");
    }
}
