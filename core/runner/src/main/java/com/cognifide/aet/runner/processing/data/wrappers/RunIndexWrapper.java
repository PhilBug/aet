/**
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cognifide.aet.runner.processing.data.wrappers;

import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.communication.api.metadata.Url;
import com.cognifide.aet.communication.api.wrappers.MetadataRunDecorator;
import com.cognifide.aet.communication.api.wrappers.Run;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class RunIndexWrapper<T> {

  protected Run<T> objectToRunWrapper;

  private final Map<Comparator, Long> comparatorCounts;

  RunIndexWrapper(Run<T> objectToRunWrapper) {
    this.objectToRunWrapper = objectToRunWrapper;
    this.comparatorCounts =
        getUrls().stream()
            .flatMap(url -> url.getObjectToRun().getSteps().stream())
            .flatMap(step -> step.getComparators().stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
  }

  public static void cleanUrlFromExecutionData(Url url) {
    url.setCollectionStats(null);
    url.getSteps()
        .forEach(step -> {
          step.setStepResult(null);
          if (step.getComparators() != null) {
            step.getComparators()
                .forEach(comparator ->
                    comparator.setStepResult(null)
                );
          }
        });
  }

  public Optional<Url> getTestUrl(String testName, final String urlName) {
    Optional<Test> test = getTest(testName);
    Optional<Url> url = Optional.ofNullable(null);
    if (test.isPresent()) {
      url = test.get().getUrl(urlName);
    }
    return url;
  }

  public Optional<Test> getTest(String testName) {
    return objectToRunWrapper.getRealSuite().getTest(testName);
  }

  public Run<T> get() {
    return objectToRunWrapper;
  }

  public abstract List<MetadataRunDecorator<Url>> getUrls();

  public abstract int countUrls();

  public Map<Comparator, Long> getComparatorCounts() { // todo test
    return comparatorCounts;
  }//todo

  @Override
  public String toString() {
    return "RunIndexWrapper{" +
        "objectToRunWrapper=" + objectToRunWrapper +
        '}';
  }
}
