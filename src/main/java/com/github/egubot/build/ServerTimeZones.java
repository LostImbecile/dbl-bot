package com.github.egubot.build;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.egubot.shared.utils.DateUtils;
import com.github.egubot.storage.DataManagerHandler;

public class ServerTimeZones extends DataManagerHandler {
    private Set<String> zones;

    public ServerTimeZones(long serverID) {
        super(serverID + File.separator + "Time_Zones", true);
    }

    private Set<String> getZones() {
        if (zones == null) {
            zones = Collections.synchronizedSet(new LinkedHashSet<>());
        }
        return zones;
    }

    public boolean addZone(String input) {
        return addZone(input, null);
    }

    public boolean addZone(String input, String customName) {
        if (input == null || input.isBlank()) return false;
        String zoneId = toZoneId(input.trim());
        if (zoneId == null) return false;
        
        String name = customName != null && !customName.isBlank() ? customName.trim() : null;
        String entry = name != null ? name + ":" + zoneId : zoneId;
        
        synchronized (getZones()) {
            boolean added = getZones().add(entry);
            if (added) writeData(null);
            return added;
        }
    }

    public boolean removeZone(String input) {
        if (input == null || input.isBlank()) return false;
        String trimmed = input.trim();
        
        synchronized (getZones()) {
            Set<String> z = getZones();
            try {
                int idx = Integer.parseInt(trimmed);
                if (idx <= 0 || idx > z.size()) return false;
                String target = new ArrayList<>(z).get(idx - 1);
                boolean removed = z.remove(target);
                if (removed) writeData(null);
                return removed;
            } catch (NumberFormatException ignore) {
            }
            
            String toRemove = null;
            for (String entry : z) {
                String[] parts = entry.split(":", 2);
                String storedZoneId = parts.length == 2 ? parts[1] : parts[0];
                String storedName = parts.length == 2 ? parts[0] : null;
                
                if (entry.equals(trimmed) || storedZoneId.equalsIgnoreCase(trimmed) || 
                    (storedName != null && storedName.equalsIgnoreCase(trimmed))) {
                    toRemove = entry;
                    break;
                }
            }
            
            if (toRemove != null) {
                boolean removed = z.remove(toRemove);
                if (removed) writeData(null);
                return removed;
            }
            return false;
        }
    }

    public List<String> listZones() {
        synchronized (getZones()) {
            return new ArrayList<>(getZones());
        }
    }

    public String formatCurrentTimes() {
        List<String> zonesList;
        synchronized (getZones()) {
            if (getZones().isEmpty()) return null;
            zonesList = new ArrayList<>(getZones());
        }
        
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (String entry : zonesList) {
            try {
                String[] parts = entry.split(":", 2);
                String zoneId = parts.length == 2 ? parts[1] : parts[0];
                String displayName = parts.length == 2 ? parts[0] : zoneId;
                
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
                sb.append(i++).append(") ").append(displayName).append(" — ")
                  .append(DateUtils.getDateTimeWithZone(now)).append("\n");
            } catch (Exception e) {
            }
        }
        return sb.toString().isBlank() ? null : sb.toString();
    }

    private String toZoneId(String input) {
        if (input == null) return null;
        String z = input.trim();
        String upperZ = z.toUpperCase(java.util.Locale.ROOT);
        
        if (upperZ.equals("BST") || upperZ.equals("GMT")) {
            return "Europe/London";
        }
        if (upperZ.equals("EST") || upperZ.equals("EDT") || upperZ.equals("ET")) {
            return "America/New_York";
        }
        if (upperZ.equals("PST") || upperZ.equals("PDT") || upperZ.equals("PT")) {
            return "America/Los_Angeles";
        }
        if (upperZ.equals("CST") || upperZ.equals("CDT") || upperZ.equals("CT")) {
            return "America/Chicago";
        }
        if (upperZ.equals("MST") || upperZ.equals("MDT") || upperZ.equals("MT")) {
            return "America/Denver";
        }
        if (upperZ.equals("JST")) {
            return "Asia/Tokyo";
        }
        if (upperZ.equals("AEST") || upperZ.equals("AEDT")) {
            return "Australia/Sydney";
        }
        if (upperZ.equals("CET") || upperZ.equals("CEST")) {
            return "Europe/Paris";
        }
        if (upperZ.equals("IST")) {
            return "Asia/Kolkata";
        }
        if (upperZ.equals("KST")) {
            return "Asia/Seoul";
        }
        if (upperZ.equals("HKT")) {
            return "Asia/Hong_Kong";
        }
        if (upperZ.equals("SGT")) {
            return "Asia/Singapore";
        }
        if (upperZ.equals("NZST") || upperZ.equals("NZDT")) {
            return "Pacific/Auckland";
        }
        if (upperZ.equals("MSK")) {
            return "Europe/Moscow";
        }
        
        try {
            ZoneId id = ZoneId.of(z);
            return id.getId();
        } catch (Exception ignore) {
        }
        
        String[] allZoneIds = ZoneId.getAvailableZoneIds().toArray(new String[0]);
        for (String zoneId : allZoneIds) {
            if (zoneId.equalsIgnoreCase(z)) {
                return zoneId;
            }
        }
        
        if (z.matches("(?i)^(UTC|GMT)?[+-]?\\d{1,2}(:\\d{2})?$")) {
            String s = z.toUpperCase(java.util.Locale.ROOT).replace("GMT", "UTC");
            if (!s.startsWith("UTC")) s = "UTC" + (s.startsWith("+") || s.startsWith("-") ? "" : "+") + s;
            try {
                ZoneId id = ZoneId.of(s);
                return id.getId();
            } catch (Exception ignore) {
            }
        }
        
        return null;
    }

    @Override
    public void updateObjects() {
        synchronized (getZones()) {
            getZones().clear();
            for (String line : getData()) {
                String v = line == null ? null : line.trim();
                if (v == null || v.isBlank()) continue;
                getZones().add(v);
            }
        }
    }

    @Override
    public void updateDataFromObjects() {
        synchronized (getZones()) {
            setData(new ArrayList<>(getZones()));
        }
    }
}