## json4s Contributers' Guide

### Issues

- Questions should be posted to [stackoverflow.com](http://stackoverflow.com/questions/tagged/json4s)
- Please describe about your issue in detail (verison, situation, examples)
- We may close your issue when we have no plan to take action right now. We appreciate your understanding.

### Pull Requests

- Pull requests basically should be sent toward "3.4" branch
- Source/binary compatibility always must be kept as far as possible
- Prefer creating scala source code for each class/object/trait (of course, except for sealed trait)
- json4s build checks binary compatibility by using [MiMa](https://github.com/typesafehub/migration-manager/wiki/Sbt-plugin) for maintenance releases (e.g. 3.3.x).

#### Branches

##### 3.4 (the default branch)

- The latest stable version
- This branch must be able to build against Scala 2.10 and 2.11

##### 3.3

- The version 3.3 series maintainance branch
- All the backports must be source/binary compatibility
- This branch must be able to build against Scala 2.10 and 2.11

#### Testing your pull request

All the pull requests must pass the Travis CI jobs before merging them.

https://travis-ci.org/json4s/json4s

Testing with default settings is required when push changes:

```sh
sbt test
```
