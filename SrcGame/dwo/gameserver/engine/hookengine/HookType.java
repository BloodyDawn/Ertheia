package dwo.gameserver.engine.hookengine;

import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

/**
 * Adding a new hook:
 * 1) add new enum to {@link HookType}
 * 2) add new method to {@link IHook}, don't forget to add the {@link HookEnumType} annotation!
 * 3) implement method in {@link Quest} and {@link AbstractHookImpl} + other classes that implements this interface
 * 4) Place {@link HookManager}'s notifyEvent() or checkEvent() on desired hook place(s)
 * 5) ???
 * 6) profit!
 *
 * Please don't forget that adding new enum value here is NOT enough. It will fuck up during runtime.
 *
 * !To register new hook, check {@link HookManager}!
 */
public enum HookType
{
	ON_ATTACK,
	ON_HP_CHANGED,
	ON_EFFECT_START,
	ON_EFFECT_STOP,
	ON_CHAR_CREATE,
	ON_CHAR_DELETE,
	ON_DELETEME,   // Выполняется при выходе / вылете из игры
	ON_DIE,
	ON_DISCONNECT,
	ON_DLGANSWER,
	ON_ENCHANT_FINISH,
	ON_ENTER_WORLD,
	ON_ENTER_ZONE,
	ON_EVENT_FINISHED,
	ON_EXIT_ZONE,
	ON_FISH_DIE,
	ON_FORBIDDEN_ACTION, //TODO
	ON_IS_INVUL_CHECK,
	ON_IS_IN_EVENT_CHECK,
	ON_LEVEL_INCREASE,
	ON_REVIVE,//TODO:
	ON_REWARD_SKILLS,
	ON_PARTY_LEAVE,
	ON_POTION_USE, //TODO
	ON_SEE_PLAYER,
	ON_SKILL_ADD, //TODO
	ON_SKILL_REMOVE,
	ON_SKILL_USE,
	ON_SPAWN,
	ON_DAYNIGHT_CHANGE,
	ON_BOTTRACKER_WARNING,
	ON_QUEST_FINISH,
	ON_SUMMON_SPAWN,
	ON_SUMMON_DIE,
	ON_SUMMON_ATTACKED,
	ON_SUMMON_ACTION,
	ON_ITEM_CRAFTED,
	ON_INVENTORY_ADD,
	ON_INVENTORY_CHANGE,
	ON_INVENTORY_DELETE,
	ON_OLY_BATTLE_END,
	ON_CHAOS_BATTLE_END,
	ON_SIEGE_START,
	ON_SIEGE_END,
	ON_ITEM_PICKUP,
	ON_ENTER_INSTANCE;
	public static final HookType[] EMPTY_ARRAY = new HookType[0];
	private final Logger _log = LogManager.getLogger(HookType.class);
	private Method invokingMethod;

	private boolean inited;

	private HookType()
	{
	}

	private void init()
	{
		try
		{
			Method[] mh = IHook.class.getMethods();

			for(Method method : mh)
			{
				if(!method.isAnnotationPresent(HookEnumType.class))
				{
					continue;
				}

				HookEnumType annot = method.getAnnotation(HookEnumType.class);

				if(annot.value() == this)
				{
					invokingMethod = method;
					return;
				}
			}

			throw new Exception("No method found for HookType." + this + " enum in IHook.java! Are you missing annotations?");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	public Method getInvokingMethod()
	{
		//TODO: Handle this better
		if(!inited)
		{
			inited = true;
			init();
		}

		return invokingMethod;
	}
}