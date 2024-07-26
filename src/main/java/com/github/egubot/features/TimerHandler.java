package com.github.egubot.features;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.github.egubot.objects.TimerObject;
import com.github.egubot.shared.Shared;

public class TimerHandler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<TimerObject> timers;

    public TimerHandler(List<TimerObject> timers) {
        this.timers = timers;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkTimers, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void checkTimers() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(Shared.getTimeZone()));

        for (TimerObject timer : timers) {
            if (!timer.isActivatedFlag() || timer.getNextExecutionTime() == null) {
                continue;
            }

            LocalDateTime nextExecution = timer.getNextExecutionTime();
            if (now.isAfter(nextExecution) || now.isEqual(nextExecution)) {
                handleMissedOrDueTimer(timer, now);
            }
        }
    }

    private void handleMissedOrDueTimer(TimerObject timer, LocalDateTime now) {
        Duration missTolerance = timer.getMissToleranceDuration();
        LocalDateTime nextExecution = timer.getNextExecutionTime();

        if (now.isAfter(nextExecution.plus(missTolerance))) {
            if (timer.isTerminateOnMiss()) {
                timers.remove(timer);
            }
            if (timer.isSendOnMiss()) {
                executeTask(timer);
                if (!timer.isRecurring()) {
                    timers.remove(timer);
                    return;
                }
            } else if (timer.isContinueOnMiss()) {
                Duration remainingTime = Duration.between(nextExecution, now);
                timer.setNextExecution(formatTimeString(nextExecution.plus(remainingTime)));
            }
        } else {
            executeTask(timer);
            if (!timer.isRecurring()) {
                timers.remove(timer);
                return;
            }
        }

        if (timer.isRecurring()) {
            updateNextExecution(timer);
        }
    }

    private void updateNextExecution(TimerObject timer) {
        LocalDateTime nextExecution = timer.getNextExecutionTime();
        Duration delay = timer.getDelayDuration();
        timer.setNextExecution(formatTimeString(nextExecution.plus(delay)));
    }

    private void executeTask(TimerObject timer) {
        // TODO: Implement task execution logic here based on timer.getTask() and timer.getTaskArguments()
        // Send message to target channels
        for (Long channelId : timer.getTargetChannel()) {
            // Send message to the channel
        }
        timer.setActivatedFlag(false);
    }

    private String formatTimeString(LocalDateTime time) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM:dd:HH:mm:ss");
        return time.format(dateFormatter);
    }
}
