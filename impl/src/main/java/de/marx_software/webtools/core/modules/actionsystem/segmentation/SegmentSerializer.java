package de.marx_software.webtools.core.modules.actionsystem.segmentation;

/*-
 * #%L
 * webtools-actions
 * %%
 * Copyright (C) 2016 - 2018 Thorsten Marx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.marx_software.webtools.api.actions.model.Segment;
import de.marx_software.webtools.api.entities.Serializer;
import de.marx_software.webtools.api.model.Pair;

/**
 *
 * @author marx
 */
public class SegmentSerializer implements Serializer<Segment> {

	private static final String VERSION = "gson";
	public static final String VERSION_SEGMENT = VERSION + "_segment_v1";

	private final Gson gson;

	public SegmentSerializer() {
		GsonBuilder builder = new GsonBuilder();
		this.gson = builder.create();
	}

	@Override
	public Pair<String, String> serialize(Segment object) {
		final String content = gson.toJson(object);
		final String version = VERSION_SEGMENT;
		Pair<String, String> result = new Pair<>(version, content);
		return result;
	}

	@Override
	public Pair<String, Segment> deserialize(String version, String content) {
		Segment object = null;
		switch (version) {
			case VERSION_SEGMENT:
				object = gson.fromJson(content, Segment.class);
				break;
			default: 
				throw new IllegalArgumentException("unknown segment serialization version");
		}
		return new Pair<>(version, object);
	}

}
