package net.mattwhyy.itemcooldowns.features.cooldown;

import java.util.List;

public final class CooldownMatcher {
    private CooldownMatcher() {
    }

    public record Probe(String material, String potion, Integer cmd, String itemModel) {
    }

    public record Filter(String material, String potion, Integer cmd, String itemModel) {
    }

    public static int score(Filter rule, Probe item) {
        if (rule == null || item == null || rule.material == null || item.material == null) {
            return -1;
        }
        if (!rule.material.equals(item.material)) {
            return -1;
        }
        int score = 1;
        if (rule.potion != null) {
            if (!rule.potion.equals(item.potion)) {
                return -1;
            }
            score++;
        }
        if (rule.cmd != null) {
            if (!rule.cmd.equals(item.cmd)) {
                return -1;
            }
            score++;
        }
        if (rule.itemModel != null) {
            if (!rule.itemModel.equals(item.itemModel)) {
                return -1;
            }
            score++;
        }
        return score;
    }

    public static int scoreAny(List<Filter> options, Probe item) {
        int best = -1;
        if (options == null) {
            return -1;
        }
        for (Filter option : options) {
            int scored = score(option, item);
            if (scored > best) {
                best = scored;
            }
        }
        return best;
    }

    public static int bestIndex(List<Filter> rules, Probe item) {
        int best = -1;
        int bestScore = -1;
        if (rules == null) {
            return -1;
        }
        for (int i = 0; i < rules.size(); i++) {
            int scored = score(rules.get(i), item);
            if (scored > bestScore) {
                bestScore = scored;
                best = i;
            }
        }
        return best;
    }

    public static boolean sameFilter(Filter left, Filter right) {
        if (left == null || right == null) {
            return false;
        }
        return java.util.Objects.equals(left.material(), right.material())
                && java.util.Objects.equals(left.potion(), right.potion())
                && java.util.Objects.equals(left.cmd(), right.cmd())
                && java.util.Objects.equals(left.itemModel(), right.itemModel());
    }

    public static void main(String[] args) {
        Filter allMaces = new Filter("MACE", null, null, null);
        Filter cmdMace = new Filter("MACE", null, 12, null);
        Filter otherCmd = new Filter("MACE", null, 99, null);
        Filter modelMace = new Filter("MACE", null, null, "pack:special");
        Filter swift = new Filter("POTION", "SWIFTNESS", null, null);
        Filter allPotions = new Filter("POTION", null, null, null);
        Probe vanillaMace = new Probe("MACE", null, null, null);
        Probe specialMace = new Probe("MACE", null, 12, "pack:special");
        Probe otherMace = new Probe("MACE", null, 99, null);
        Probe swiftness = new Probe("POTION", "SWIFTNESS", null, null);
        Probe healing = new Probe("POTION", "HEALING", null, null);

        assert score(allMaces, vanillaMace) == 1;
        assert score(allMaces, specialMace) == 1;
        assert score(cmdMace, specialMace) == 2;
        assert score(cmdMace, vanillaMace) == -1;
        assert score(cmdMace, otherMace) == -1;
        assert score(otherCmd, specialMace) == -1;
        assert score(modelMace, specialMace) == 2;
        assert score(modelMace, vanillaMace) == -1;
        assert score(swift, swiftness) == 2;
        assert score(swift, healing) == -1;
        assert score(allPotions, swiftness) == 1;
        assert score(allPotions, healing) == 1;
        assert score(allMaces, swiftness) == -1;

        List<Filter> rules = List.of(allMaces, cmdMace, swift);
        assert bestIndex(rules, vanillaMace) == 0;
        assert bestIndex(rules, specialMace) == 1;
        assert bestIndex(rules, swiftness) == 2;
        assert bestIndex(rules, healing) == -1;
        assert bestIndex(List.of(), vanillaMace) == -1;
        assert sameFilter(cmdMace, new Filter("MACE", null, 12, null));
        assert !sameFilter(cmdMace, allMaces);
        assert !sameFilter(cmdMace, otherCmd);
        List<Filter> spears = List.of(new Filter("WOODEN_SPEAR", null, null, null), new Filter("IRON_SPEAR", null, null, null));
        assert scoreAny(spears, new Probe("IRON_SPEAR", null, null, null)) == 1;
        assert scoreAny(spears, vanillaMace) == -1;
        assert scoreAny(List.of(), vanillaMace) == -1;
    }
}
