# -*- truncate-lines: true; -*-
# vim: set nowrap:

@noextensions
Feature: Don't use any extensions

  Scenario: New file and append
    Given the result atom is empty
    And the directory "target/test_files" is empty
    And a watcher enabling extension "no extensions" is watching "target/test_files" serially

    When the following data is written to "target/test_files/goodstring.dat":
      """
      foo
      """
    Then the following data should be in the result atom
      """
      foo

      """
