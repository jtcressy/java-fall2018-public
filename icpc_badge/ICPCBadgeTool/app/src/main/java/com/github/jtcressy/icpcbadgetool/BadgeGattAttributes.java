package com.github.jtcressy.icpcbadgetool;

import java.util.HashMap;
import java.util.UUID;

public class BadgeGattAttributes {
    public static UUID SERVICE_USER_DATA = convertFromInteger(0x181C);
    public static UUID SERVICE_RGB_LED = UUID.fromString("6d7df50f-3732-458a-b9fe-929df18f12a8");
    public static UUID SERVICE_BADGE_MESSAGE = UUID.fromString("7f40c29a-b34a-4aca-b5d0-53606b6fe538");
    public static UUID CHARACTERISTIC_USER_DATA_FNAME = convertFromInteger(0x2A8A);
    public static UUID CHARACTERISTIC_USER_DATA_LNAME = convertFromInteger(0x2A90);
    public static UUID CHARACTERISTIC_RGB_LED_COLOR = UUID.fromString("8fb3bf3c-8448-455c-ae9e-93905b7bd41e");
    public static UUID CHARACTERISTIC_BADGE_MESSAGE = UUID.fromString("563d1394-3282-4262-88cb-677962b7e69a");
    private static HashMap<UUID, String> attributes = new HashMap<>();

    static {
        // attributes.put(UUID, "Description");
        //Services
        attributes.put(SERVICE_USER_DATA, "User Data Service");
        attributes.put(SERVICE_RGB_LED, "RGB LED Service");
        attributes.put(SERVICE_BADGE_MESSAGE, "Badge Message Service");
        //Characteristics
        attributes.put(CHARACTERISTIC_USER_DATA_FNAME, "First Name");
        attributes.put(CHARACTERISTIC_USER_DATA_LNAME, "Last Name");
        attributes.put(CHARACTERISTIC_RGB_LED_COLOR, "24-bit RGB value in hex");
        attributes.put(CHARACTERISTIC_BADGE_MESSAGE, "Badge Message");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(UUID.fromString(uuid));
        return name == null ? defaultName : name;
    }

    public static String lookup(int uuid, String defaultName) {
        String name = attributes.get(convertFromInteger(uuid));
        return name == null ? defaultName : name;
    }

    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }
}
