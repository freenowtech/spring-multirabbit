# Contributing Guides
Thank you for investing your time in contributing to Spring MultiRabbit!

In this guide you will get an overview of the contribution workflow from opening an issue, creating a PR, reviewing, and
merging the PR.

## How to Get Started
To get an overview of the project, read the [README](README.md).

If you spot a problem with Spring MultiRabbit, search if an issue already exists. If not, create an issue, so it can
serve as a centralized thread of communication with you, maintainers and the community.

#### Did you find a bug or have a feature proposal?
- Search if an issue is already registered. If not, create one
- Fork the repository to your own GitHub account and work from your fork's `main` branch
    - Make sure to have a working build and make sure to be covering the changes with tests
    - In case of changes of the usability of the library, please also provide changes on the documentation and examples
- When finished, create a PR to the original `main` branch. If it's a change that should be limited to a specific
  release, create the PR to the specific release instead (e.g. `release/2.5`)
- Be patience, so maintainers can review and merge your code

## Release Flow
- The `main` branch has the latest updates and compatibility of the library.
- Every PR is merged against the `main` branch, except the changes valid only for specific releases.
- Once a PR is merged to the `main` branch, the commits are cherry-picked to the active releases, so they also include
  the latest updates of the library. Note that this might be affected to some further changes so the code is compatible
  with previous releases.
