# Demo JIRA Auditing Plugin

## Prerequisites
### Install Atlassian SDK

On a Mac using homebrew

```bash
brew tap atlassian/tap
brew install atlassian/tap/atlassian-plugin-sdk
```

## Build instructions

From the root of this repo...

### Start local JIRA container
```bash
atlas-debug -DskipTests
```

### Run Integration Tests

```bash
atlas-integration-test
```

### Package
```
mvn package
```