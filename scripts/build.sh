#!/usr/bin/env bash

set -e
now=$(date +'%Y-%m-%dT%T%z')
version=$(git rev-parse --short HEAD)
package="gitlab.com/bnpl-hdbank/api/core/pkg/server"

go build -a -ldflags "-X $package.version=$version -X $package.buildTime=$now" -o server cmd/api/main.go
