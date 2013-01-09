# -*- truncate-lines: true; -*-
# vim: set nowrap:

@trackappends
Feature: Notice append vs non-append file changes.

  Scenario: New file and append
    Given the result atom is empty
    And the directory "target/test_files" is empty
    And a watcher enabling extension "throttle and track-appends" is watching "target/test_files" using parallel workers

    When the following data is written to "target/test_files/goodstring.dat":
      """
      foo
      """
    And the following data is appended to "target/test_files/goodstring.dat":
      """
      bar
      """
    Then the following data should be in the result atom
      """
      foo
      bar
      (append-only)
      """

    When the following data is written to "target/test_files/goodstring.dat":
      """
      baz
      """
    Then the following data should be in the result atom
      """
      baz

      """
