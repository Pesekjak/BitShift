package me.pesekjak.bitshift.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprBit extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprBit.class, Number.class, ExpressionType.COMBINED,
                "[the] %integer%(st|nd|rd|th) bit (in|of) %numbers%",
                "bit %integer% (in|of) %numbers%");
    }

    private Expression<Integer> positionExpr;
    private Expression<Number> numberExpr;

    @Override
    protected @Nullable Number[] get(Event e) {
        Integer integer = positionExpr.getSingle(e);
        if (integer == null) return null;
        int pos = integer;
        Number[] numbers = numberExpr.getArray(e);
        Number[] bits = new Number[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            long num = numbers[i].longValue();
            num >>= (pos - 1);
            num &= 1;
            bits[i] = num;
        }
        return bits;
    }

    @Override
    public boolean isSingle() {
        return numberExpr.isSingle();
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "bit " + positionExpr.toString(e, debug) + " of " + numberExpr.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        positionExpr = (Expression<Integer>) exprs[0];
        numberExpr = (Expression<Number>) exprs[1];
        return true;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET)
            return CollectionUtils.array(Boolean.class, Number.class);
        return null;
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta[0] == null) return;
        Integer integer = positionExpr.getSingle(e);
        if (integer == null) return;
        int pos = integer;
        Number[] numbers = numberExpr.getArray(e);
        int x;
        if (delta[0] instanceof Boolean bool)
            x = bool ? 1 : 0;
        else x = ((Number) delta[0]).intValue();
        if (x != 1 && x != 0) return;
        for (int i = 0; i < numbers.length; i++) {
            long num = 1L << (pos - 1);
            if (x == 0) {
                num = ~num;
                num &= numbers[i].longValue();
            }
            else {
                num |= numbers[i].longValue();
            }
            numbers[i] = num;
        }
        numberExpr.change(e, numbers, Changer.ChangeMode.SET);
    }

}
