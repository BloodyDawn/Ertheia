package dwo.gameserver.model.skills.base.formulas;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 20:57
 */

public interface Variables
{
	Logger _log = LogManager.getLogger(Variables.class);

	int HP_REGENERATE_PERIOD = 3000; // Период регенерации здоровья (3 секунды)

	byte SHIELD_DEFENSE_FAILED = 0; // защита щитом не сработала
	byte SHIELD_DEFENSE_SUCCEED = 1; // обычная блокировка щитом
	byte SHIELD_DEFENSE_PERFECT_BLOCK = 2; // превосходная блокировка щитом

	byte SKILL_REFLECT_FAILED = 0; // нет возврата урона
	byte SKILL_REFLECT_SUCCEED = 1; // обычный возврат урона
	byte SKILL_REFLECT_VENGEANCE = 2; // 100%-ый возврат уронв

	byte MELEE_ATTACK_RANGE = 40; // радиус атаки физическими мили скилами
}
