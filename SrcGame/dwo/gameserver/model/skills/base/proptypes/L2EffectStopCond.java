package dwo.gameserver.model.skills.base.proptypes;

/**
 * User: Bacek
 * Date: 21.06.13
 * Time: 17:57
 */
public enum L2EffectStopCond
{
	ON_NONE,

	ON_DAMAGE_DEBUFF,             // При получении урона для дебафов
	ON_DAMAGE_BUFF,               // При получении урона для бафов

	ON_ATTACK_DEBUFF,             // При атаке для дебафов
	ON_ATTACK_BUFF,               // При атаке для бафов

	ON_START_DEBUFF,                   // TODO При прохождении дебафа
	ON_START_BUFF,                     // TODO

	ON_ACTION_EXCEPT_MOVE,        // При действиях
	ON_DISCONNECT
}
