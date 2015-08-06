package dwo.gameserver.model.skills.base.proptypes;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 15.09.12
 * Time: 18:31
 */
public enum L2SkillComboType
{

	// 367 = Опрокидывающая Атака
	// 365 = Сковывающая Атака
	// 424 = Проникнуть во внутрь Земляного Червя

	// 457
	// 458     комбо для инста 4 профы
	// 459

	NO_COMBO(0, "no_combo"),
	COMBO_FLY_UP(365, "comboFlyUp"),
	COMBO_KNOCK_DOWN(367, "comboKnockDown"),

    COMBO_AQ_HARNAK_1(457, "comboAqHarnak1"),
    COMBO_AQ_HARNAK_2(458, "comboAqHarnak2"),
    COMBO_AQ_HARNAK_3(459, "comboAqHarnak3"),
    
    COMBO_CHARGE(499, "comboCharge");

	private final int _id;
	private final String _dbField;

	private L2SkillComboType(int id, String dbField)
	{
		_id = id;
		_dbField = dbField;
	}

	public int getId()
	{
		return _id;
	}

}
