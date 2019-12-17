/*
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.job.common.comparators.jserrors.JsErrorsComparator;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import java.util.concurrent.atomic.AtomicLong;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class JsErrorsGrouperFactory implements GrouperFactory {

  @Reference private ArtifactsDAO artifactsDAO;

  @Override
  public String getName() {
    return JsErrorsComparator.COMPARATOR_NAME;
  }

  @Override
  public GrouperJob createInstance(DBKey dbKey, long expectedInputCount) {
    AtomicLong expectedMessagesCount = new AtomicLong(expectedInputCount);
    return new JsErrorsGrouper(artifactsDAO, dbKey, expectedMessagesCount);
  }
}