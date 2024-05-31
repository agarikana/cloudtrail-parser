package com.amazon.parser.interfaces;

import com.amazon.parser.data.CloudTrailEvent;

import java.util.List;

public interface EventSource {

    List<CloudTrailEvent> readEvents();

}
