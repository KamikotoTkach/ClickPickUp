package tkachgeek.clickpickup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.WeakHashMap;

public final class ClickPickUp extends JavaPlugin implements Listener {
  WeakHashMap<LivingEntity, Item> pickupQueries = new WeakHashMap<>();
  
  @EventHandler
  void onItemPickup(EntityPickupItemEvent event) {
    if (pickupQueries.containsKey(event.getEntity())
       && pickupQueries.get(event.getEntity()) != null
       && pickupQueries.get(event.getEntity()).equals(event.getItem())) {
      pickupQueries.remove(event.getEntity());
    } else {
      event.setCancelled(true);
    }
  }
  
  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);
  }
  
  @EventHandler
  void onItemSpawn(ItemSpawnEvent event) {
    event.getEntity().setCanPlayerPickup(false);
  }
  
  @EventHandler
  void onCLick(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK
       && event.getItem() == null
       && event.getInteractionPoint() != null
       && !event.getClickedBlock().getType().isInteractable()) {
      Location loc = event.getInteractionPoint();
      Comparator<Entity> comparator = Comparator.comparingInt((Entity a) -> getLocationOffset(a.getLocation(), loc));
      loc.getNearbyEntitiesByType(Item.class, 0.2).stream().min(comparator).ifPresent(x -> {
        setPickupAllowed(x, event.getPlayer());
      });
    }
  }
  
  int getLocationOffset(Location a, Location b) {
    return (int) (Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2) + Math.pow(a.getZ() - b.getZ(), 2));
  }
  
  void setPickupAllowed(Item item, Player player) {
    pickupQueries.put(player, item);
    item.setCanPlayerPickup(true);
    Bukkit.getScheduler().runTaskLater(this, () -> item.setCanPlayerPickup(false), 4);
  }
}
