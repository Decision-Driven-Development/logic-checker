# Codeless business logic checker

[![PDD status](https://www.0pdd.com/svg?name=Decision-Driven-Development/logic-checker)](https://www.0pdd.com/p?name=Decision-Driven-Development/logic-checker)
[![Hits-of-Code](https://hitsofcode.com/github/Decision-Driven-Development/logic-checker)](https://hitsofcode.com/github/Decision-Driven-Development/logic-checker/view)

A tool to check the correctness of business-logic, described by decision tables and commands

### Auto testing

If the state yaml files contain two sections, separated with `---`, it is possible to use them as
autotests. It can be done with following Maven command:

```shell
mvn clean compile test -Dtest=StateBasedTest -Dtables=<path-to-tables-folder> -Dstates=<path-to-states-folder>
```

This command will scan all the yaml files in specified `states` folder and check if the expectations stated in
the second section of those files hold true.

The expectations should be stated in following format:

```yaml
table-name:
  outcome1: "expected value"
  outcome2: "expected value"
another-table:
  outcome1: "expected value"
  outcome2: "expected value"
```

All the expected values should be Strings.

Example of the state with expectation could be found in [state-test.yml](https://github.com/nergal-perm/Decision-Driven-Development/logic-checker/master/src/test/resources/states/state-test.yml).
