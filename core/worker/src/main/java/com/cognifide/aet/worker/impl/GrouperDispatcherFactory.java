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

package com.cognifide.aet.worker.impl;

import com.cognifide.aet.communication.api.SuiteComparatorsCount;
import com.cognifide.aet.communication.api.SuiteTestIdentifier;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to create {@link GrouperDispatcher} objects and manage their lifespan by counting down
 * through ticks.
 *
 * <p>References to {@link GrouperDispatcher} objects are being kept until their life ends or they
 * are force-removed.
 *
 * <p>Ensures thread-safety by using concurrent maps internally.
 */
@Component(immediate = true, service = GrouperDispatcherFactory.class)
public class GrouperDispatcherFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherFactory.class);

  private final ConcurrentMap<SuiteTestIdentifier, AtomicInteger> lifespanCountdowns =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<SuiteTestIdentifier, GrouperDispatcher> dispatchers =
      new ConcurrentHashMap<>();

  /**
   * Used to obtain grouper dispatcher instance. Will return existing object if there's already been
   * a call with the same test identifier and the then returned object hasn't been force-removed and
   * its life didn't end yet. In other case, will create new grouper dispatcher object.
   *
   * @param suiteTestIdentifier   unique identifier of test within suite run
   * @param suiteComparatorsCount comparator type counts provider
   * @param grouperJobs           grouper jobs map supplier
   * @return instance of grouper dispatcher
   * @throws IllegalArgumentException when there are no comparators in the suite
   */
  public GrouperDispatcher getDispatcher(
      SuiteTestIdentifier suiteTestIdentifier,
      SuiteComparatorsCount suiteComparatorsCount,
      Supplier<Map<String, GrouperJob>> grouperJobs) {
    lifespanCountdowns.computeIfAbsent(
        suiteTestIdentifier, it -> newCountdown(suiteTestIdentifier, suiteComparatorsCount));
    return dispatchers.computeIfAbsent(
        suiteTestIdentifier,
        it -> newDispatcher(suiteTestIdentifier, suiteComparatorsCount, grouperJobs.get()));
  }

  /**
   * Counts down the remaining life of the grouper dispatcher with given identifier.
   *
   * @param suiteTestIdentifier unique identifier of test within suite run
   * @return remaining lifespan in ticks
   * @throws NullPointerException when there's no grouper dispatcher for given identifier
   */
  public int tick(SuiteTestIdentifier suiteTestIdentifier) {
    int lifespanRemaining = countDown(suiteTestIdentifier);
    if (lifespanRemaining == 0) {
      LOGGER.info("Removing GrouperDispatcher for id: {}", suiteTestIdentifier);
      dispatchers.remove(suiteTestIdentifier);
      lifespanCountdowns.remove(suiteTestIdentifier);
    }
    return lifespanRemaining;
  }

  /**
   * Provides ability to force-remove grouper dispatcher from the factory's container.
   *
   * @param suiteTestIdentifier unique identifier of test within suite run
   */
  public void forceRemove(SuiteTestIdentifier suiteTestIdentifier) {
    GrouperDispatcher dispatcher = dispatchers.remove(suiteTestIdentifier);
    AtomicInteger countdown = lifespanCountdowns.remove(suiteTestIdentifier);
    if (Objects.isNull(dispatcher) || Objects.isNull(countdown)) {
      LOGGER.warn(
          "Tried to force remove non-existing GrouperDispatcher: {}, countdown: {}",
          dispatcher,
          countdown);
    }
  }

  private GrouperDispatcherImpl newDispatcher(
      SuiteTestIdentifier suiteTestIdentifier,
      SuiteComparatorsCount suiteComparatorsCount,
      Map<String, GrouperJob> grouperJobs) {
    LOGGER.info("Creating new GrouperDispatcher for id: {}", suiteComparatorsCount);
    Map<String, AtomicInteger> comparatorCountdowns =
        suiteComparatorsCount.prepareCountdownsByComparatorTypes(suiteTestIdentifier.getTestName());
    return new GrouperDispatcherImpl(comparatorCountdowns, grouperJobs);
  }

  private AtomicInteger newCountdown(
      SuiteTestIdentifier suiteTestIdentifier, SuiteComparatorsCount suiteComparatorsCount) {
    int distinctComparators =
        suiteComparatorsCount.getDistinctComparatorsCountForTest(suiteTestIdentifier.getTestName());
    if (distinctComparators == 0) {
      throw new IllegalArgumentException("Countdown must be initialized with positive value");
    }
    return new AtomicInteger(distinctComparators);
  }

  private int countDown(SuiteTestIdentifier suiteTestIdentifier) {
    AtomicInteger remainingJobsCountdown = lifespanCountdowns.get(suiteTestIdentifier);
    return remainingJobsCountdown.decrementAndGet();
  }
}
