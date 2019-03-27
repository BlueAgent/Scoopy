package scoopy.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemWithMetadata {
    public final Item item;
    public final int meta;

    public ItemWithMetadata(Item item, int meta) {
        this.item = item;
        this.meta = meta;
    }

    public ItemWithMetadata(ItemStack stack) {
        this(stack.getItem(), stack.getItemDamage());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemWithMetadata that = (ItemWithMetadata) o;

        if (meta != that.meta) return false;
        return item.equals(that.item);

    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + meta;
        return result;
    }

    public ItemStack getStack() {
        return new ItemStack(item, 1, meta);
    }
}
