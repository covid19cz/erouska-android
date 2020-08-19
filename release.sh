#!/bin/bash

# save current branch into variable to be able to return to it later
currentBranch=$(git symbolic-ref --short HEAD)
git stash
git checkout master
git pull
git rebase origin/develop

commitCount=$(git rev-list HEAD --count)

git tag -a "$commitCount" HEAD -m "Android Release $commitCount"
git push origin master:master --tags
git checkout "$currentBranch"
git stash apply