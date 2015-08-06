package dwo.gameserver.instancemanager;

import dwo.gameserver.model.world.quest.Quest;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;

public class QuestManager
{
	protected static final Logger _log = LogManager.getLogger(QuestManager.class);
	private Map<String, Quest> _quests = new FastMap<>();

	private QuestManager()
	{
		_log.log(Level.INFO, "QuestManager: Initializing.");
	}

	public static QuestManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/***
	 * Перезагрузка всех квестов
	 */
	public void reloadAllQuests()
	{
		_log.log(Level.INFO, "Reloading ServerMode Scripts");
		// unload all scripts
		_quests.values().stream().filter(quest -> quest != null).forEach(quest -> quest.unload(false));
		// now load all scripts
		ScriptsManager.getInstance().executeCoreScripts();
		getInstance().report();
	}

	public void report()
	{
		_log.log(Level.INFO, "Loaded: " + _quests.size() + " quests");
	}

	public void save()
	{
		for(Quest q : _quests.values())
		{
			q.saveGlobalData();
		}
	}

	public Quest getQuest(String name)
	{
		return _quests.get(name);
	}

	public Quest getQuest(Class<? extends Quest> className)
	{
		return _quests.get(className.getSimpleName());
	}

	public Quest getQuest(int questId)
	{
		for(Quest q : _quests.values())
		{
			if(q.getQuestId() == questId)
			{
				return q;
			}
		}
		return null;
	}

	public void addQuest(Quest newQuest)
	{
		if(newQuest == null)
		{
			throw new IllegalArgumentException("Quest argument cannot be null");
		}
		Quest old = _quests.get(newQuest.getName());

		// FIXME: unloading the old quest at this point is a tad too late.
		// the new quest has already initialized itself and read the data, starting
		// an unpredictable number of tasks with that data.  The old quest will now
		// save data which will never be read.
		// However, requesting the newQuest to re-read the data is not necessarily a
		// good option, since the newQuest may have already started timers, spawned NPCs
		// or taken any other action which it might re-take by re-reading the data.
		// the current solution properly closes the running tasks of the old quest but
		// ignores the data; perhaps the least of all evils...
		if(old != null)
		{
			old.unload();
			_log.log(Level.INFO, "Replaced: (" + old.getName() + ") with a new version (" + newQuest.getName() + ')');

		}
		_quests.put(newQuest.getName(), newQuest);
	}

	public boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}

	private static class SingletonHolder
	{
		protected static final QuestManager _instance = new QuestManager();
	}
}