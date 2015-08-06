package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Calendar;

public class GraciaSeedsManager
{
	private static final Logger _log = LogManager.getLogger(GraciaSeedsManager.class);
	private static final byte SOATYPE = 3;
	// Seed of Destruction
	private static final byte SODTYPE = 1;
	// Seed of Infinity
	private static final byte SOITYPE = 2;
	public static String qn = "EnergySeeds";
	private final Calendar _SoDLastStateChangeDate;
	private final Calendar _SoILastStateChangeDate;
	private int _SoDTiatKilled;
	private int _SoDState = 1;
	private int _SoIEkimusKilled;
	private int _SoIState = 1;

	private GraciaSeedsManager()
	{
		_log.log(Level.INFO, getClass().getSimpleName() + ": Initializing");
		_SoDLastStateChangeDate = Calendar.getInstance();
		_SoILastStateChangeDate = Calendar.getInstance();
		loadData();
		handleSodStages();
		handleSoIStages();
	}

	public static GraciaSeedsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void saveData(byte seedType)
	{
		switch(seedType)
		{
			case SODTYPE:
				// Seed of Destruction
				GlobalVariablesManager.getInstance().storeVariable("SoDState", String.valueOf(_SoDState));
				GlobalVariablesManager.getInstance().storeVariable("SoDTiatKilled", String.valueOf(_SoDTiatKilled));
				GlobalVariablesManager.getInstance().storeVariable("SoDLSCDate", String.valueOf(_SoDLastStateChangeDate.getTimeInMillis()));
				break;
			case SOITYPE:
				GlobalVariablesManager.getInstance().storeVariable("SoIState", String.valueOf(_SoIState));
				GlobalVariablesManager.getInstance().storeVariable("SoIEkimusKilled", String.valueOf(_SoIEkimusKilled));
				GlobalVariablesManager.getInstance().storeVariable("SoILSCDate", String.valueOf(_SoILastStateChangeDate.getTimeInMillis()));
				break;
			case SOATYPE:
				// Seed of Annihilation
				break;
			default:
				_log.log(Level.WARN, "GraciaSeedManager: Unknown SeedType in SaveData: " + seedType);
				break;
		}
	}

	public void loadData()
	{
		// Seed of Destruction variables
		if(GlobalVariablesManager.getInstance().isVariableStored("SoDState"))
		{
			_SoDState = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDState"));
			_SoDTiatKilled = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDTiatKilled"));
			_SoDLastStateChangeDate.setTimeInMillis(Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("SoDLSCDate")));
		}
		else if(GlobalVariablesManager.getInstance().isVariableStored("SoIState"))
		{
			_SoIState = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoIState"));
			_SoIEkimusKilled = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoIEkimusKilled"));
			_SoILastStateChangeDate.setTimeInMillis(Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("SoILSCDate")));
		}
		else
		{
			// save Initial values
			saveData(SODTYPE);
		}
	}

	private void handleSodStages()
	{
		switch(_SoDState)
		{
			case 1:
				// do nothing, players should kill Tiat a few times
				break;
			case 2:
				// Conquest Complete state, if too much time is passed than change to defense state
				long timePast = System.currentTimeMillis() - _SoDLastStateChangeDate.getTimeInMillis();
				if(timePast >= Config.SOD_STAGE_2_LENGTH)
				// change to Attack state because Defend statet is not implemented
				{
					setSoDState(1, true);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleEffect(() -> {
						setSoDState(1, true);
						Quest esQuest = QuestManager.getInstance().getQuest(qn);
						if(esQuest == null)
						{
							_log.log(Level.WARN, "GraciaSeedManager: missing EnergySeeds Quest!");
						}
						else
						{
							esQuest.notifyEvent("StopSoDAi", null, null);
						}
					}, Config.SOD_STAGE_2_LENGTH - timePast);
				}
				break;
			case 3:
				// not implemented
				setSoDState(1, true);
				break;
			default:
				_log.log(Level.WARN, "GraciaSeedManager: Unknown Seed of Destruction state(" + _SoDState + ")! ");
		}
	}

	private void handleSoIStages()
	{
		switch(_SoIState)
		{
			case 1:
				// do nothing, players should kill Ekimus a few times
				break;
			case 2:
				long timePast = System.currentTimeMillis() - _SoDLastStateChangeDate.getTimeInMillis();
				if(timePast >= Config.SOD_STAGE_2_LENGTH)
				// change to Attack state because Defend state is not implemented
				{
					setSoDState(1, true);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleEffect(() -> {
						setSoIState(1, true);
						Quest esQuest = QuestManager.getInstance().getQuest(qn);
						if(esQuest == null)
						{
							_log.log(Level.INFO, "GraciaSeedManager: missing EnergySeeds Quest!");
						}
						else
						{
							esQuest.notifyEvent("StopSoIAi", null, null);
						}

						esQuest = QuestManager.getInstance().getQuest("SeedOfInfinity");
						if(esQuest == null)
						{
							_log.log(Level.INFO, "GraciaSeedManager: missing SeedOfInfinity Quest!");
						}
						else
						{
							esQuest.notifyEvent("StopSoIAi", null, null);
						}
					}, Config.SOI_STAGE_2_LENGTH - timePast);
				}
				break;
			default:
				_log.log(Level.INFO, "GraciaSeedManager: Unknown Seed of Infinity state(" + _SoIState + ")! ");
		}
	}

	public void increaseSoDTiatKilled()
	{
		if(_SoDState == 1)
		{
			_SoDTiatKilled++;
			if(_SoDTiatKilled >= Config.SOD_TIAT_KILL_COUNT)
			{
				setSoDState(2, false);
			}
			saveData(SODTYPE);
			Quest esQuest = QuestManager.getInstance().getQuest(qn);
			if(esQuest == null)
			{
				_log.log(Level.WARN, "GraciaSeedManager: missing EnergySeeds Quest!");
			}
			else
			{
				esQuest.notifyEvent("StartSoDAi", null, null);
			}
		}
	}

	public void increaseSoIEkimusKilled()
	{
		if(_SoIState == 1)
		{
			_SoIEkimusKilled++;
			if(_SoIEkimusKilled >= Config.SOI_EKIMUS_KILL_COUNT)
			{
				setSoIState(2, false);
			}
			saveData(SOITYPE);
			Quest esQuest = QuestManager.getInstance().getQuest(qn);
			if(esQuest == null)
			{
				_log.log(Level.INFO, "GraciaSeedManager: missing EnergySeeds Quest!");
			}
			else
			{
				esQuest.notifyEvent("StartSoIAi", null, null);
			}
		}
	}

	public int getSoDTiatKilled()
	{
		return _SoDTiatKilled;
	}

	public void setSoDState(int value, boolean doSave)
	{
		_log.log(Level.INFO, "GraciaSeedManager: New Seed of Destruction state -> " + value + '.');
		_SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		_SoDState = value;
		// reset number of Tiat kills
		if(_SoDState == 1)
		{
			_SoDTiatKilled = 0;
		}

		handleSodStages();

		if(doSave)
		{
			saveData(SODTYPE);
		}
	}

	public void setSoIState(int value, boolean doSave)
	{
		_log.log(Level.INFO, "GraciaSeedManager: New Seed of Infinitty state -> " + value + '.');
		_SoILastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		_SoIState = value;
		// reset number of Ekimus kills
		if(_SoIState == 1)
		{
			_SoIEkimusKilled = 0;
		}
		if(doSave)
		{
			saveData(SOITYPE);
		}
	}

	public long getSoDTimeForNextStateChange()
	{
		switch(_SoDState)
		{
			case 1:
				return -1;
			case 2:
				return _SoDLastStateChangeDate.getTimeInMillis() + Config.SOD_STAGE_2_LENGTH - System.currentTimeMillis();
			case 3:
				// not implemented yet
				return -1;
			default:
				// this should not happen!
				return -1;
		}
	}

	public long getSoITimeForNextStateChange()
	{
		switch(_SoIState)
		{
			case 1:
				return -1;
			case 2:
				return _SoILastStateChangeDate.getTimeInMillis() + Config.SOI_STAGE_2_LENGTH - System.currentTimeMillis();
			case 3:
				// not implemented yet
				return -1;
			default:
				// this should not happen!
				return -1;
		}
	}

	public Calendar getSoDLastStateChangeDate()
	{
		return _SoDLastStateChangeDate;
	}

	public Calendar getSoILastStateChangeDate()
	{
		return _SoILastStateChangeDate;
	}

	public int getSoDState()
	{
		return _SoDState;
	}

	public int getSoIState()
	{
		return _SoIState;
	}

	private static class SingletonHolder
	{
		protected static final GraciaSeedsManager _instance = new GraciaSeedsManager();
	}
}