# Codeless business logic checker

[![PDD status](https://www.0pdd.com/svg?name=Decision-Driven-Development/logic-checker)](https://www.0pdd.com/p?name=Decision-Driven-Development/logic-checker)
[![Hits-of-Code](https://hitsofcode.com/github/Decision-Driven-Development/logic-checker)](https://hitsofcode.com/github/Decision-Driven-Development/logic-checker/view)

A tool to check the correctness of business-logic, described by decision tables and commands

## Source files format

### Folder structure

The tool is able to run autotests for decision tables and state machines. The folder structure for 
every project should consist of the following folders:

- `commands` - folder with commands described in yaml format
- `tables` - folder with decision tables
- `states` - folder with initial state descriptions and test expectations

### Commands

Commands should be described like that:

```yaml
operations:
  - "cells::${request::move} -> ${request::player}"
  - "table::currentPlayer -> ${table::nextPlayer}"
  - "table::nextPlayer -> ${request::player}"
```

The only required top-level section is `operations`. It should contain a list of operations, in a 
format of `target -> value`. The target must point to a valid Coordinate, i.e. a field of an entity
in the system's state. The value can be a constant or a reference to another valid Coordinate. 

References are written in a format of `${source::field}`, they can be nested and point to any valid
Coordinate.

### Decision tables

For the decision table to be processed, it should be in CSV format with semicolon (`;`) as a separator.
The columns of the table represent rules, except for the first one, which contains the "base" of each
condition. The rows in the first section (before `---`) represent the conditions, and the rows in the second
section (after `---`) represent the outcomes.

Every value in the conditions section should refer to a specific data fragment, which can be located
by its description, called `Coordinate`. Coordinates consist of two parts: `Locator` - that is the
name of the data domain, and `Identifier` - that is the name of the data fragment. Those parts are separated
by `::`. For example, value `ui_button_create::enabled` refers to the `enabled` fragment of the
`ui_button_create` locator.

For the constant values, the `Locator` part can be omitted, so the value `5` refers to a constant value `5`.
For the sake of completeness, the `Locator` part for constant values is `constant`, so it is possible to
write `constant::5` instead of just `5` (but it is not necessary).

The table cells in the conditions section should contain the condition description, consisting of
the logical operator (if any), followed by the comparison operator (if any), followed by the value.

If the comparison operator is omitted, it is assumed to be `==`, which means that the "base" value of
the condition should be equal to the cell value for the condition to be true. Other comparison operators
are `>` and `<`.

The logical operators are used to modify the condition itself. The operator `~` means "any", so it is
effectively means that the condition is always true. The operator `!` means "not", so it negates the
specified condition.

### Tests

The tests are described in yaml format. The file should contain several sections, separated by `---`.

1. Initial state - the description of the initial state of the system. It should contain the description
of the data fragments, which are the Coordinates of the system's state. An example of the initial state
description:

```yaml
request:
  player: "O"
  move: "A1"
table:
  currentPlayer: "X"
cells:
  A1: "empty"
...
```

2. Commands - the list of commands to be executed. The commands are described like that:

```yaml
commands:
  - name: "computed_move"
    request:
      player: "X"
      move: "A1"
```

So, every command should have a name and a description of the request. The request should contain the
description of the data fragments, which are the Coordinates of the system's state.

3. Expectations - the expected result of decision tables computation OR state of the entities after 
all the commands are executed. The expectations should be described in the following format:

```yaml
game_state:
  is_over: "false"
  winner: "none"
```

If it is the assertion on a decision table, then the upper-level key should be the name of the 
decision table, and the lower-level keys are the names of the outcomes with their expected values.
If it is the assertion on a state, then it's the same format as in the initial state description.

## Running the tests

The tests are run by:

1. starting the web server and passing the absolute path to the project folder as an argument:
    ```bash
    java -jar logic-checker.jar <absolute-path-to-the-app-resources>
    ```
2. and then heading to the `http://localhost:8080/test` in the browser.
