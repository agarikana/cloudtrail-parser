package com.amazon.parser;

import com.amazon.parser.data.CloudTrailEvent;

import java.util.List;

public interface EventSource {

    List<CloudTrailEvent> readEvents();

}
