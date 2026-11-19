package com.resumeforge.jobdiscovery;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DiscoveredJobDto {
    String id;
    String jobTitle;
    String companyName;
    String location;
    String experienceLevel;
    String jobType;
    String source;
    String applicationUrl;
    String postedDate;
    String description;
    boolean isLive;
    boolean alreadySaved;
}

