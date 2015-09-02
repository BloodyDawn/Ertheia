package dwo.gameserver.model.actor.templates;

import dwo.gameserver.datatables.xml.CharStartingItems;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.player.PlayerLvlUpData;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.base.Sex;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class L2PcTemplate extends L2CharTemplate
{
	private static final Logger _log = LogManager.getLogger(L2PcTemplate.class);

	private final ClassId _classId;
	private final Race _race;

	private final int _classBaseLevel;

	private final Map<Integer, PlayerLvlUpData> _baseHpMpCp;
	private final L2CharBaseTemplate _baseTemplate;

	private final List<ItemHolder> _initialEquipment;

	public L2PcTemplate(L2CharBaseTemplate baseTemplate, StatsSet set, Map<Integer, PlayerLvlUpData> baseStats)
	{
		super(set);
		_classId = ClassId.getClassId(set.getInteger("classId"));
		_race = Race.values()[set.getInteger("raceId")];

		_classBaseLevel = set.getInteger("classBaseLevel");

		_baseHpMpCp = baseStats;
		_baseTemplate = baseTemplate;

		if(baseTemplate != null)
		{
			setBasePAtk(_baseTemplate.getStats().getPAtk());
			setBaseMatk(_baseTemplate.getStats().getMAtk());
			setBasePAtkSpd(_baseTemplate.getStats().getAtkSpd());
			setBaseCritRate(_baseTemplate.getStats().getCritRate());
			setBaseMAtkSpd(_baseTemplate.getStats().getCastSpd());
			setBaseMCritRate(_baseTemplate.getStats().getMCritRate());
			setBaseAttackRange(_baseTemplate.getStats().getAttackRange());
			setBaseBreath(_baseTemplate.getStats().getBreathBonus());
			setBaseWalkSpd(_baseTemplate.getStats().getWalkSpd());
			setBaseRunSpd(_baseTemplate.getStats().getRunSpd());
			setBaseDEX(_baseTemplate.getDefaultAttributes().base().getDex());
			setBaseSTR(_baseTemplate.getDefaultAttributes().base().getStr());
			setBaseCON(_baseTemplate.getDefaultAttributes().base().getCon());
			setBaseINT(_baseTemplate.getDefaultAttributes().base().getInt());
			setBaseWIT(_baseTemplate.getDefaultAttributes().base().getWit());
			setBaseMEN(_baseTemplate.getDefaultAttributes().base().getMen());
			setBaseLUC( _baseTemplate.getDefaultAttributes().base().getLuc() );
			setBaseCHA( _baseTemplate.getDefaultAttributes().base().getCha() );

			int ringDef = _baseTemplate.getDefaultAttributes().defense().getLeftRing() + _baseTemplate.getDefaultAttributes().defense().getRightRing();
			int earringDef = _baseTemplate.getDefaultAttributes().defense().getLeftEarring() + _baseTemplate.getDefaultAttributes().defense().getRightEarring();
			int necklaceDef = _baseTemplate.getDefaultAttributes().defense().getNecklace();

			setBaseMDef(ringDef + earringDef + necklaceDef);
		}

		_initialEquipment = CharStartingItems.getInstance().getEquipmentList(_classId.getId());
	}

	public int getFallHeight(L2Character activeChar)
	{
		return activeChar instanceof L2PcInstance ? _baseTemplate.getStats().getSafeFallHeight(((L2PcInstance) activeChar).getAppearance().getSex() ? Sex.FEMALE : Sex.MALE) : Integer.MAX_VALUE;
	}

	@Override
	public int getCollisionRadius(L2Character activeChar)
	{
		return activeChar instanceof L2PcInstance ? (int) _baseTemplate.getStats().getCollisionRadius(((L2PcInstance) activeChar).getAppearance().getSex() ? Sex.FEMALE : Sex.MALE) : super.getCollisionRadius(activeChar);
	}

	@Override
	public int getCollisionHeight(L2Character activeChar)
	{
		try
		{
			return activeChar instanceof L2PcInstance ? (int) _baseTemplate.getStats().getCollisionHeight(((L2PcInstance) activeChar).getAppearance().getSex() ? Sex.FEMALE : Sex.MALE) : super.getCollisionHeight(activeChar);
		}
		catch(Exception e)
		{
			_log.error("Failed to compute PC collision height!", e);
			if(_baseTemplate == null)
			{
				_log.error("Base template is NULL!");
			}
			else if(_baseTemplate.getStats() == null)
			{
				_log.error("Base template stats set is NULL!");
			}
			return 80;
		}
	}

	@Override
	public double getFCollisionRadius(L2Character activeChar)
	{
		return getCollisionRadius(activeChar);
	}

	@Override
	public double getFCollisionHeight(L2Character activeChar)
	{
		return getCollisionHeight(activeChar);
	}

	public float getBaseRandomDamage()
	{
		return _baseTemplate.getStats().getRandomDamage();
	}

	public L2CharBaseTemplate getBaseCharTemplate()
	{
		return _baseTemplate;
	}

	public ClassId getClassId()
	{
		return _classId;
	}

	public Race getRace()
	{
		return _race;
	}

	public int getClassBaseLevel()
	{
		return _classBaseLevel;
	}

	public double getBaseHp(int level)
	{
		return _baseHpMpCp.get(level).getHP();
	}

	public double getBaseCp(int level)
	{
		return _baseHpMpCp.get(level).getCP();
	}

	public double getBaseMp(int level)
	{
		return _baseHpMpCp.get(level).getMP();
	}

	/**
	 * @return стартовые предметы для персонажа
	 */
	public List<ItemHolder> getInitialEquipment()
	{
		return _initialEquipment;
	}

	/**
	 * @return {@code true} если у темплейта персонажа есть стартовые предметы
	 */
	public boolean hasInitialEquipment()
	{
		return _initialEquipment != null;
	}
}
