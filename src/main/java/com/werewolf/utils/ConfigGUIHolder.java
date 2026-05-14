package com.werewolf.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** 設定GUI用のInventoryHolder。表示中のページ番号を保持する。 */
public class ConfigGUIHolder implements InventoryHolder {

    private final int page;
    private Inventory inventory;

    public ConfigGUIHolder(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
