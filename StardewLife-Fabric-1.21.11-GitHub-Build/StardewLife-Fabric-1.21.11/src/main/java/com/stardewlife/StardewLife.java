package com.stardewlife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class StardewLife implements ModInitializer {
    public static final String MOD_ID = "stardewlife";
    private static final String COINS = "life_coins";
    private static final String ENERGY = "life_energy";
    private static final String FARM_XP = "life_farmxp";
    private static final String FARM_LVL = "life_farmlvl";
    private static final String PENDING = "life_pending";
    private static final String SHIP_DAY = "life_shipday";

    private record ShopItem(String itemId, int price, String label, String season) {}
    private static final Map<String, ShopItem> SHOP = new LinkedHashMap<>();
    private static final Map<String, Integer> SELL = new LinkedHashMap<>();
    private static final String[] SEASONS = {"Spring", "Summer", "Fall", "Winter"};
    private static final String[] VILLAGERS = {"Abigail", "Alex", "Elliott", "Emily", "Haley", "Harvey",
            "Leah", "Maru", "Penny", "Sam", "Sebastian", "Shane"};

    static {
        SHOP.put("parsnip_seeds", new ShopItem("minecraft:wheat_seeds", 8, "Bibit Parsnip (pakai wheat seeds)", "Spring"));
        SHOP.put("carrot", new ShopItem("minecraft:carrot", 12, "Wortel", "Spring"));
        SHOP.put("potato", new ShopItem("minecraft:potato", 14, "Kentang", "Spring"));
        SHOP.put("beetroot_seeds", new ShopItem("minecraft:beetroot_seeds", 10, "Bibit Bit", "Spring"));
        SHOP.put("pumpkin_seeds", new ShopItem("minecraft:pumpkin_seeds", 18, "Bibit Labu", "Fall"));
        SHOP.put("melon_seeds", new ShopItem("minecraft:melon_seeds", 16, "Bibit Melon", "Summer"));
        SHOP.put("wheat_seeds", new ShopItem("minecraft:wheat_seeds", 6, "Bibit Gandum", "All"));
        SHOP.put("sweet_berries", new ShopItem("minecraft:sweet_berries", 20, "Sweet Berries", "Fall"));
        SELL.put("minecraft:wheat", 5);
        SELL.put("minecraft:carrot", 12);
        SELL.put("minecraft:potato", 10);
        SELL.put("minecraft:beetroot", 8);
        SELL.put("minecraft:pumpkin", 30);
        SELL.put("minecraft:melon", 18);
        SELL.put("minecraft:sweet_berries", 6);
        SELL.put("minecraft:apple", 8);
        SELL.put("minecraft:bread", 10);
        SELL.put("minecraft:golden_carrot", 45);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("life")
                .executes(ctx -> help(ctx.getSource()))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("calendar").executes(ctx -> calendar(ctx.getSource())))
                .then(Commands.literal("season").executes(ctx -> season(ctx.getSource())))
                .then(Commands.literal("clock").executes(ctx -> clock(ctx.getSource())))
                .then(Commands.literal("wallet").executes(ctx -> wallet(ctx.getSource())))
                .then(Commands.literal("energy").executes(ctx -> energy(ctx.getSource())))
                .then(Commands.literal("farmlevel").executes(ctx -> farmLevel(ctx.getSource())))
                .then(Commands.literal("farmxp")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000))
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> addFarmXp(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))))
                .then(Commands.literal("shop")
                    .then(Commands.literal("list").executes(ctx -> shopList(ctx.getSource())))
                    .then(Commands.literal("buy")
                        .then(Commands.argument("item", StringArgumentType.word())
                            .suggests((ctx, builder) -> { SHOP.keySet().forEach(builder::suggest); return builder.buildFuture(); })
                            .executes(ctx -> buy(ctx.getSource(), StringArgumentType.getString(ctx, "item"), 1))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> buy(ctx.getSource(), StringArgumentType.getString(ctx, "item"),
                                        IntegerArgumentType.getInteger(ctx, "amount")))))))
                .then(Commands.literal("sellhand").executes(ctx -> sellHand(ctx.getSource())))
                .then(Commands.literal("shiphand").executes(ctx -> shipHand(ctx.getSource())))
                .then(Commands.literal("shipping")
                    .then(Commands.literal("collect").executes(ctx -> collectShipping(ctx.getSource())))
                    .then(Commands.literal("status").executes(ctx -> shippingStatus(ctx.getSource()))))
                .then(Commands.literal("gift")
                    .then(Commands.argument("villager", StringArgumentType.word())
                        .suggests((ctx, builder) -> { for (String v : VILLAGERS) builder.suggest(v); return builder.buildFuture(); })
                        .executes(ctx -> gift(ctx.getSource(), StringArgumentType.getString(ctx, "villager")))))
                .then(Commands.literal("friendship")
                    .then(Commands.argument("villager", StringArgumentType.word())
                        .suggests((ctx, builder) -> { for (String v : VILLAGERS) builder.suggest(v); return builder.buildFuture(); })
                        .executes(ctx -> friendship(ctx.getSource(), StringArgumentType.getString(ctx, "villager")))))
                .then(Commands.literal("hello")
                    .then(Commands.argument("villager", StringArgumentType.word())
                        .executes(ctx -> hello(ctx.getSource(), StringArgumentType.getString(ctx, "villager")))))
            )
        );
    }

    private static int help(CommandSourceStack s) {
        s.sendSuccess(() -> Component.literal(
            "§6§lSTARDEW LIFE §8| §fFITUR & PERINTAH\\n" +
            "§e/life status §7- tanggal, musim, tahun, jam\\n" +
            "§e/life calendar §7- kalender 4 musim (28 hari/musim)\\n" +
            "§e/life wallet §7- saldo koin\\n" +
            "§e/life shop list §7- katalog toko sesuai musim\\n" +
            "§e/life shop buy <item> [jumlah] §7- belanja\\n" +
            "§e/life sellhand §7- jual item di tangan\\n" +
            "§e/life shiphand §7- masukkan item ke Shipping Bin\\n" +
            "§e/life shipping status|collect §7- cek/ambil hasil kiriman\\n" +
            "§e/life energy §7- energi pemain\\n" +
            "§e/life farmlevel §7- level farming & XP\\n" +
            "§e/life friendship <villager> §7- cek hubungan\\n" +
            "§e/life gift <villager> §7- beri hadiah dari tangan\\n" +
            "§e/life hello <villager> §7- sapa villager\\n" +
            "§8Catatan: fitur ini adalah prototype gameplay berbasis command."), false);
        return 1;
    }

    private static long worldDay(CommandSourceStack s) {
        return Math.max(0, s.getLevel().getDayTime() / 24000L);
    }
    private static int dayOfYear(CommandSourceStack s) { return (int)(worldDay(s) % 112); }
    private static String currentSeason(CommandSourceStack s) { return SEASONS[dayOfYear(s) / 28]; }
    private static int dayInSeason(CommandSourceStack s) { return dayOfYear(s) % 28 + 1; }
    private static String formatTime(long dayTime) {
        long t = Math.floorMod(dayTime, 24000L);
        int h = (int)((t / 1000L + 6L) % 24L);
        int m = (int)((t % 1000L) * 60L / 1000L);
        return String.format(Locale.ROOT, "%02d:%02d", h, m);
    }
    private static int status(CommandSourceStack s) {
        s.sendSuccess(() -> Component.literal("§6§lSTARDEW LIFE §8— §f" + currentSeason(s) + " " +
            dayInSeason(s) + " §7| Tahun " + (worldDay(s)/112+1) + " §7| " + formatTime(s.getLevel().getDayTime())), false);
        return 1;
    }
    private static int season(CommandSourceStack s) {
        s.sendSuccess(() -> Component.literal("§aMusim sekarang: §f" + currentSeason(s) +
            " §7(" + dayInSeason(s) + "/28)"), false); return 1;
    }
    private static int calendar(CommandSourceStack s) {
        String season = currentSeason(s);
        int d = dayInSeason(s);
        s.sendSuccess(() -> Component.literal("§6§lKALENDER §7— Tahun " + (worldDay(s)/112+1) +
            "\\n§f" + season + " §7| Hari " + d + "/28" +
            "\\n§7Spring 1-28 → Summer 1-28 → Fall 1-28 → Winter 1-28" +
            "\\n§8Besok: " + (d == 28 ? SEASONS[(dayOfYear(s)/28+1)%4] + " 1" : season + " " + (d+1))), false);
        return 1;
    }
    private static int clock(CommandSourceStack s) {
        s.sendSuccess(() -> Component.literal("§eJam dunia: §f" + formatTime(s.getLevel().getDayTime())), false); return 1;
    }

    private static void ensure(CommandSourceStack s, String objective) {
        run(s, "scoreboard objectives add " + objective + " dummy");
    }
    private static void ensureAll(CommandSourceStack s) {
        ensure(s, COINS); ensure(s, ENERGY); ensure(s, FARM_XP); ensure(s, FARM_LVL);
        ensure(s, PENDING); ensure(s, SHIP_DAY);
    }
    private static int getScore(CommandSourceStack s, ServerPlayer p, String obj) {
        try {
            var o = s.getServer().getScoreboard().getObjective(obj);
            if (o == null) return 0;
            return s.getServer().getScoreboard().getOrCreatePlayerScore(p, o).get();
        } catch (Exception e) { return 0; }
    }
    private static void setScore(CommandSourceStack s, ServerPlayer p, String obj, int value) {
        run(s, "scoreboard players set " + safeName(p) + " " + obj + " " + Math.max(0, value));
    }
    private static String safeName(ServerPlayer p) { return p.getGameProfile().getName().replaceAll("[^A-Za-z0-9_]", "_"); }
    private static void run(CommandSourceStack s, String command) {
        try { s.getServer().getCommands().performPrefixedCommand(s, command); } catch (Exception ignored) {}
    }
    private static int wallet(CommandSourceStack s) {
        ServerPlayer p=s.getPlayer(); ensureAll(s); int bal=getScore(s,p,COINS);
        s.sendSuccess(() -> Component.literal("§6Dompet: §e" + bal + "g"), false); return 1;
    }
    private static int energy(CommandSourceStack s) {
        ServerPlayer p=s.getPlayer(); ensureAll(s);
        int e=getScore(s,p,ENERGY);
        if (e==0) { e=100; setScore(s,p,ENERGY,e); }
        int shown=e;
        s.sendSuccess(() -> Component.literal("§bEnergi: §f" + shown + "/100 §7| Tidur di tempat tidur untuk memulihkan energi (prototype: gunakan /life energy)"), false);
        return 1;
    }
    private static int farmLevel(CommandSourceStack s) {
        ServerPlayer p=s.getPlayer(); ensureAll(s);
        int xp=getScore(s,p,FARM_XP), lvl=getScore(s,p,FARM_LVL);
        s.sendSuccess(() -> Component.literal("§aFarming Lv." + lvl + " §7| XP: " + xp + "/" + ((lvl+1)*100)), false); return 1;
    }
    private static int addFarmXp(CommandSourceStack s, int amount) {
        ServerPlayer p=s.getPlayer(); ensureAll(s);
        int xp=getScore(s,p,FARM_XP)+amount, lvl=getScore(s,p,FARM_LVL);
        while (xp >= (lvl+1)*100 && lvl < 10) { xp -= (lvl+1)*100; lvl++; }
        setScore(s,p,FARM_XP,xp); setScore(s,p,FARM_LVL,lvl);
        s.sendSuccess(() -> Component.literal("§a+" + amount + " Farming XP §7| Level sekarang: " + lvl), false); return 1;
    }
    private static int shopList(CommandSourceStack s) {
        String season=currentSeason(s);
        StringBuilder b=new StringBuilder("§6§lTOKO STARDEW §7— Musim ").append(season).append("\\n");
        for (var e:SHOP.entrySet()) {
            ShopItem i=e.getValue();
            if (i.season().equals("All") || i.season().equals(season))
                b.append("§e").append(e.getKey()).append(" §f- ").append(i.price()).append("g §7(").append(i.label()).append(")\\n");
        }
        b.append("§8Contoh: /life shop buy potato 4");
        s.sendSuccess(() -> Component.literal(b.toString()), false); return 1;
    }
    private static int buy(CommandSourceStack s, String key, int amount) {
        ServerPlayer p=s.getPlayer(); ensureAll(s);
        ShopItem item=SHOP.get(key.toLowerCase(Locale.ROOT));
        if(item==null){s.sendFailure(Component.literal("Barang tidak ditemukan. Gunakan /life shop list."));return 0;}
        if(!item.season().equals("All") && !item.season().equals(currentSeason(s))){
            s.sendFailure(Component.literal("Barang ini hanya dijual saat musim " + item.season() + ".")); return 0;
        }
        int total=item.price()*amount;
        if(getScore(s,p,COINS)<total){s.sendFailure(Component.literal("Uang tidak cukup. Butuh " + total + "g."));return 0;}
        run(s,"scoreboard players remove " + safeName(p) + " " + COINS + " " + total);
        run(s,"give " + safeName(p) + " " + item.itemId() + " " + amount);
        s.sendSuccess(() -> Component.literal("§aBerhasil membeli " + amount + "x " + item.label() + " §7seharga §e" + total + "g"), false);
        return 1;
    }
    private static int sellHand(CommandSourceStack s){return sell(s,false);}
    private static int shipHand(CommandSourceStack s){return sell(s,true);}
    private static int sell(CommandSourceStack s, boolean ship) {
        ServerPlayer p=s.getPlayer(); ItemStack stack=p.getMainHandItem();
        if(stack.isEmpty()){s.sendFailure(Component.literal("Pegang item yang ingin dijual di tangan utama."));return 0;}
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int price=SELL.getOrDefault(id,0), count=stack.getCount();
        if(price<=0){s.sendFailure(Component.literal("Item ini belum memiliki harga jual."));return 0;}
        ensureAll(s);
        int total=price*count;
        if(ship){
            int pending=getScore(s,p,PENDING)+total;
            setScore(s,p,PENDING,pending);
            setScore(s,p,SHIP_DAY,(int)worldDay(s)+1);
            s.sendSuccess(() -> Component.literal("§bShipping Bin: " + count + "x " + id + " masuk antrean. §7Hasil " + total + "g bisa diambil setelah hari berganti dengan /life shipping collect."),false);
        } else {
            run(s,"scoreboard players add " + safeName(p) + " " + COINS + " " + total);
            s.sendSuccess(() -> Component.literal("§aTerjual: " + count + "x " + id + " §7→ §e+" + total + "g"),false);
        }
        stack.shrink(count);
        return 1;
    }
    private static int shippingStatus(CommandSourceStack s) {
        ServerPlayer p=s.getPlayer(); ensureAll(s);
        int pending=getScore(s,p,PENDING), due=getScore(s,p,SHIP_DAY);
        s.sendSuccess(() -> Component.literal("§bShipping Bin §7| Saldo tertunda: §e" + pending + "g §7| Hari pencairan: " + (due==0?"belum ada":due)),false);
        return 1;
    }
    private static int collectShipping(CommandSourceStack s) {
        ServerPlayer p=s.getPlayer(); ensureAll(s);
        int pending=getScore(s,p,PENDING), due=getScore(s,p,SHIP_DAY);
        if(pending<=0){s.sendFailure(Component.literal("Shipping Bin masih kosong."));return 0;}
        if(worldDay(s)<due){s.sendFailure(Component.literal("Hasil Shipping Bin baru bisa diambil besok setelah hari berganti."));return 0;}
        run(s,"scoreboard players add " + safeName(p) + " " + COINS + " " + pending);
        setScore(s,p,PENDING,0); setScore(s,p,SHIP_DAY,0);
        s.sendSuccess(() -> Component.literal("§aHasil Shipping Bin cair: §e+" + pending + "g"),false); return 1;
    }
    private static String friendshipObjective(String name) { return "life_" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]",""); }
    private static String normalizeVillager(String raw) {
        for(String v:VILLAGERS) if(v.equalsIgnoreCase(raw)) return v;
        return null;
    }
    private static int friendship(CommandSourceStack s,String raw) {
        ServerPlayer p=s.getPlayer(); String v=normalizeVillager(raw);
        if(v==null){s.sendFailure(Component.literal("Nama villager tidak dikenal. Coba: Abigail, Alex, Leah, Sebastian, dll."));return 0;}
        String obj=friendshipObjective(v); ensure(s,obj);
        int points=getScore(s,p,obj);
        s.sendSuccess(() -> Component.literal("§dHubungan dengan " + v + ": §f" + points + "/1000 §7(" + (points>=800?"Sangat dekat":points>=500?"Teman baik":points>=200?"Mulai akrab":"Baru kenal") + ")"),false);
        return 1;
    }
    private static int gift(CommandSourceStack s,String raw) {
        ServerPlayer p=s.getPlayer(); String v=normalizeVillager(raw);
        if(v==null){s.sendFailure(Component.literal("Nama villager tidak dikenal."));return 0;}
        ItemStack stack=p.getMainHandItem();
        if(stack.isEmpty()){s.sendFailure(Component.literal("Pegang item hadiah di tangan utama."));return 0;}
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int points=id.contains("flower")||id.contains("cake")?80:id.contains("diamond")?100:id.contains("bread")||id.contains("apple")?35:15;
        String obj=friendshipObjective(v); ensure(s,obj);
        int current=getScore(s,p,obj), next=Math.min(1000,current+points);
        setScore(s,p,obj,next); stack.shrink(1);
        s.sendSuccess(() -> Component.literal("§dKamu memberi " + id + " kepada " + v + ". §a+" + points + " friendship §7(" + next + "/1000)"),false);
        return 1;
    }
    private static int hello(CommandSourceStack s,String raw) {
        String v=normalizeVillager(raw);
        if(v==null){s.sendFailure(Component.literal("Nama villager tidak dikenal."));return 0;}
        ServerPlayer p=s.getPlayer(); String obj=friendshipObjective(v); ensure(s,obj);
        int next=Math.min(1000,getScore(s,p,obj)+5); setScore(s,p,obj,next);
        s.sendSuccess(() -> Component.literal("§d" + v + "§f: Halo! Senang bertemu denganmu. §7(+5 friendship, total " + next + "/1000)"),false);
        return 1;
    }
}
