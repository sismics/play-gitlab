# play-gitlab plugin

This plugin adds [Gitlab](https://about.gitlab.com/) support to Play! Framework 1 applications.

# Features

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - gitlab -> gitlab 1.1.0

repositories:
    - sismicsNexusRaw:
        type: http
        artifact: "https://nexus.sismics.com/repository/sismics/[module]-[revision].zip"
        contains:
            - gitlab -> *

```
####  Set configuration parameters

Add the following parameters to **application.conf**:

```
# Gitlab configuration
# ~~~~~~~~~~~~~~~~~~~~
gitlab.mock=false
gitlab.url=https://gitlab.example.com
gitlab.token=12345678
```
####  Use the API

```
GitlabClient.get().getGroupService().createGroup("New Group", "newgroup");
```

####  Mock the Gitlab server in dev

We recommand to mock Gitlab in development mode and test profile.

Use the following configuration parameter:

```
gitlab.mock=true
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
