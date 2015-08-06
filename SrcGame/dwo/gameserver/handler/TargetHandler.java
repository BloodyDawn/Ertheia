package dwo.gameserver.handler;

import dwo.gameserver.handler.targets.TargetAlly;
import dwo.gameserver.handler.targets.TargetArea;
import dwo.gameserver.handler.targets.TargetAreaCorpseMob;
import dwo.gameserver.handler.targets.TargetAreaSummon;
import dwo.gameserver.handler.targets.TargetAura;
import dwo.gameserver.handler.targets.TargetAuraCorpseMob;
import dwo.gameserver.handler.targets.TargetBehindArea;
import dwo.gameserver.handler.targets.TargetBehindAura;
import dwo.gameserver.handler.targets.TargetChain;
import dwo.gameserver.handler.targets.TargetClan;
import dwo.gameserver.handler.targets.TargetClanMember;
import dwo.gameserver.handler.targets.TargetCommandChannel;
import dwo.gameserver.handler.targets.TargetCorpseAlly;
import dwo.gameserver.handler.targets.TargetCorpseClan;
import dwo.gameserver.handler.targets.TargetCorpseCommandChannel;
import dwo.gameserver.handler.targets.TargetCorpseMob;
import dwo.gameserver.handler.targets.TargetCorpseParty;
import dwo.gameserver.handler.targets.TargetCorpsePartyMember;
import dwo.gameserver.handler.targets.TargetCorpsePet;
import dwo.gameserver.handler.targets.TargetCorpsePlayer;
import dwo.gameserver.handler.targets.TargetEnemySummon;
import dwo.gameserver.handler.targets.TargetFlagPole;
import dwo.gameserver.handler.targets.TargetFrontArea;
import dwo.gameserver.handler.targets.TargetFrontAura;
import dwo.gameserver.handler.targets.TargetGround;
import dwo.gameserver.handler.targets.TargetHoly;
import dwo.gameserver.handler.targets.TargetMentee;
import dwo.gameserver.handler.targets.TargetMentor;
import dwo.gameserver.handler.targets.TargetOne;
import dwo.gameserver.handler.targets.TargetOwnerPet;
import dwo.gameserver.handler.targets.TargetParty;
import dwo.gameserver.handler.targets.TargetPartyClan;
import dwo.gameserver.handler.targets.TargetPartyMember;
import dwo.gameserver.handler.targets.TargetPartyNotMe;
import dwo.gameserver.handler.targets.TargetPartyOther;
import dwo.gameserver.handler.targets.TargetPet;
import dwo.gameserver.handler.targets.TargetSelf;
import dwo.gameserver.handler.targets.TargetSublime;
import dwo.gameserver.handler.targets.TargetSummon;
import dwo.gameserver.handler.targets.TargetSummonAndMe;
import dwo.gameserver.handler.targets.TargetSummonOne;
import dwo.gameserver.handler.targets.TargetUnlockable;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TargetHandler implements IHandler<ITargetTypeHandler, Enum<L2TargetType>>
{
	protected static Logger _log = LogManager.getLogger(TargetHandler.class);

	private final Map<Enum<L2TargetType>, ITargetTypeHandler> _handlers;

	private TargetHandler()
	{
		_handlers = new HashMap<>();
		registerHandler(new TargetAlly());
		registerHandler(new TargetArea());
		registerHandler(new TargetAreaCorpseMob());
		registerHandler(new TargetAreaSummon());
		registerHandler(new TargetAura());
		registerHandler(new TargetAuraCorpseMob());
		registerHandler(new TargetBehindArea());
		registerHandler(new TargetBehindAura());
		registerHandler(new TargetChain());
		registerHandler(new TargetClan());
		registerHandler(new TargetClanMember());
		registerHandler(new TargetCommandChannel());
		registerHandler(new TargetCorpseAlly());
		registerHandler(new TargetCorpseClan());
		registerHandler(new TargetCorpseParty());
		registerHandler(new TargetCorpseCommandChannel());
		registerHandler(new TargetCorpsePartyMember());
		registerHandler(new TargetCorpseMob());
		registerHandler(new TargetCorpsePet());
		registerHandler(new TargetCorpsePlayer());
		registerHandler(new TargetEnemySummon());
		registerHandler(new TargetFlagPole());
		registerHandler(new TargetFrontArea());
		registerHandler(new TargetFrontAura());
		registerHandler(new TargetGround());
		registerHandler(new TargetHoly());
		registerHandler(new TargetOne());
		registerHandler(new TargetMentee());
		registerHandler(new TargetMentor());
		registerHandler(new TargetOwnerPet());
		registerHandler(new TargetParty());
		registerHandler(new TargetPartyClan());
		registerHandler(new TargetPartyMember());
		registerHandler(new TargetPartyNotMe());
		registerHandler(new TargetPartyOther());
		registerHandler(new TargetPet());
		registerHandler(new TargetSelf());
		registerHandler(new TargetSublime());
		registerHandler(new TargetSummonOne());
		registerHandler(new TargetSummon());
		registerHandler(new TargetSummonAndMe());
		registerHandler(new TargetUnlockable());
		registerHandler(new TargetMentee());
		registerHandler(new TargetMentor());
		_log.log(Level.INFO, "Loaded " + size() + " Target Handlers");
	}

	public static TargetHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void registerHandler(ITargetTypeHandler handler)
	{
		_handlers.put(handler.getTargetType(), handler);
	}

	@Override
	public void removeHandler(ITargetTypeHandler handler)
	{
		synchronized(this)
		{
			_handlers.remove(handler.getTargetType());
		}
	}

	@Override
	public ITargetTypeHandler getHandler(Enum<L2TargetType> skillTargetType)
	{
		return _handlers.get(skillTargetType);
	}

	@Override
	public int size()
	{
		return _handlers.size();
	}

	private static class SingletonHolder
	{
		protected static final TargetHandler _instance = new TargetHandler();
	}
}