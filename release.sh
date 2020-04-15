#!/bin/bash

# save current branch into variable to be able to return to it later
currentBranch=$(git symbolic-ref --short HEAD)

git checkout master
git pull
git rebase origin/develop
git push origin master:master
git checkout $currentBranch