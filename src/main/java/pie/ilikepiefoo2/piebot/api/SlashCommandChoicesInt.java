package pie.ilikepiefoo2.piebot.api;

public @interface SlashCommandChoicesInt {
    public String[] choiceNames() default {};
    public int[] choiceValues() default {};
}
