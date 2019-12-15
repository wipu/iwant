# iwant
A java-based declarative, multi-threaded and incremental build system that provides a fluent java API for writing build programs.
Can use ant tasks and ant and shell scripts as backend.

Plese see [iwant.fluentjava.org](http://iwant.fluentjava.org) for more information.

**NOTE** This is a clone from [sourceforge](http://iwant.sourceforge.net). Things don't yet work in this git clone (there are still dependencies to svn), but migration is in progress.

## News

* *2017-11-18* **Build-breaking refactor: renamed iwant package** As part of migration away from sourceforge the package of iwant was renamed to `org.fluentjava.iwant` so when you upgrade iwant for your project, you need to fix your imports. There is a [migration script](https://github.com/wipu/iwant/blob/feature/package-rename-to-org.fluentjava/optional/iwant-migration-scripts/2017-10-package-rename-to-org.fluentjava.sh) that is hopefully helpful there. If not, sorry for the trouble and please ask for help.
