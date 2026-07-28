/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.routing;

import com.graphhopper.reader.DataReaderConfig;

/**
 * Import settings for the OSM reader.
 *
 * <p>Every setting turned out to apply to any source, not just OSM, so they now live in {@link
 * DataReaderConfig} and this type adds nothing. It is kept because it names the OSM case at call
 * sites and keeps existing code compiling; prefer {@link DataReaderConfig} in new code.
 */
public class OSMReaderConfig extends DataReaderConfig {}
