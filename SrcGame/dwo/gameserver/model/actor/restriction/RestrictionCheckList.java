package dwo.gameserver.model.actor.restriction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restriction list for annotating restriction checkers (basically, classes).
 * @author Yorie
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestrictionCheckList
{
	RestrictionCheck[] value();
}
