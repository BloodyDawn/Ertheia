package dwo.gameserver.handler;

import dwo.gameserver.handler.skills.BalanceLife;
import dwo.gameserver.handler.skills.BeastFeed;
import dwo.gameserver.handler.skills.BeastSkills;
import dwo.gameserver.handler.skills.Blow;
import dwo.gameserver.handler.skills.Cancel;
import dwo.gameserver.handler.skills.ChangeSubClass;
import dwo.gameserver.handler.skills.Charge;
import dwo.gameserver.handler.skills.CombatPointHeal;
import dwo.gameserver.handler.skills.Continuous;
import dwo.gameserver.handler.skills.CpDam;
import dwo.gameserver.handler.skills.CpDamPercent;
import dwo.gameserver.handler.skills.Craft;
import dwo.gameserver.handler.skills.DeluxeKey;
import dwo.gameserver.handler.skills.Detection;
import dwo.gameserver.handler.skills.Disablers;
import dwo.gameserver.handler.skills.DrainSoul;
import dwo.gameserver.handler.skills.Dummy;
import dwo.gameserver.handler.skills.Fishing;
import dwo.gameserver.handler.skills.FishingSkill;
import dwo.gameserver.handler.skills.GetPlayer;
import dwo.gameserver.handler.skills.GiveReco;
import dwo.gameserver.handler.skills.GiveSp;
import dwo.gameserver.handler.skills.GiveVitality;
import dwo.gameserver.handler.skills.Harvest;
import dwo.gameserver.handler.skills.Heal;
import dwo.gameserver.handler.skills.HealPercent;
import dwo.gameserver.handler.skills.InstantJump;
import dwo.gameserver.handler.skills.ManaHeal;
import dwo.gameserver.handler.skills.Manadam;
import dwo.gameserver.handler.skills.Mdam;
import dwo.gameserver.handler.skills.NornilsPower;
import dwo.gameserver.handler.skills.PailakaSpear;
import dwo.gameserver.handler.skills.Pdam;
import dwo.gameserver.handler.skills.RefreshDebuffTime;
import dwo.gameserver.handler.skills.RefuelAirShip;
import dwo.gameserver.handler.skills.ReplaceWithPet;
import dwo.gameserver.handler.skills.Resurrect;
import dwo.gameserver.handler.skills.ShockingBlow;
import dwo.gameserver.handler.skills.Soul;
import dwo.gameserver.handler.skills.Sow;
import dwo.gameserver.handler.skills.Spoil;
import dwo.gameserver.handler.skills.StealBuffs;
import dwo.gameserver.handler.skills.StrSiegeAssault;
import dwo.gameserver.handler.skills.SummonFriend;
import dwo.gameserver.handler.skills.Sweep;
import dwo.gameserver.handler.skills.TakeCastle;
import dwo.gameserver.handler.skills.TakeFort;
import dwo.gameserver.handler.skills.TransferSoul;
import dwo.gameserver.handler.skills.TransformDispel;
import dwo.gameserver.handler.skills.Trap;
import dwo.gameserver.handler.skills.Unlock;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SkillHandler implements IHandler<ISkillHandler, L2SkillType>
{
	protected static Logger _log = LogManager.getLogger(SkillHandler.class);

	private final Map<Integer, ISkillHandler> _handlers;

	private SkillHandler()
	{
		_handlers = new HashMap<>();
		registerHandler(new BalanceLife());
		registerHandler(new BeastFeed());
		registerHandler(new BeastSkills());
		registerHandler(new Blow());
		registerHandler(new Cancel());
		registerHandler(new ShockingBlow());
		registerHandler(new Charge());
		registerHandler(new CombatPointHeal());
		registerHandler(new Continuous());
		registerHandler(new CpDam());
		registerHandler(new CpDamPercent());
		registerHandler(new Craft());
		registerHandler(new DeluxeKey());
		registerHandler(new Detection());
		registerHandler(new Disablers());
		registerHandler(new DrainSoul());
		registerHandler(new Dummy());
		registerHandler(new Fishing());
		registerHandler(new FishingSkill());
		registerHandler(new GetPlayer());
		registerHandler(new GiveReco());
		registerHandler(new GiveSp());
		registerHandler(new GiveVitality());
		registerHandler(new Harvest());
		registerHandler(new Heal());
		registerHandler(new HealPercent());
		registerHandler(new InstantJump());
		registerHandler(new Manadam());
		registerHandler(new ManaHeal());
		registerHandler(new Mdam());
		registerHandler(new NornilsPower());
		registerHandler(new PailakaSpear());
		registerHandler(new Pdam());
		registerHandler(new RefuelAirShip());
		registerHandler(new Resurrect());
		registerHandler(new RefreshDebuffTime());
		registerHandler(new ReplaceWithPet());
		registerHandler(new Soul());
		registerHandler(new Sow());
		registerHandler(new Spoil());
		registerHandler(new StealBuffs());
		registerHandler(new StrSiegeAssault());
		registerHandler(new SummonFriend());
		registerHandler(new Sweep());
		registerHandler(new TakeCastle());
		registerHandler(new TakeFort());
		registerHandler(new TransferSoul());
		registerHandler(new TransformDispel());
		registerHandler(new Trap());
		registerHandler(new Unlock());
		registerHandler(new ChangeSubClass());
		_log.log(Level.INFO, "Loaded " + size() + " Skill Handlers");
	}

	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void registerHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for(L2SkillType t : types)
		{
			_handlers.put(t.ordinal(), handler);
		}
	}

	@Override
	public void removeHandler(ISkillHandler handler)
	{
		synchronized(this)
		{
			L2SkillType[] types = handler.getSkillIds();
			for(L2SkillType t : types)
			{
				_handlers.remove(t.ordinal());
			}
		}
	}

	@Override
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _handlers.get(skillType.ordinal());
	}

	@Override
	public int size()
	{
		return _handlers.size();
	}

	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}
