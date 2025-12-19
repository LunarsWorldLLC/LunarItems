package me.dunescifye.lunaritems.listeners;

import me.dunescifye.lunaritems.LunarItems;
import me.dunescifye.lunaritems.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerCollectItemListener implements Listener {

  public static final NamespacedKey keyVoidMaterial = new NamespacedKey("score", "score-void_material");
  public static final NamespacedKey keyVoidMaterial2 = new NamespacedKey("score", "score-void_material2");
  public static final NamespacedKey keyVoidMaterial3 = new NamespacedKey("score", "score-void_material3");

  @EventHandler
  public void onPlayerCollectItem(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player p)) return;

    PlayerInventory inv = p.getInventory();

    Item pickupItem = e.getItem();
    ItemStack pickupStack = pickupItem.getItemStack();
    Material pickupMat = pickupStack.getType();

    for (int i = 0; i < 9; i++) { // why we iterate through whole inv?
      ItemStack invItem = inv.getItem(i);

      if (invItem == null || invItem.isEmpty()) continue;

      ItemMeta meta = invItem.getItemMeta();
      if (meta == null) continue; // actually im not yet sure whether this produces same output as hasItemMeta

      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      String itemID = pdc.get(LunarItems.keyEIID, PersistentDataType.STRING);
      if (itemID == null || !itemID.contains("sunblackhole")) continue;

      String mat = pdc.get(keyVoidMaterial, PersistentDataType.STRING);
      String mat2 = pdc.get(keyVoidMaterial2, PersistentDataType.STRING);
      String mat3 = pdc.get(keyVoidMaterial3, PersistentDataType.STRING);

      if (matches(mat, pickupMat) || matches(mat2, pickupMat) || matches(mat3, pickupMat)) {
        e.setCancelled(true);
        pickupItem.remove();
      }

    }

    // TODO: maybe moving the result check before the item meta data is better to avoid doing meta data copying too often
    ItemStack offhand = inv.getItemInOffHand();
    if (offhand.isEmpty()) return;

    ItemMeta meta = offhand.getItemMeta();
    if (meta == null) return;

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    String itemID = pdc.get(LunarItems.keyEIID, PersistentDataType.STRING);
    if (itemID == null || !itemID.contains("autumnsmoker"))  return;

    Material result = Utils.rawOres.get(pickupMat); // no need for containsKey check since this will return null if not in map
    if (result == null) result = Utils.smeltedFoods.get(pickupMat); // falls back if its not in map
    if (result == null) return; // if both not in map then we dont continue

    inv.addItem(new ItemStack(result, pickupStack.getAmount()));
    e.setCancelled(true);
    pickupItem.remove();

    Utils.runConsoleCommands("ei console-modification modification variable " + p.getName() + " 40 cookedfood " + pickupStack.getAmount());
  }

  private boolean matches(String materialName, Material pickupMat) {
    if (materialName == null) return false;
    Material mat = Material.matchMaterial(materialName);
    return mat == pickupMat;
  }
}
