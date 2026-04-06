package com.werewolf.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIHolder implements InventoryHolder {

    private final SelectionType type;
    private Inventory inventory;

    public GUIHolder(SelectionType type) {
        this.type = type;
    }

    public SelectionType getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
