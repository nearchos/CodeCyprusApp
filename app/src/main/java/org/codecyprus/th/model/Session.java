package org.codecyprus.th.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Session implements Serializable {

    private String uuid; // PK
    private String treasureHuntUuid; // FK
    private String playerName;
    private String appName;
    private Long startTime; // UTC unix timestamp
    private Long endTime; // UTC unix timestamp
    private Long score; // current score for player, initially 0
    private long completionTime; // UTC unix timestamp of the moment the last question is answered or skipped
    private ArrayList<String> configuredQuestionUuids; // possibly shuffled
    private Long currentConfiguredQuestionIndex; // typically, initially 0 - on finish, configuredQuestionUuids.size

    public Session(String treasureHuntUuid, String playerName, String appName, long startTime, long endTime, ArrayList<String> configuredQuestionUuids) {
        this(null, treasureHuntUuid, playerName, appName, startTime, endTime, 0L, 0L, configuredQuestionUuids, 0L);
    }

    public Session(String uuid, String treasureHuntUuid, String playerName, String appName, long startTime, long endTime, long score, long completionTime, ArrayList<String> configuredQuestionUuids, Long currentConfiguredQuestionIndex) {
        this.uuid = uuid;
        this.treasureHuntUuid = treasureHuntUuid;
        this.playerName = playerName;
        this.appName = appName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.score = score;
        this.completionTime = completionTime;
        this.configuredQuestionUuids = configuredQuestionUuids;
        this.currentConfiguredQuestionIndex = currentConfiguredQuestionIndex;
    }

    public String getUuid() {
        return uuid;
    }

    public String getShortUuid() {
        return uuid.length() > 8 ? uuid.substring(uuid.length() - 8) : uuid;
    }

    public String getTreasureHuntUuid() {
        return treasureHuntUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAppName() {
        return appName;
    }

    public long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ZZZ", Locale.US);

    public String getStartTimeFormatted() {
        return SIMPLE_DATE_FORMAT.format(new Date(startTime));
    }

    public Long getScore() {
        return score;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public ArrayList<String> getConfiguredQuestionUuids() {
        return configuredQuestionUuids;
    }

    public Long getCurrentConfiguredQuestionIndex() {
        return currentConfiguredQuestionIndex;
    }

    /**
     * @return true iff it is completed when there are no more questions to be answered
     */
    public boolean isCompleted() {
        // it is completed when there are no more questions to be answered
        return currentConfiguredQuestionIndex >= configuredQuestionUuids.size();
        // also could check that completionTime > 0
    }

    /**
     * @return true iff it is finished when it has run out of time
     */
    public boolean isFinished() {
        // it is finished when it has run out of time
        return System.currentTimeMillis() > endTime || currentConfiguredQuestionIndex >= configuredQuestionUuids.size();
    }

    @Override
    public String toString() {
        return "Session{" +
                "uuid='" + uuid + '\'' +
                ", treasureHuntUuid='" + treasureHuntUuid + '\'' +
                ", playerName='" + playerName + '\'' +
                ", appName='" + appName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", score=" + score +
                ", completionTime=" + completionTime +
                ", configuredQuestionUuids=" + configuredQuestionUuids +
                ", currentConfiguredQuestionIndex=" + currentConfiguredQuestionIndex +
                '}';
    }
}