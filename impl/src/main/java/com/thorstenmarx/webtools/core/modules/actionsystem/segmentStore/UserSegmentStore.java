package com.thorstenmarx.webtools.core.modules.actionsystem.segmentStore;

import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import java.util.List;

/**
 *
 * @author marx
 */
public interface UserSegmentStore {

	void add(final String userid, final SegmentData segmentData);

	List<SegmentData> get(final String userid);

	void lock();

	void removeBySegment(final String segment_id);

	void removeByUser(final String user_id);

	void unlock();
	
}
