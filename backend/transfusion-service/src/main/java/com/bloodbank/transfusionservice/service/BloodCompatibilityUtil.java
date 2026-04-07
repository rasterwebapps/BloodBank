package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.model.enums.BloodGroupEnum;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class BloodCompatibilityUtil {

    private BloodCompatibilityUtil() {}

    private static final Map<BloodGroupEnum, Set<BloodGroupEnum>> COMPATIBILITY_MAP;

    static {
        COMPATIBILITY_MAP = new EnumMap<>(BloodGroupEnum.class);
        COMPATIBILITY_MAP.put(BloodGroupEnum.O_NEGATIVE,
                EnumSet.of(BloodGroupEnum.O_NEGATIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.O_POSITIVE,
                EnumSet.of(BloodGroupEnum.O_NEGATIVE, BloodGroupEnum.O_POSITIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.A_NEGATIVE,
                EnumSet.of(BloodGroupEnum.A_NEGATIVE, BloodGroupEnum.O_NEGATIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.A_POSITIVE,
                EnumSet.of(BloodGroupEnum.A_POSITIVE, BloodGroupEnum.A_NEGATIVE,
                        BloodGroupEnum.O_POSITIVE, BloodGroupEnum.O_NEGATIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.B_NEGATIVE,
                EnumSet.of(BloodGroupEnum.B_NEGATIVE, BloodGroupEnum.O_NEGATIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.B_POSITIVE,
                EnumSet.of(BloodGroupEnum.B_POSITIVE, BloodGroupEnum.B_NEGATIVE,
                        BloodGroupEnum.O_POSITIVE, BloodGroupEnum.O_NEGATIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.AB_NEGATIVE,
                EnumSet.of(BloodGroupEnum.AB_NEGATIVE, BloodGroupEnum.A_NEGATIVE,
                        BloodGroupEnum.B_NEGATIVE, BloodGroupEnum.O_NEGATIVE));
        COMPATIBILITY_MAP.put(BloodGroupEnum.AB_POSITIVE,
                EnumSet.allOf(BloodGroupEnum.class));
    }

    public static boolean isCompatible(BloodGroupEnum patientBloodGroup, BloodGroupEnum donorBloodGroup) {
        Set<BloodGroupEnum> compatibleGroups = COMPATIBILITY_MAP.get(patientBloodGroup);
        return compatibleGroups != null && compatibleGroups.contains(donorBloodGroup);
    }

    public static Set<BloodGroupEnum> getCompatibleDonorGroups(BloodGroupEnum patientBloodGroup) {
        return COMPATIBILITY_MAP.getOrDefault(patientBloodGroup, EnumSet.noneOf(BloodGroupEnum.class));
    }
}
