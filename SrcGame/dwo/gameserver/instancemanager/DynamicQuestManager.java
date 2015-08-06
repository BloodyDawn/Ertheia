/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.instancemanager;

import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;

public class DynamicQuestManager
{
	protected static final Logger _log = LogManager.getLogger(DynamicQuestManager.class);
	// Configs
	private static boolean _gmPointBonus;
	private Map<Integer, DynamicQuest> _activeQuests = new FastMap();

	private DynamicQuestManager()
	{
		_log.log(Level.INFO, "Initializing Dynamic Quest Manager");
		load();
	}

	public static DynamicQuestManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * ID задачи можно получить, зная ID квеста и шага, который передает клиент.
	 * @param questId ID квеста.
	 * @param stepId ID шага.
	 * @return
	 */
	public static int getTaskId(int questId, int stepId)
	{
		return questId * 100 + stepId;
	}

	public static int getStepId(int taskId)
	{
		return taskId % 100;
	}

	public static void setGmPointBonus(boolean value)
	{
		_gmPointBonus = value;
	}

	public static boolean hasGmBonus()
	{
		return _gmPointBonus;
	}

	public void reload()
	{
		_activeQuests.clear();
		load();
	}

	// todo
	private void load()
	{
		/*for (DynamicQuestTemplate template : DynamicQuestsData.getInstance().getAllQuests().values())
		{
			_activeQuests.put(template.getQuestId(), new DynamicQuest(template));
		}*/
	}

	public void addQuest(DynamicQuest quest)
	{
		_activeQuests.put(quest.getTemplate().getTaskId(), quest);
	}

	public DynamicQuest getQuest(int questId, int stepId)
	{
		return _activeQuests.get(getTaskId(questId, stepId));
	}

	public Map<Integer, DynamicQuest> getAllQuests()
	{
		return _activeQuests;
	}

	/**
	 * Инстанс квеста по его ID задания.
	 * @param taskId ID задания.
	 * @return
	 */
	public DynamicQuest getQuestByTaskId(int taskId)
	{
		if(_activeQuests.containsKey(taskId))
		{
			return _activeQuests.get(taskId);
		}

		return null;
	}

	private static class SingletonHolder
	{
		protected static final DynamicQuestManager _instance = new DynamicQuestManager();
	}
}
