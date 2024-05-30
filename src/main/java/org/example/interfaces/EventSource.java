package org.example.interfaces;

import org.example.data.CloudTrailEvent;

import java.util.List;

public interface EventSource {

    List<CloudTrailEvent> readEvents();

}
