# Copyright 2017 Penny Rohr Curich
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

@Simple
@Empty()
@Complex("one")
Feature: Tag Handling

  @Smoke
  Scenario: A Simple @Smoke Case
  This tests the @Smoke annotation without an argument.

    Given a @Smoke annotation without arguments
    When this scenario is filtered with SPeL statement isSmoke()
    Then this scenario is included in returned results

  @Smoke @Regression
  Scenario: Two Different Tags on One Line

    Given a @Smoke tag and a @Regression tag on one line
    When this scenario is filtered with SPeL statement isSmoke()
    Then this scenario is included in returned results


  @Smoke("withArgument")
  Scenario: An Argumented @Smoke Case
  This tests the Smoke annotation with an argument.

    Given a @Smoke annotation without arguments
    When this scenario is filtered with SPeL statement isSmoke()
    Then this scenario is included in returned results


  @Smoke @Smoke("withArgument")
  Scenario: Two different @Smoke tags on one line.

    Given a @Smoke tag and an argumented @Smoke tag on one line
    When this scenario is filtered with SPeL statement isSmoke()
    Then this scenario is included in returned results

  @Smoke
  Scenario Outline: A Scenario Outline @Smoke Case
    Given a Scenario Outline with a @Smoke annotation
    When this outline is filtered with SPeL statement isSmoke();
    Then each example is returned as a separate result

    Examples:
      | weight | energy | protein |
      | 450    | 26500  | 215     |
      | 500    | 29500  | 245     |
