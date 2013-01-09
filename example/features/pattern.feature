# -*- truncate-lines: true; -*-
# vim: set nowrap:

@pattern
Feature: Filename pattern matching
  As a watcher of files
  I want to match only relevant files
  So that I can be a good file watcher

  Scenario: Pattern matching
    Given the result atom is empty
    And the directory "target/test_files" is empty
    And a watcher enabling extension "throttle" is watching "target/test_files" serially
    When the following data is written to "target/test_files/string.dat":
      """
      some data.
      """
    Then the result atom should be empty.
    When the following data is written to "target/test_files/x goodstring x.dat":
      """
      some data 2.
      """
    Then the result atom should be empty.
    When the following data is written to "target/test_files/goodstring.dat":
      """
      some data 3.
      """
    Then the following data should be in the result atom.
      """
      some data 4.
      """
