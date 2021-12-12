package net.splatcraft.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.util.Identifier;

public class ColorOption extends Option<Integer> {
    protected ColorOption(Integer defaultValue) {
        super(defaultValue);
    }

    public static ColorOption of(int defaultValue) {
        return new ColorOption(defaultValue);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.getValue());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json instanceof JsonPrimitive primitive && primitive.isNumber()) {
            this.setValue(primitive.getAsInt());
        } else invalidConfig(json);
    }

    @Override
    public AbstractConfigListEntry<Integer> createConfigListEntry(Identifier id, ConfigEntryBuilder builder) {
        return builder.startColorField(this.getTitle(id), this.getValue())
                      .setDefaultValue(this.getDefaultValue())
                      .setSaveConsumer(this::setValue)
                      .setTooltip(this.getTooltip(id))
                      .build();
    }
}
