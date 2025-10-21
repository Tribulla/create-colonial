package com.example.create_colonial.integration;

import com.example.create_colonial.util.ModUtils;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.IdentityHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = "create_colonial")
public class ClipboardIntegration {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Check if clipboard integration is enabled
        if (!ModUtils.isClipboardEnabled()) {
            return;
        }
        
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        
        // Check if player is holding Create's clipboard item by checking the registry name
        ItemStack heldItem = player.getItemInHand(hand);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(heldItem.getItem());
        if (itemId == null || !itemId.getNamespace().equals("create") || !itemId.getPath().equals("clipboard")) {
            return;
        }
        
        // Only run on server
        if (level.isClientSide) {
            return;
        }
        
        try {
            // Try to get the colony and building at this position
            IColony colony = com.minecolonies.api.colony.IColonyManager.getInstance().getColonyByPosFromWorld(level, pos);
            if (colony == null) {
                return;
            }
            
            IBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building == null) {
                return;
            }
            
            // Check if it's a builder's hut or has work orders
            String buildingType = building.getBuildingType().getRegistryName().getPath();
            if (buildingType.contains("builder")) {
                addResourcesToClipboard(player, building, colony);
                event.setCanceled(true);
            }
            
        } catch (Exception e) {
            // If MineColonies integration fails, just ignore
            ModUtils.debugLog("Failed to process clipboard interaction: {}", e.getMessage());
        }
    }
    
    private static void addResourcesToClipboard(Player player, IBuilding building, IColony colony) {
    Map<String, Integer> requiredResources = new HashMap<>();
    Map<ResourceLocation, ItemAggregate> itemsById = new HashMap<>();
    // For predicate resolution, gather example ItemStacks seen in requests
    List<ItemStack> observedStacks = new ArrayList<>();
        int requestCount = 0;

        // Prefer the official building API: getRequiredItemsAndAmount
        try {
            Map<java.util.function.Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> map = building.getRequiredItemsAndAmount();
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<java.util.function.Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> e : map.entrySet()) {
                    // We don't have a concrete ItemStack here; attempt to guess a name from predicate toString()
                    String key = e.getKey().toString();
                    int count = e.getValue().getA();
                    requiredResources.merge(key, count, Integer::sum);
                }
            }
        } catch (Throwable t) {
            ModUtils.debugLog("building.getRequiredItemsAndAmount failed: {}", t.getMessage());
        }

        // Also aggregate from open requests of type Stack for each assigned citizen
        try {
            Set<ICitizenData> assigned = building.getAllAssignedCitizen();
            TypeToken<Stack> stackType = TypeToken.of(Stack.class);
            for (ICitizenData citizen : assigned) {
                int citizenId = citizen.getId();
                List<IRequest<? extends Stack>> reqs = building.getOpenRequestsOfType(citizenId, stackType);
                for (IRequest<? extends Stack> r : reqs) {
                    Stack s = r.getRequest();
                    if (s != null) {
                        ItemStack stack = s.getStack();
                        int count = s.getCount();
                        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                        String name = rl != null ? rl.toString() : stack.getHoverName().getString();
                        int add = Math.max(count, stack.getCount());
                        requiredResources.merge(name, add, Integer::sum);
                        if (rl != null) {
                            itemsById.merge(rl, new ItemAggregate(stack.copy(), add), (a, b) -> {
                                a.count += b.count;
                                return a;
                            });
                        }
                        observedStacks.add(stack.copy());
                        requestCount++;
                    } else {
                        // Fallback to display stacks if request does not expose stack directly
                        for (ItemStack ds : r.getDisplayStacks()) {
                            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(ds.getItem());
                            String name = rl != null ? rl.toString() : ds.getHoverName().getString();
                            requiredResources.merge(name, ds.getCount(), Integer::sum);
                            if (rl != null) {
                                itemsById.merge(rl, new ItemAggregate(ds.copy(), ds.getCount()), (a, b) -> {
                                    a.count += b.count;
                                    return a;
                                });
                            }
                            observedStacks.add(ds.copy());
                            requestCount++;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            ModUtils.debugLog("Failed to read open requests: {}", t.getMessage());
        }

        if (!requiredResources.isEmpty()) {
            ModUtils.sendMessageToPlayer(player, "Builder needs:", ModUtils.MessageType.INFO);
            requiredResources.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(40)
                    .forEach(entry -> player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§7  - " + entry.getValue() + "x " + entry.getKey()
                    )));

            // Try to replace predicate-looking keys with actual items we observed
            // building.getRequiredItemsAndAmount returns predicates; attempt to bind to observed stacks
            try {
                Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> map = building.getRequiredItemsAndAmount();
                if (map != null && !map.isEmpty()) {
                    // First, try to bind to observed stacks
                    Map<Predicate<ItemStack>, ItemStack> predMatches = new IdentityHashMap<>();
                    for (Map.Entry<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> e : map.entrySet()) {
                        Predicate<ItemStack> pred = e.getKey();
                        ItemStack matched = null;
                        for (ItemStack s : observedStacks) {
                            if (pred.test(s)) { matched = s; break; }
                        }
                        predMatches.put(pred, matched);
                    }

                    // Fallback: scan registry for a representative item if none observed
                    for (Map.Entry<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> e : map.entrySet()) {
                        Predicate<ItemStack> pred = e.getKey();
                        if (predMatches.get(pred) == null) {
                            for (var entry : ForgeRegistries.ITEMS.getEntries()) {
                                net.minecraft.world.item.Item item = entry.getValue();
                                ItemStack candidate = new ItemStack(item);
                                try {
                                    if (!candidate.isEmpty() && pred.test(candidate)) {
                                        predMatches.put(pred, candidate);
                                        break;
                                    }
                                } catch (Throwable ignore) {
                                }
                            }
                        }
                    }

                    // Aggregate matches
                    for (Map.Entry<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> e : map.entrySet()) {
                        int count = e.getValue().getA();
                        ItemStack matched = predMatches.get(e.getKey());
                        if (matched != null && !matched.isEmpty()) {
                            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(matched.getItem());
                            if (rl != null) {
                                itemsById.merge(rl, new ItemAggregate(matched.copy(), count), (a, b) -> { a.count += b.count; return a; });
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                ModUtils.debugLog("Predicate resolution failed: {}", t.getMessage());
            }

            // Also write into Create's clipboard pages with icons when available
            try {
                ItemStack clipboard = player.getMainHandItem();
                // If the interaction was with offhand, prefer that stack
                if (!isCreateClipboard(clipboard)) {
                    clipboard = player.getOffhandItem();
                }
                if (isCreateClipboard(clipboard)) {
                    final int PAGE_SIZE = 12; // More conservative page size to prevent overflow in UI
                    List<List<ClipboardEntry>> pages = new ArrayList<>();
                    List<ClipboardEntry> current = new ArrayList<>();

                    // Build a sorted list of icon entries
                    List<Map.Entry<ResourceLocation, ItemAggregate>> iconEntries = new ArrayList<>(itemsById.entrySet());
                    iconEntries.sort(Comparator.comparingInt(e -> e.getValue().count));
                    java.util.Collections.reverse(iconEntries);

                    for (Map.Entry<ResourceLocation, ItemAggregate> e : iconEntries) {
                        ItemAggregate ag = e.getValue();
                        int cnt = ag.count;
                        ItemStack sample = ag.sample.copy();
                        sample.setCount(1);
                        net.minecraft.network.chat.MutableComponent line = net.minecraft.network.chat.Component.literal(cnt + "x ")
                                .append(sample.getHoverName());
                        ClipboardEntry ce = new ClipboardEntry(false, line).displayItem(sample);
                        current.add(ce);
                        if (current.size() >= PAGE_SIZE) {
                            pages.add(current);
                            current = new ArrayList<>();
                        }
                    }

                    // Skip unresolved predicate text-only lines to avoid lambda/predicate noise

                    if (!current.isEmpty()) pages.add(current);

                    if (!pages.isEmpty()) {
                        ClipboardEntry.saveAll(pages, clipboard);
                        player.getInventory().setChanged();
                    }
                }
            } catch (Throwable t) {
                ModUtils.debugLog("Failed writing to Create clipboard: {}", t.getMessage());
            }

            ModUtils.sendMessageToPlayer(player, "Resources added to clipboard!" + (requestCount > 0 ? " (" + requestCount + " requests)" : ""), ModUtils.MessageType.SUCCESS);
        } else {
            ModUtils.sendMessageToPlayer(player, "No outstanding item requests for this builder right now.", ModUtils.MessageType.WARNING);
        }
    }
    
    // removed obsolete work-order reflection helper
    
    // removed unused helpers

    private static boolean isCreateClipboard(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getNamespace().equals("create") && id.getPath().equals("clipboard");
    }

    private static class ItemAggregate {
        ItemStack sample;
        int count;
        ItemAggregate(ItemStack sample, int count) {
            this.sample = sample;
            this.count = count;
        }
    }
}
