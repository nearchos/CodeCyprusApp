package org.codecyprus.th.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TreasureHunt implements Serializable {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);

    private String uuid; // PK
    private String name; // non-null, non-empty
    private String description;
    private String ownerEmail;
    private Visibility visibility; // determines who can view the treasure hunt
    private long startsOn; // UTC unix timestamp (milliseconds)
    private long endsOn; // UTC unix timestamp (milliseconds)
    private long maxDuration; // milliseconds
    private boolean shuffled; // if true, each player gets a shuffled sequence of questions
    private boolean requiresAuthentication; // if true, only authenticated users can play
    private boolean emailResults; // if true, sends a summary of results after the end of the competition (not of the player's session)

    public TreasureHunt(String name, String description, String ownerEmail, Visibility visibility, long startsOn, long endsOn, long maxDuration, boolean shuffled, boolean requiresAuthentication, boolean emailResults) {
        this(null, name, description, ownerEmail, visibility, startsOn, endsOn, maxDuration, shuffled, requiresAuthentication, emailResults);
    }

    public TreasureHunt(String uuid, String name, String description, String ownerEmail, Visibility visibility, long startsOn, long endsOn, long maxDuration, boolean shuffled, boolean requiresAuthentication, boolean emailResults) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.ownerEmail = ownerEmail;
        this.visibility = visibility;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.maxDuration = maxDuration;
        this.shuffled = shuffled;
        this.requiresAuthentication = requiresAuthentication;
        this.emailResults = emailResults;
    }

    public String getUuid() {
        return uuid;
    }

    public String getShortUuid() {
        return uuid.length() > 8 ? uuid.substring(uuid.length() - 8) : uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public long getStartsOn() {
        return startsOn;
    }

    public String getStartsOnAsString() {
        return SIMPLE_DATE_FORMAT.format(new Date(startsOn));
    }

    public long getEndsOn() {
        return endsOn;
    }

    public String getEndsOnAsString() {
        return SIMPLE_DATE_FORMAT.format(new Date(endsOn));
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public boolean isShuffled() {
        return shuffled;
    }

    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    public boolean isEmailResults() {
        return emailResults;
    }

    public boolean isNotStarted() {
        final long now = System.currentTimeMillis();
        return now < startsOn;
    }

    public boolean isFinished() {
        final long now = System.currentTimeMillis();
        return now >= endsOn;
    }

    public boolean isActiveNow() {
        final long now = System.currentTimeMillis();
        return now >= startsOn && now <= endsOn;
    }

    public boolean hasFinished() {
        final long now = System.currentTimeMillis();
        return now > endsOn;
    }

    public String toString() {
        return getName();
    }
}