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
package com.cognifide.aet.executor.http;

import com.cognifide.aet.communication.api.execution.SuiteExecutionResult;
import org.apache.http.HttpStatus;

/**
 * Wraps HTTP response for Suite execution result.
 */
public class HttpSuiteExecutionResultWrapper {

  private final int statusCode;
  private final SuiteExecutionResult executionResult;

  private HttpSuiteExecutionResultWrapper(SuiteExecutionResult executionResult, int statusCode) {
    this.statusCode = statusCode;
    this.executionResult = executionResult;
  }

  public static HttpSuiteExecutionResultWrapper wrap(SuiteExecutionResult executionResult) {
    return new HttpSuiteExecutionResultWrapper(executionResult, HttpStatus.SC_OK);
  }

  public static HttpSuiteExecutionResultWrapper wrapError(SuiteExecutionResult executionResult,
      int statusCode) {
    return new HttpSuiteExecutionResultWrapper(executionResult, statusCode);
  }

  public SuiteExecutionResult getExecutionResult() {
    return executionResult;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public boolean hasError() {
    return statusCode != HttpStatus.SC_OK;
  }
}
