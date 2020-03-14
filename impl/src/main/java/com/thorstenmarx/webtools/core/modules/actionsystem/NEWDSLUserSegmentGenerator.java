package com.thorstenmarx.webtools.core.modules.actionsystem;

import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.query.Aggregator;
import com.thorstenmarx.webtools.api.analytics.query.Query;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.entities.criteria.Restrictions;
import com.thorstenmarx.webtools.core.modules.actionsystem.newdsl.DSLSegment;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.GraalDSL;
import com.thorstenmarx.webtools.core.modules.actionsystem.newdsl.JsonDsl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public class NEWDSLUserSegmentGenerator {

	public static final Logger LOGGER = LoggerFactory.getLogger(NEWDSLUserSegmentGenerator.class);

	final AnalyticsDB db;
	final JsonDsl dslRunner;
	final SegmentService segmentService;

	public NEWDSLUserSegmentGenerator(final AnalyticsDB db, final JsonDsl dslRunner, final SegmentService segmentService) {
		this.db = db;
		this.dslRunner = dslRunner;
		this.segmentService = segmentService;

	}

	public List<SegmentData> generate(final String userid) {
		return generate(userid, null);
	}

	public List<SegmentData> generate(final String userid, final String site) {
		return get(userid, site).stream().map(AdvancedSegment.class::cast).map((segment) -> {

			final SegmentData segmentData = new SegmentData();
			segmentData.setSegment(new SegmentData.Segment(segment.getName(), segment.getExternalId(), segment.getId()));
			return segmentData;
		}).collect(Collectors.toList());
	}

	protected List<Segment> get(final String userid, final String site) {
		List<Segment> activeSegments;
		if (site != null) {
			activeSegments = segmentService.criteria().add(Restrictions.EQ.eq("site", site)).query().stream()
					.filter(Segment::isActive)
					.collect(Collectors.toList());
		} else {
			activeSegments = segmentService.all().stream().filter(Segment::isActive).collect(Collectors.toList());
		}
		return activeSegments.stream().filter((segment) -> segment_contains_userdata(segment, userid)).collect(Collectors.toList());
	}

	public boolean segment_contains_userdata(final Segment segment, final String userid) {
		try {
			Query simpleQuery = Query.builder().start(segment.start()).end(segment.end())
					.term(Fields.UserId.value(), userid)
					.build();

			Future<Boolean> future;
			future = db.query(simpleQuery, new Aggregator<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					DSLSegment dsl;
					if (segment instanceof AdvancedSegment) {
						AdvancedSegment aseg = (AdvancedSegment) segment;
						if (aseg.getDsl() == null) {
							aseg.setDsl(aseg.getContent());
						}
						try {
							dsl = dslRunner.parse(aseg.getDsl());
						} catch (Exception e)  {
							throw new RuntimeException(e);
						}
					} else {
						throw new IllegalStateException("unkown segment definition");
					}
					documents.stream().forEach(dsl::handle);
					dsl.match();

					return dsl.matchs(userid);

//					Set<String> matchingUsers = new HashSet<>();
//					dsl.getAllUsers().stream().filter(dsl::matchs).forEach(matchingUsers::add);
//					return matchingUsers.contains(userid);
				}
			});
			
			
			return future != null ? future.get() : false;
		} catch (InterruptedException | ExecutionException ex) {
			LOGGER.error("", ex);
		}

		return false;
	}
}