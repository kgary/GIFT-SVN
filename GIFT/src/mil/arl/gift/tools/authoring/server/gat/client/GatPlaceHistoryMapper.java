/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.CoursePlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.LearnerConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.MetadataPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.PedagogyConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.SensorsConfigurationPlace;


/**
 * PlaceHistoryMapper interface is used to attach all places which the
 * PlaceHistoryHandler should be aware of. This is done via the @WithTokenizers
 * annotation or by extending PlaceHistoryMapperWithFactory and creating a
 * separate TokenizerFactory.
 */
@WithTokenizers( {CoursePlace.Tokenizer.class, DkfPlace.Tokenizer.class, ConversationPlace.Tokenizer.class, SensorsConfigurationPlace.Tokenizer.class, LearnerConfigurationPlace.Tokenizer.class, MetadataPlace.Tokenizer.class, PedagogyConfigurationPlace.Tokenizer.class})

public interface GatPlaceHistoryMapper extends PlaceHistoryMapper {
}
