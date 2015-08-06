package dwo.gameserver.handler;

import dwo.gameserver.handler.effects.*;
import dwo.gameserver.model.skills.effects.L2Effect;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.11
 * Time: 12:27
 */

public class EffectHandler implements IHandler<Class<? extends L2Effect>, String>
{
	private static final Logger _log = LogManager.getLogger(EffectHandler.class);
	private final Map<String, Class<? extends L2Effect>> _handlers;

	private EffectHandler()
	{
		_handlers = new HashMap<>();
		registerHandler(AbortCast.class);
		registerHandler(AbortCastAll.class);
		registerHandler(AwakenForce.class);
		registerHandler(Betray.class);
		registerHandler(BetrayalMark.class);
		registerHandler(BigHead.class);
		registerHandler(BlockResurrection.class);
		registerHandler(BlockSkills.class);
		registerHandler(Bluff.class);
		registerHandler(Buff.class);
		registerHandler(BlackHole.class);
		registerHandler(Blink.class);
		registerHandler(Cancel.class);
		registerHandler(CancelAll.class);
		registerHandler(CancelDebuff.class);
		registerHandler(CancelAllDebuff.class);
		registerHandler(ChameleonRest.class);
		registerHandler(ChanceSkillTrigger.class);
		registerHandler(ChangeFace.class);
		registerHandler(ChangeHairColor.class);
		registerHandler(ChangeHairStyle.class);
		registerHandler(CharmOfCourage.class);
		registerHandler(CharmOfLuck.class);
		registerHandler(ClanGate.class);
		registerHandler(ConfuseMob.class);
		registerHandler(Confusion.class);
		registerHandler(CpDamPercent.class);
		registerHandler(CpHeal.class);
		registerHandler(CpHealOverTime.class);
		registerHandler(CpHealPercent.class);
		registerHandler(CrystalGradeModify.class);
		registerHandler(Cell.class);
		registerHandler(CpHpHeal.class);
		registerHandler(Debuff.class);
		registerHandler(Disarm.class);
		registerHandler(DispelBySlot.class);
        registerHandler(DualCast.class);
		registerHandler(NonStopCast.class);
		registerHandler(EnemyCharge.class);
		registerHandler(FakeDeath.class);
		registerHandler(Fear.class);
		registerHandler(FeatherOfBlessing.class);
		registerHandler(FlyUp.class);
		registerHandler(Fusion.class);
		registerHandler(Grow.class);
		registerHandler(HpDamPercent.class);
		registerHandler(HpDamOverTime.class);
		registerHandler(HpDamOverTimePercent.class);
		registerHandler(HpHeal.class);
		registerHandler(HpHealOverTime.class);
		registerHandler(HpHealPercent.class);
        registerHandler(HpToOne.class);
		registerHandler(Hide.class);
		registerHandler(IgnoreDeath.class);
		registerHandler(ImmobileBuff.class);
		registerHandler(ImmobilePetBuff.class);
		registerHandler(IncreaseCharges.class);
		registerHandler(Invincible.class);
		registerHandler(IncreaseChargesOverTime.class);
		registerHandler(KnockBack.class);
		registerHandler(KnockDown.class);
		registerHandler(Laksis.class);
		registerHandler(MpDamOverTime.class);
		registerHandler(MpHeal.class);
		registerHandler(MpHealOverTime.class);
		registerHandler(MpHealPercent.class);
		registerHandler(MpConsumePerLevel.class);
		registerHandler(Mute.class);
		registerHandler(MpHealByLevel.class);
		registerHandler(MpHpHealByLevel.class);
		registerHandler(MarkOfVoid.class);
		registerHandler(Negate.class);
		registerHandler(NegateMark.class);
		registerHandler(NoblesseBless.class);
        registerHandler(NonStopCast.class);
		registerHandler(Paralyze.class);
		registerHandler(Petrification.class);
		registerHandler(PhoenixBless.class);
		registerHandler(PhysicalAttackMute.class);
		registerHandler(PhysicalMute.class);
		registerHandler(Plunder.class);
		registerHandler(ProtectionBlessing.class);
		registerHandler(RandomizeHate.class);
		registerHandler(Recovery.class);
		registerHandler(Relax.class);
		registerHandler(RemoveTarget.class);
		registerHandler(ResistSkillId.class);
		registerHandler(RestorationRandom.class);
		registerHandler(Root.class);
		registerHandler(SetHP.class);
		registerHandler(Shackle.class);
		registerHandler(Signet.class);
		registerHandler(SignetAntiSummon.class);
		registerHandler(SignetDebuff.class);
		registerHandler(SignetMDam.class);
		registerHandler(SignetPDam.class);
		registerHandler(SignetNoise.class);
		registerHandler(SilentMove.class);
		registerHandler(Sleep.class);
		registerHandler(Slow.class);
        registerHandler(Spallation.class);
		registerHandler(Spoil.class);
		registerHandler(Stun.class);
		registerHandler(SummonShare.class);
		registerHandler(TargetMe.class);
		registerHandler(TargetLock.class);
		registerHandler(TargetImmunity.class);
		registerHandler(ThrowHorizontal.class);
		registerHandler(ThrowUp.class);
		registerHandler(TransferDamage.class);
		registerHandler(Transformation.class);
		registerHandler(NonStopCast.class);
		registerHandler(TalismanPower.class);
		registerHandler(Warp.class);
		registerHandler(WarpForward.class);
		registerHandler(RemoteControl.class);
		registerHandler(SummonAgathion.class);
		registerHandler(UnsummonAgathion.class);
		registerHandler(Chain.class);
		registerHandler(BlockRecall.class);
		registerHandler(HpCpStaticHeal.class);
		registerHandler(ShiftTarget.class);
		registerHandler(TransformDebuff.class);

		_log.log(Level.INFO, "Loaded " + size() + " Effect Handlers");
	}

	public static EffectHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void registerHandler(Class<? extends L2Effect> func)
	{
		_handlers.put(func.getSimpleName(), func);
	}

	@Override
	public void removeHandler(Class<? extends L2Effect> handler)
	{
		synchronized(this)
		{
			_handlers.remove(handler.getSimpleName());
		}
	}

	@Override
	public Class<? extends L2Effect> getHandler(String name)
	{
		return _handlers.get(name);
	}

	@Override
	public int size()
	{
		return _handlers.size();
	}

	private static class SingletonHolder
	{
		private static final EffectHandler _instance = new EffectHandler();
	}
}