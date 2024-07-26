package com.github.egubot.objects;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Duration;
import java.util.List;

import com.github.egubot.shared.Shared;
import com.google.gson.annotations.SerializedName;

public class TimerObject {
    @SerializedName("task")
    private String task;
    @SerializedName("task_arguments")
    private String taskArguments;
    @SerializedName("target_channel")
    private List<Long> targetChannel;
    @SerializedName("activated_flag")
    private boolean activatedFlag;
    @SerializedName("next_execution")
    private String nextExecution; // MM:dd:HH:mm:ss format
    @SerializedName("exit_time")
    private String exitTime; // UK time on app exit
    @SerializedName("summer_time")
    private boolean summerTime;
    @SerializedName("recurring")
    private boolean recurring;
    @SerializedName("delay")
    private String delay; // 0M0w0d0h0m0s format
    @SerializedName("start_date")
    private String startDate; // null if not set
    @SerializedName("send_on_miss")
    private boolean sendOnMiss;
    @SerializedName("continue_on_miss")
    private boolean continueOnMiss;
    @SerializedName("terminate_on_miss")
    private boolean terminateOnMiss;
    @SerializedName("miss_tolerance")
    private String missTolerance; // 0M0w0d0h0m0s format or 0 if half the delay or less

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM:dd:HH:mm:ss");

    public boolean isActivatedFlag() {
        return activatedFlag;
    }

    public void setActivatedFlag(boolean activatedFlag) {
        this.activatedFlag = activatedFlag;
    }

    public void setNextExecution(String nextExecution) {
        if (isValidDateTime(nextExecution)) {
            this.nextExecution = nextExecution;
        } else {
            throw new IllegalArgumentException("Invalid nextExecution format: " + nextExecution);
        }
    }

    public void setExitTime(String exitTime) {
        if (isValidDateTime(exitTime)) {
            this.exitTime = exitTime;
        } else {
            throw new IllegalArgumentException("Invalid exitTime format: " + exitTime);
        }
    }

    public boolean isSummerTime() {
        return summerTime;
    }

    public void setSummerTime(boolean summerTime) {
        this.summerTime = summerTime;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public void setDelay(String delay) {
        if (isValidDelay(delay)) {
            this.delay = delay;
        } else {
            throw new IllegalArgumentException("Invalid delay format: " + delay);
        }
    }

    public void setStartDate(String startDate) {
        if (startDate == null || isValidDateTime(startDate)) {
            this.startDate = startDate;
        } else {
            throw new IllegalArgumentException("Invalid startDate format: " + startDate);
        }
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTaskArguments() {
        return taskArguments;
    }

    public void setTaskArguments(String taskArguments) {
        this.taskArguments = taskArguments;
    }

    public List<Long> getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(List<Long> targetChannel) {
        this.targetChannel = targetChannel;
    }

    public boolean isSendOnMiss() {
        return sendOnMiss;
    }

    public void setSendOnMiss(boolean sendOnMiss) {
        this.sendOnMiss = sendOnMiss;
    }

    public boolean isContinueOnMiss() {
        return continueOnMiss;
    }

    public void setContinueOnMiss(boolean continueOnMiss) {
        this.continueOnMiss = continueOnMiss;
    }

    public boolean isTerminateOnMiss() {
        return terminateOnMiss;
    }

    public void setTerminateOnMiss(boolean terminateOnMiss) {
        this.terminateOnMiss = terminateOnMiss;
    }

    public String getMissTolerance() {
        return missTolerance;
    }

    public void setMissTolerance(String missTolerance) {
        if (missTolerance.equals("0") || isValidDelay(missTolerance)) {
            this.missTolerance = missTolerance;
        } else {
            throw new IllegalArgumentException("Invalid missTolerance format: " + missTolerance);
        }
    }

    private boolean isValidDateTime(String dateTime) {
        try {
            LocalDateTime.parse(dateTime, dateFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidDelay(String delayString) {
        String[] parts = delayString.split("[Mwdhms]");
        if (parts.length != 6) {
            return false;
        }
        try {
            for (String part : parts) {
                Integer.parseInt(part);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Duration parseDelayString(String delayString) {
        String[] parts = delayString.split("[Mwdhms]");
        try {
            int months = Integer.parseInt(parts[0]);
            int weeks = Integer.parseInt(parts[1]);
            int days = Integer.parseInt(parts[2]);
            int hours = Integer.parseInt(parts[3]);
            int minutes = Integer.parseInt(parts[4]);
            int seconds = Integer.parseInt(parts[5]);
            return Duration.ofDays((long) months * 30 + weeks * 7 + days)
                    .plusHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid delay format: " + delayString, e);
        }
    }
    
    public Duration getMissToleranceDuration() {
        if (missTolerance.equals("0")) {
            return parseDelayString(delay).dividedBy(2);
        } else {
            return parseDelayString(missTolerance);
        }
    }

    public LocalDateTime getNextExecutionTime() {
        return parseTimeStringWithSummerTime(nextExecution);
    }

    public LocalDateTime getExitTimeAsDateTime() {
        return parseTimeStringWithSummerTime(exitTime);
    }

    public Duration getDelayDuration() {
        return parseDelayString(delay);
    }

    public LocalDateTime getStartDateTime() {
        return startDate == null ? null : parseTimeStringWithSummerTime(startDate);
    }

    private LocalDateTime parseTimeStringWithSummerTime(String timeString) {
        LocalDateTime localDateTime = parseTimeString(timeString);
        return applySummerTimeAdjustment(localDateTime);
    }

    private LocalDateTime parseTimeString(String timeString) {
        try {
            return LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("MM:dd:HH:mm:ss"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeString, e);
        }
    }

    private LocalDateTime applySummerTimeAdjustment(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(Shared.getTimeZone()));
        boolean isCurrentlySummerTime = zonedDateTime.getZone().getRules().isDaylightSavings(zonedDateTime.toInstant());
        if (summerTime && !isCurrentlySummerTime) {
            zonedDateTime = zonedDateTime.plusHours(1);
        } else if (!summerTime && isCurrentlySummerTime) {
            zonedDateTime = zonedDateTime.minusHours(1);
        }
        return zonedDateTime.toLocalDateTime();
    }

    public String formatTimeString(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("MM:dd:HH:mm:ss"));
    }

}
