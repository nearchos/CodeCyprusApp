package org.codecyprus.th.model;

import java.io.Serializable;

public class ConfiguredQuestion implements Serializable {

    private String uuid; // PK
    private String treasureHuntUuid; // FK
    private String questionUuid; // FK
    private Long seqNumber; // used to specify sequence in non-shuffled treasure hunts
    private Long correctScore; // default +10
    private Long wrongScore; // default -3
    private Long skipScore; // default -5
    private Boolean canBeSkipped; // if false, then must be answered to continue
    private Double latitude; // can 0 or not set to ignore location
    private Double longitude; // can 0 or not set to ignore location
    private Double distanceThreshold; // can 0 or not set to ignore location, otherwise a positive integer in meters, default is 25

    public ConfiguredQuestion(String treasureHuntUuid, String questionUuid, Long seqNumber, Long correctScore, Long wrongScore, Long skipScore, Boolean canBeSkipped, Double latitude, Double longitude, Double distanceThreshold) {
        this(null, treasureHuntUuid, questionUuid, seqNumber, correctScore, wrongScore, skipScore, canBeSkipped, latitude, longitude, distanceThreshold);
    }

    public ConfiguredQuestion(String uuid, String treasureHuntUuid, String questionUuid, Long seqNumber, Long correctScore, Long wrongScore, Long skipScore, Boolean canBeSkipped, Double latitude, Double longitude, Double distanceThreshold) {
        this.uuid = uuid;
        this.treasureHuntUuid = treasureHuntUuid;
        this.questionUuid = questionUuid;
        this.seqNumber = seqNumber;
        this.correctScore = correctScore;
        this.wrongScore = wrongScore;
        this.skipScore = skipScore;
        this.canBeSkipped = canBeSkipped;
        this.latitude = latitude;
        this.longitude  = longitude;
        this.distanceThreshold = distanceThreshold;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTreasureHuntUuid() {
        return treasureHuntUuid;
    }

    public String getQuestionUuid() {
        return questionUuid;
    }

    public Long getSeqNumber() {
        return seqNumber;
    }

    public Long getCorrectScore() {
        return correctScore;
    }

    public Long getWrongScore() {
        return wrongScore;
    }

    public Long getSkipScore() {
        return skipScore;
    }

    public Boolean isCanBeSkipped() {
        return canBeSkipped;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDistanceThreshold() {
        return distanceThreshold;
    }

    public boolean isLocationRelevant() {
        return this.latitude != 0.0d && this.longitude != 0.0d;
    }
}