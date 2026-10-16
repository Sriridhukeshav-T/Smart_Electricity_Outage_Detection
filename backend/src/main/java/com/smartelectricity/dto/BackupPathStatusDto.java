package com.smartelectricity.dto;

public class BackupPathStatusDto {
    private String backupId;
    private String name;
    private String primaryFeederId;
    private String primaryFeederStatus;
    private String primaryFeederSource;
    private String primaryFeederTarget;
    private String alternateFeederId;
    private String alternateFeederStatus;
    private String alternateFeederSource;
    private String alternateFeederTarget;
    private boolean alternatePathAvailable;
    private boolean canRestoreAffectedLoad;
    private String pathAnalysis;
    private String simulationDisclaimer = "Simulation note: Graph reachability verified. Real-world switching requires phase sync and thermal capacity checks.";

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryFeederId() {
        return primaryFeederId;
    }

    public void setPrimaryFeederId(String primaryFeederId) {
        this.primaryFeederId = primaryFeederId;
    }

    public String getPrimaryFeederStatus() {
        return primaryFeederStatus;
    }

    public void setPrimaryFeederStatus(String primaryFeederStatus) {
        this.primaryFeederStatus = primaryFeederStatus;
    }

    public String getPrimaryFeederSource() {
        return primaryFeederSource;
    }

    public void setPrimaryFeederSource(String primaryFeederSource) {
        this.primaryFeederSource = primaryFeederSource;
    }

    public String getPrimaryFeederTarget() {
        return primaryFeederTarget;
    }

    public void setPrimaryFeederTarget(String primaryFeederTarget) {
        this.primaryFeederTarget = primaryFeederTarget;
    }

    public String getAlternateFeederId() {
        return alternateFeederId;
    }

    public void setAlternateFeederId(String alternateFeederId) {
        this.alternateFeederId = alternateFeederId;
    }

    public String getAlternateFeederStatus() {
        return alternateFeederStatus;
    }

    public void setAlternateFeederStatus(String alternateFeederStatus) {
        this.alternateFeederStatus = alternateFeederStatus;
    }

    public String getAlternateFeederSource() {
        return alternateFeederSource;
    }

    public void setAlternateFeederSource(String alternateFeederSource) {
        this.alternateFeederSource = alternateFeederSource;
    }

    public String getAlternateFeederTarget() {
        return alternateFeederTarget;
    }

    public void setAlternateFeederTarget(String alternateFeederTarget) {
        this.alternateFeederTarget = alternateFeederTarget;
    }

    public boolean isAlternatePathAvailable() {
        return alternatePathAvailable;
    }

    public void setAlternatePathAvailable(boolean alternatePathAvailable) {
        this.alternatePathAvailable = alternatePathAvailable;
    }

    public boolean isCanRestoreAffectedLoad() {
        return canRestoreAffectedLoad;
    }

    public void setCanRestoreAffectedLoad(boolean canRestoreAffectedLoad) {
        this.canRestoreAffectedLoad = canRestoreAffectedLoad;
    }

    public String getPathAnalysis() {
        return pathAnalysis;
    }

    public void setPathAnalysis(String pathAnalysis) {
        this.pathAnalysis = pathAnalysis;
    }

    public String getSimulationDisclaimer() {
        return simulationDisclaimer;
    }
}
