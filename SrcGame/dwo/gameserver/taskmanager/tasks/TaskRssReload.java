package dwo.gameserver.taskmanager.tasks;

import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.model.world.communitybbs.Manager.RssBBSManager;
import dwo.gameserver.taskmanager.Task;
import dwo.gameserver.taskmanager.TaskTypes;
import dwo.gameserver.taskmanager.manager.TaskManager;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.02.12
 * Time: 3:20
 */

public class TaskRssReload extends Task
{
	public static final String NAME = "rss_reload";

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "10000", String.valueOf(ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_RELOAD_DELAY), "");
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		RssBBSManager.getInstance().loadData();
	}
}
