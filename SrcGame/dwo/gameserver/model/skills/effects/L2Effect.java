package dwo.gameserver.model.skills.effects;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.ChanceCondition;
import dwo.gameserver.model.skills.IChanceSkillTrigger;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.Effects;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.base.funcs.Lambda;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.AbnormalStatusUpdate;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadSpelledInfo;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySpelled;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class L2Effect implements IChanceSkillTrigger
{
    protected static final Logger _log = LogManager.getLogger(L2Effect.class);

    private static final Func[] _emptyFunctionSet = new Func[0];

    //member _effector is the instance of L2Character that cast/used the spell/skill that is
    //causing this effect.  Do not confuse with the instance of L2Character that
    //is being affected by this effect.
    private final L2Character _effector;

    //member _effected is the instance of L2Character that was affected
    //by this effect.  Do not confuse with the instance of L2Character that
    //casted/used this effect.
    private final L2Character _effected;

    //the skill that was used.
    private final L2Skill _skill;

    //or the items that was used.
    //private final L2Item _item;

    // the value of an update
    private final Lambda _lambda;
    private final EffectTemplate _template;
    // function templates
    private final FuncTemplate[] _funcTemplates;
    private final List<L2EffectStopCond> _onRemovedEffect = new ArrayList<>();
    // abnormal effect mask
    private final AbnormalEffect[] _abnormalEffect;
    // show icon
    private final boolean _icon;
    /**
     * The position of the effect in the stack group
     */
    private final byte _abnormalLvl;
    public boolean preventExitUpdate;
    // the current state
    private EffectState _state;
    // period, seconds
    private int _abnormalTime;
    private int _periodStartTicks;
    private int _periodFirstTime;
    // counter
    private int _tickCount;
    private Byte _hitMaxCount;
    private Byte _hitCorrectCount = 0;
    // is self effect?
    private boolean _isSelfEffect;
    // is passive effect?
    private boolean _isPassiveEffect;
    // false - новый эффек  true - обновление времени эффека
    private boolean _isRefreshTime;
    private ScheduledFuture<?> _currentFuture;

    /**
     * The Identifier of the stack group
     */
    private String _abnormalType;

    /**
     * If true, all checks on effect stacking will be ommited
     */
    private boolean _ignoreAbnormalType;
    private boolean _inUse;
    private boolean _startConditionsCorrect = true;
    /**
     * For special behavior. See Formulas.calcEffectSuccess
     */
    private double _effectPower;
    private L2SkillType _effectSkillType;
    private boolean cancelled;

    /**
     * >WARNING: scheduleEffect no longer inside constructor
     * So you must call it explicitly
     */
    protected L2Effect(Env env, EffectTemplate template)
    {
        _state = EffectState.CREATED;
        _skill = env.getSkill();
        //_item = env._item == null ? null : env._item.getItem();
        _template = template;
        _effected = env.getTarget();
        _effector = env.getCharacter();
        _lambda = template.lambda;
        _funcTemplates = template.funcTemplates;
        _onRemovedEffect.addAll(template.onRemovedEffect);
        _hitMaxCount = template.hitToRemove;
        _abnormalTime = Effects.calcEffectAbnormalTime(env, template);
        _abnormalEffect = _template.getAbnormalVisualEffect();
        _abnormalType = _template.getAbnormalType();
        _abnormalLvl = _template.getAbnormalLevel();
        _periodStartTicks = GameTimeController.getInstance().getGameTicks();
        _tickCount = 0;
        _periodFirstTime = 0;
        _icon = template.icon;
        _effectPower = template.effectPower;
        _effectSkillType = template.effectType;
    }

    /**
     * Special constructor to "steal" buffs. Must be implemented on
     * every child class that can be stolen.
     * WARNING: scheduleEffect nolonger inside constructor
     * So you must call it explicitly
     */
    protected L2Effect(Env env, L2Effect effect)
    {
        _template = effect._template;
        _state = EffectState.CREATED;
        _skill = env.getSkill();
        _effected = env.getTarget();
        _effector = env.getCharacter();
        _lambda = _template.lambda;
        _funcTemplates = _template.funcTemplates;
        _tickCount = effect._tickCount;
        _abnormalTime = _template.abnormalTime;
        _abnormalEffect = _template.getAbnormalVisualEffect();
        _abnormalType = _template.getAbnormalType();
        _abnormalLvl = _template.getAbnormalLevel();
        _periodStartTicks = effect._periodStartTicks;
        _periodFirstTime = effect.getTime();
        _icon = _template.icon;
        _onRemovedEffect.addAll(_template.onRemovedEffect);
        _hitMaxCount = _template.hitToRemove;
    }

    public int getCount()
    {
        return _tickCount;
    }

    public void setCount(int newcount)
    {
        _tickCount = Math.min(newcount, _template.getTotalTickCount()); // sanity check
    }

    public void setFirstTime(int newFirstTime)
    {
        _periodFirstTime = Math.min(newFirstTime, _abnormalTime);
        _periodStartTicks -= _periodFirstTime * GameTimeController.TICKS_PER_SECOND;
    }

    public boolean isIconDisplay()
    {
        return _icon;
    }

    public int getAbnormalTime()
    {
        return _abnormalTime;
    }

    public void setAbnormalTime(int time)
    {
        _abnormalTime = time;
    }

    public int getTime()
    {
        return (GameTimeController.getInstance().getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
    }

    /**
     * Get the remaining time.
     * @return the remaining time
     */
    public int getTimeLeft()
    {
        if(_template.getTotalTickCount() > 1)
        {
            return (_abnormalTime > 0 ? _abnormalTime : 1) * _template.getTotalTickCount() - getTime();
        }
        return _abnormalTime - getTime();
    }

    public boolean isInUse()
    {
        return _inUse;
    }

    public boolean setInUse(boolean inUse)
    {
        _inUse = inUse;
        if(_inUse)
        {
            _startConditionsCorrect = onStart();
        }
        else
        {
            onExit();
        }

        return _startConditionsCorrect;
    }

    public String getAbnormalType()
    {
        return _abnormalType;
    }

    public void setAbnormalType(String type)
    {
        _abnormalType = type;
    }

    public byte getAbnormalLvl()
    {
        return _abnormalLvl;
    }

    public L2Skill getSkill()
    {
        return _skill;
    }

    public L2Character getEffector()
    {
        return _effector;
    }

    public L2Character getEffected()
    {
        return _effected;
    }

    public boolean isSelfEffect()
    {
        return _isSelfEffect;
    }

    public void setSelfEffect()
    {
        _isSelfEffect = true;
    }

    public boolean isPassiveEffect()
    {
        return _isPassiveEffect;
    }

    public void setPassiveEffect()
    {
        _isPassiveEffect = true;
    }

    public boolean isHerbEffect()
    {
        return _skill.isHerbEffect();
    }

    public double calc()
    {
        Env env = new Env();
        env.setPlayer(_effector);
        env.setTarget(_effected);
        env.setSkill(_skill);
        return _lambda.calc(env);
    }

    /**
     * Restarts effect task. Effect duration time and affect count will be reset.
     */
    public void restart()
    {
        _isRefreshTime = true;
        _tickCount = 0;
        _state = EffectState.ACTING;
        _periodStartTicks = GameTimeController.getInstance().getGameTicks();
        startEffectTask();
    }

    /**
     * Start the effect task.<br>
     * If the effect has ticks defined it will be scheduled.<br>
     * If abnormal time is defined (greater than 1) the period will be calculated like abnormal time divided total tick count.<br>
     * Otherwise it each tick will represent 1 second (1000 milliseconds).
     */
    private void startEffectTask()
    {
        stopEffectTask();

        if(isInstant())
        {
            _currentFuture = ThreadPoolManager.getInstance().scheduleEffect(new EffectTask(), 5);
            return;
        }

        // Задержка перед стартом, обычно 5000
        int initialDelay = Math.max((_abnormalTime - _periodFirstTime) * 1000, 5); // Sanity check
        _currentFuture = _template.getTotalTickCount() > 0 ? ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new EffectTask(), initialDelay, _abnormalTime * 1000) : ThreadPoolManager.getInstance().scheduleEffect(new EffectTask(), initialDelay);

        if(_state == EffectState.ACTING)
        {
            if(isSelfEffectType())
            {
                _effector.addEffect(this);
            }
            else
            {
                _effected.addEffect(this);
            }
        }
    }

    /**
     * Stop the L2Effect task and send ServerMode->Client update packet.<BR><BR>
     * <p/>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Cancel the effect in the the abnormal effect map of the L2Character </li>
     * <li>Stop the task of the L2Effect, remove it and update client magic icon </li><BR><BR>
     */
    public void exit()
    {
        exit(false);
    }

    public void exit(boolean preventUpdate)
    {
        preventExitUpdate = preventUpdate;
        _state = EffectState.FINISHING;
        scheduleEffect();
    }

    public void cancel()
    {
        cancelled = true;
        exit();
    }

    /**
     * Stop the task of the L2Effect, remove it and update client magic icon.<BR><BR>
     * <p/>
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Cancel the task </li>
     * <li>Stop and remove L2Effect from L2Character and update client magic icon </li><BR><BR>
     */
    public void stopEffectTask()
    {
        synchronized(this)
        {
            if(_currentFuture != null)
            {
                // Cancel the task
                _currentFuture.cancel(false);
                _currentFuture = null;

                if(isSelfEffectType() && _effector != null)
                {
                    _effector.removeEffect(this);
                }
                else if(_effected != null)
                {
                    _effected.removeEffect(this);
                }

                if(_effected != null && _effected.getPets() != null)
                {
                    for(L2Summon summon : _effected.getPets())
                    {
                        summon.removeEffect(this);
                    }
                }
            }
        }
    }

    /**
     * @return effect type
     */
    public abstract L2EffectType getEffectType();

    /**
     * Notify started.
     * @return {@code true} if all the start conditions are meet, {@code false} otherwise
     */
    public boolean onStart()
    {
        if(_abnormalEffect != null)
        {
            _effected.startAbnormalEffect(_abnormalEffect);
        }

        return true;
    }

    /**
     * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR><BR>
     */
    public void onExit()
    {
        if(_abnormalEffect != null)
        {
            _effected.stopAbnormalEffect(_abnormalEffect);
        }
    }

    /**
     * Method called on each tick.<br>
     * By default if the abnormal time is lesser than zero it will return {@code true}, this means the effect will last forever.
     * @return if {@code true} this effect will continue forever, if {@code false} it will stop after tick count is reached
     */
    public boolean onActionTime()
    {
        return _abnormalTime < 0;
    }

    public void scheduleEffect()
    {
        switch(_state)
        {
            case CREATED: {
                _state = isInstant() ? EffectState.FINISHING : EffectState.ACTING;

                if (_skill.isPvpSkill() && _icon && _effected instanceof L2PcInstance && !_skill.isMsgStatusHidden()) {
                    _effected.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(_skill));
                }

                if (getAbnormalTime() > 0) {
                    startEffectTask();
                    return;
                }

                // effects not having count or period should start
                _startConditionsCorrect = onStart();

                if (isSelfEffectType()) {
                    _effector.addEffect(this);
                    break;
                }

                _effected.addEffect(this);
                break;
            }
            case ACTING: {
                if (getAbnormalTime() == -1) {
                    return; // Do not finish.
                }

                if (_template.getTotalTickCount() > 0) {
                    _tickCount++; // Increase tick count.

                    if (isInUse() && onActionTime() && _startConditionsCorrect) {
                        return; // Do not finish.
                    }

                    if (_tickCount <= _template.getTotalTickCount()) {
                        return; // Do not finish it yet, has remaining ticks.
                    }
                }

                _state = EffectState.FINISHING;
            } 
            case FINISHING: {
                //If the time left is equal to zero, send the message
                if (_tickCount == 0 && _icon && _effected instanceof L2PcInstance && !getSkill().isMsgStatusHidden()) {
                    _effected.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF).addSkillName(_skill));
                }

                // if task is null - stopEffectTask does not remove effect
                if (_currentFuture == null && getEffected() != null) {
                    getEffected().removeEffect(this);
                    if (getEffected().getPets() != null) {
                        for (L2Summon summon : getEffected().getPets()) {
                            summon.removeEffect(this);
                        }
                    }
                }
                // Stop the task of the L2Effect, remove it and update client magic icon
                stopEffectTask();

                // Cancel the effect in the the abnormal effect map of the L2Character
                if (isInUse() || (_tickCount <= 1 || getAbnormalTime() <= 0) && _startConditionsCorrect) {
                    onExit();
                }

                if (_skill.getAfterEffectId() <= 0) {
                    break;
                }

                final L2Skill skill = SkillTable.getInstance().getInfo(_skill.getAfterEffectId(), _skill.getAfterEffectLvl());
                if (skill != null) {
                    getEffected().broadcastPacket(new MagicSkillUse(_effected, skill.getId(), skill.getLevel(), 0, 0));
                    getEffected().broadcastPacket(new MagicSkillLaunched(_effected, skill.getId(), skill.getLevel()));
                    skill.getEffects(getEffected(), getEffected());
                    break;
                }
                break;
            }
        }
    }

    public Func[] getStatFuncs()
    {
        if(_funcTemplates == null)
        {
            return _emptyFunctionSet;
        }
        ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);

        Env env = new Env();
        env.setPlayer(_effector);
        env.setTarget(_effected);
        env.setSkill(_skill);
        Func f;

        for(FuncTemplate t : _funcTemplates)
        {
            f = t.getFunc(env, this); // effect is owner
            if(f != null)
            {
                funcs.add(f);
            }
        }
        if(funcs.isEmpty())
        {
            return _emptyFunctionSet;
        }

        return funcs.toArray(new Func[funcs.size()]);
    }

    public void addIcon(AbnormalStatusUpdate mi)
    {
        if(_state != EffectState.ACTING)
        {
            return;
        }
        
        if (getSkill().getId() == 1940 
                || getSkill().getId() == 1936 
                || getSkill().getId() == 1932 
                || getSkill().getId() == 1928 
                || getSkill().getId() == 1930 
                || getSkill().getId() == 1938 
                || getSkill().getId() == 1934 
                || getSkill().getId() == 1955
                || getSkill().getId() == 30603
                || getSkill().getId() == 30606
                ) 
        {
            mi.addEffect(getSkill().getDisplayId(), getSkill().getLevel(), -1, -1);
        }
        else if(_abnormalTime == -1 || _skill.isToggle())
        {
            mi.addEffect(_skill.getDisplayId(), _skill.getLevel(), -1, getTimeLeft());
        }
        else
        {
            mi.addEffect(_skill.getDisplayId(), _skill.getLevel(), _skill.getBuffDuration(), getTimeLeft());
        }
    }

    public void addPartySpelledIcon(PartySpelled ps)
    {
        if(_state != EffectState.ACTING)
        {
            return;
        }

        ScheduledFuture<?> future = _currentFuture;

        if(_abnormalTime == -1 || _skill.isToggle())
        {
            ps.addPartySpelledEffect(_skill.getDisplayId(), _skill.getLevel(), -1);
        }
        else if(future != null)
        {
            ps.addPartySpelledEffect(_skill.getDisplayId(), _skill.getLevel(), (int) future.getDelay(TimeUnit.SECONDS));
        }
    }

    public void addOlympiadSpelledIcon(ExOlympiadSpelledInfo spelledInfo)
    {
        if(_state != EffectState.ACTING)
        {
            return;
        }

        ScheduledFuture<?> future = _currentFuture;
        if(future != null)
        {
            spelledInfo.addEffect(_skill.getDisplayId(), _skill.getLevel(), (int) future.getDelay(TimeUnit.SECONDS));
        }
        else if(_abnormalTime == -1)
        {
            spelledInfo.addEffect(_skill.getDisplayId(), _skill.getLevel(), -1);
        }
    }

    public EffectTemplate getEffectTemplate()
    {
        return _template;
    }

    public double getEffectPower()
    {
        return _effectPower;
    }

    public boolean canBeStolen()
    {
        return !(!effectCanBeStolen() || getEffectType() == L2EffectType.TRANSFORMATION || _skill.isPassive() || _skill.isToggle() || _skill.isDebuff() || _skill.isHeroSkill() || _skill.isGMSkill() || _skill.isStatic() || !_skill.canBeDispeled());
    }

    /**
     * @return {@code true} если эффект может быть украден
     */
    protected boolean effectCanBeStolen()
    {
        return false;
    }

    /**
     * @return бит флага текущего эффекта
     */
    public int getEffectFlags()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return "Effect " + getClass().getSimpleName() + ", " + _skill + ", State: " + _state + ", Time: " + _abnormalTime + ", Remaining: " + getTimeLeft();
    }

    public boolean isSelfEffectType()
    {
        return false;
    }

    public void decreaseForce()
    {
    }

    public void increaseEffect()
    {
    }

    @Override
    public boolean triggersChanceSkill()
    {
        return false;
    }

    @Override
    public int getTriggeredChanceId()
    {
        return 0;
    }

    @Override
    public int getTriggeredChanceLevel()
    {
        return 0;
    }

    @Override
    public ChanceCondition getTriggeredChanceCondition()
    {
        return null;
    }

    public void setIgnoreAbnormalType(boolean value)
    {
        _ignoreAbnormalType = value;
    }

    public boolean isAbnormatTypeIgnored()
    {
        return _ignoreAbnormalType;
    }

    public void addRemovedEffectType(L2EffectStopCond e)
    {
        _onRemovedEffect.add(e);
    }

    public List<L2EffectStopCond> getRemovedEffectType()
    {
        return _onRemovedEffect;
    }

    public boolean isHitCountRemove()
    {
        _hitCorrectCount++;
        return _hitCorrectCount < _hitMaxCount;
    }

    public boolean isRefreshTime()
    {
        return _isRefreshTime;
    }

    public L2SkillType getSkillType()
    {
        return _effectSkillType;
    }

    /**
     * Verify if this effect is an instant effect.
     * @return {@code true} if this effect is instant, {@code false} otherwise
     */
    public boolean isInstant()
    {
        return false;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    private class EffectTask implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                _periodFirstTime = 0;
                _periodStartTicks = GameTimeController.getInstance().getGameTicks();
                scheduleEffect();
            }
            catch(Exception e)
            {
                _log.log(Level.ERROR, "Error while running EffectTask()", e);
            }
        }
    }
}