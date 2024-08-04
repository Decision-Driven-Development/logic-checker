# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [0.3.2](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.3.1...v0.3.2) (2024-08-04)


### Features

* **server:** initialize an empty tests folder ([807d016](https://github.com/Decision-Driven-Development/logic-checker/commit/807d016394ae3e3b54b833bc0c13822be14d98f0)), closes [#34](https://github.com/Decision-Driven-Development/logic-checker/issues/34)
* **server:** initializing with a barebones project ([3551e55](https://github.com/Decision-Driven-Development/logic-checker/commit/3551e55022b420a9e545ae2875e16bae45885e0e)), closes [#33](https://github.com/Decision-Driven-Development/logic-checker/issues/33)
* **test:** visual enhancements for test results page ([a4d75a7](https://github.com/Decision-Driven-Development/logic-checker/commit/a4d75a77d2db54b624a74d7dea686ff126defe09)), closes [#39](https://github.com/Decision-Driven-Development/logic-checker/issues/39)
* **webUI:** added a logo to the navbar ([49386d2](https://github.com/Decision-Driven-Development/logic-checker/commit/49386d2897c86f8e82741be8590e7b7d95ad4c8c)), closes [#45](https://github.com/Decision-Driven-Development/logic-checker/issues/45)
* **webUI:** added specific page titles ([ea672fe](https://github.com/Decision-Driven-Development/logic-checker/commit/ea672fe16dca8c241bacbaf5eb23d8cebb32e8db)), closes [#38](https://github.com/Decision-Driven-Development/logic-checker/issues/38)
* **webUI:** created index page as a server entrypoint ([81cbe76](https://github.com/Decision-Driven-Development/logic-checker/commit/81cbe76b90579f5f889d0571d85f7eb8abfe910e)), closes [#33](https://github.com/Decision-Driven-Development/logic-checker/issues/33)
* **webUI:** implemented a simple navbar for the tool ([bdcf2e9](https://github.com/Decision-Driven-Development/logic-checker/commit/bdcf2e9d0550b7c5a4e7f0a6f15a5c16a9ff71b2)), closes [#45](https://github.com/Decision-Driven-Development/logic-checker/issues/45)
* **webUI:** implemented the server configuration page ([12113b8](https://github.com/Decision-Driven-Development/logic-checker/commit/12113b82a385f00db69527ee0ebbce09b4049ea9)), closes [#44](https://github.com/Decision-Driven-Development/logic-checker/issues/44)
* **webUI:** rendering all pages as layout inclusions ([5e45d68](https://github.com/Decision-Driven-Development/logic-checker/commit/5e45d68cdf0c017927d8969845546aa15ff2b77e)), closes [#56](https://github.com/Decision-Driven-Development/logic-checker/issues/56)
* **webUI:** rewrite stored request after checking commands availability ([8891046](https://github.com/Decision-Driven-Development/logic-checker/commit/8891046e4110a9a062f6f0aea6062e21e442e237)), closes [#40](https://github.com/Decision-Driven-Development/logic-checker/issues/40)


### Bug Fixes

* **test:** running a test referencing non-existing commands just fails the test ([d2bff97](https://github.com/Decision-Driven-Development/logic-checker/commit/d2bff972a9a20aed072a91ff7e84cfa77db5a175)), closes [#36](https://github.com/Decision-Driven-Development/logic-checker/issues/36)
* **test:** running a test referencing non-existing tables just fails the test ([e5907fa](https://github.com/Decision-Driven-Development/logic-checker/commit/e5907fa937ffd06ee03feb133c6d5c7e75e5fe43)), closes [#35](https://github.com/Decision-Driven-Development/logic-checker/issues/35)
* **test:** running a test without Arrange section outputs informative fail reason ([5615348](https://github.com/Decision-Driven-Development/logic-checker/commit/56153486c9f05419fda28191f6a08ef58ad5e905)), closes [#53](https://github.com/Decision-Driven-Development/logic-checker/issues/53)
* **webUI:** commands are asking for request params only ([faa9d1b](https://github.com/Decision-Driven-Development/logic-checker/commit/faa9d1b52555145eb3dff636e7bf737c61fb473b)), closes [#42](https://github.com/Decision-Driven-Development/logic-checker/issues/42)

### [0.3.1](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.3.0...v0.3.1) (2024-07-08)


### Features

* added Bootstrap and created the command arguments form ([5d1ba96](https://github.com/Decision-Driven-Development/logic-checker/commit/5d1ba96dbb0c41663a7a3cf7c1bc8122f52ecdba)), closes [#25](https://github.com/Decision-Driven-Development/logic-checker/issues/25)
* checking the commands availability ([54ebd9d](https://github.com/Decision-Driven-Development/logic-checker/commit/54ebd9da5d64100fbc76be11e5fe1a22f57400df)), closes [#29](https://github.com/Decision-Driven-Development/logic-checker/issues/29)
* coloring buttons based on command availability ([e65f9b3](https://github.com/Decision-Driven-Development/logic-checker/commit/e65f9b33fd598e0fd2d2ae6b74f7343c7d0985ae)), closes [#29](https://github.com/Decision-Driven-Development/logic-checker/issues/29)
* designed a form for updating the availability check context ([3af63f8](https://github.com/Decision-Driven-Development/logic-checker/commit/3af63f8b772641744c041b46dc92c0bb019225b6)), closes [#29](https://github.com/Decision-Driven-Development/logic-checker/issues/29)
* listing all the command arguments on a command modal ([e11b0a6](https://github.com/Decision-Driven-Development/logic-checker/commit/e11b0a68c62f084486f5531465efdddac1d654e5)), closes [#28](https://github.com/Decision-Driven-Development/logic-checker/issues/28)
* remembering user input for commands context computation ([b17950b](https://github.com/Decision-Driven-Development/logic-checker/commit/b17950beaec8782f96653693b90d28c42422f058)), closes [#32](https://github.com/Decision-Driven-Development/logic-checker/issues/32)
* running the command with user-supplied parameters ([4cbd9ef](https://github.com/Decision-Driven-Development/logic-checker/commit/4cbd9efb5c77902d7653bea5d62c59c0022c7a28)), closes [#26](https://github.com/Decision-Driven-Development/logic-checker/issues/26)
* showing all the current values for command args ([df15c7e](https://github.com/Decision-Driven-Development/logic-checker/commit/df15c7e0fcf8daa8d48a7739f59515dbc8fb5c58)), closes [#25](https://github.com/Decision-Driven-Development/logic-checker/issues/25)


### Bug Fixes

* recreating ComputationContext for each request ([467af68](https://github.com/Decision-Driven-Development/logic-checker/commit/467af68a38e221db6a9686243ba0289d29175ae5)), closes [#31](https://github.com/Decision-Driven-Development/logic-checker/issues/31)

## [0.3.0](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.2.0...v0.3.0) (2024-07-01)


### Features

* **config:** initialize empty set of Locators ([76fe0ed](https://github.com/Decision-Driven-Development/logic-checker/commit/76fe0ed6c3dfd300535daf560e07654310e24ac6)), closes [#16](https://github.com/Decision-Driven-Development/logic-checker/issues/16)
* showing all the commands on a page ([98d4066](https://github.com/Decision-Driven-Development/logic-checker/commit/98d406625a380c93644d55d8d105564651141922)), closes [#23](https://github.com/Decision-Driven-Development/logic-checker/issues/23) [#9](https://github.com/Decision-Driven-Development/logic-checker/issues/9)
* **webUI:** a simple web page for the tool ([7a7baae](https://github.com/Decision-Driven-Development/logic-checker/commit/7a7baaeb585639fa44d8580f6ee2ed65bfe94384)), closes [#7](https://github.com/Decision-Driven-Development/logic-checker/issues/7)
* **webUI:** button to get the command info and execute the command ([b6e3ed4](https://github.com/Decision-Driven-Development/logic-checker/commit/b6e3ed463cedd9b40d147cd750ec9857458da9c9)), closes [#19](https://github.com/Decision-Driven-Development/logic-checker/issues/19)
* **webUI:** created a static state page ([2dc513c](https://github.com/Decision-Driven-Development/logic-checker/commit/2dc513cc71bf2de2e704408117d19951b9132676)), closes [#10](https://github.com/Decision-Driven-Development/logic-checker/issues/10)
* **webUI:** handle error while performing commands ([39f87b8](https://github.com/Decision-Driven-Development/logic-checker/commit/39f87b8c688ebc7280e4bb95a40044305bd9a763)), closes [#27](https://github.com/Decision-Driven-Development/logic-checker/issues/27)
* **webUI:** rendering state page with stored state ([e6125a4](https://github.com/Decision-Driven-Development/logic-checker/commit/e6125a4cbba4c87ab206d5e40289b0856b66e469)), closes [#12](https://github.com/Decision-Driven-Development/logic-checker/issues/12)
* **webUI:** run tests from the web interface ([e5c1c20](https://github.com/Decision-Driven-Development/logic-checker/commit/e5c1c20ff590dae1d9d9c36684f713391f2333a4)), closes [#30](https://github.com/Decision-Driven-Development/logic-checker/issues/30)
* **webUI:** simple command invocation (no arguments) ([e9e1ba1](https://github.com/Decision-Driven-Development/logic-checker/commit/e9e1ba166a4b99776ab4a3e25011c7479d56ad02)), closes [#9](https://github.com/Decision-Driven-Development/logic-checker/issues/9)

## [0.2.0](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.1.1...v0.2.0) (2024-05-13)


### Features

* ability to make assertions against state ([0ec58d0](https://github.com/Decision-Driven-Development/logic-checker/commit/0ec58d0d82be55fa1f6bb8b9537868378f0142d9)), closes [#6](https://github.com/Decision-Driven-Development/logic-checker/issues/6)

### [0.1.1](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.1.0...v0.1.1) (2024-05-12)

## [0.1.0](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.0.3...v0.1.0) (2024-05-12)


### Bug Fixes

* correct variable in workflow file ([9cacdd8](https://github.com/Decision-Driven-Development/logic-checker/commit/9cacdd8bba4b928d6c1ffbcf77945ccf73fc765e))

### [0.0.3](https://github.com/Decision-Driven-Development/logic-checker/compare/v0.0.2...v0.0.3) (2024-05-12)


### Features

* **commands:** support writing commands in tests ([f476882](https://github.com/Decision-Driven-Development/logic-checker/commit/f476882c77699bd6a466bf3c12e7367ebcaca131))
* packing as a standalone executable jar ([04c8867](https://github.com/Decision-Driven-Development/logic-checker/commit/04c886727d3c2732603d25d3753270777a3009dc))

### 0.0.2 (2024-05-04)


### Features

* added a file-based state test ([64d15a1](https://github.com/Decision-Driven-Development/logic-checker/commit/64d15a1b6dbc3e5ebaed1baba67936e97fc76ab0)), closes [#3](https://github.com/Decision-Driven-Development/logic-checker/issues/3)

### 0.0.1 (2024-05-04)
