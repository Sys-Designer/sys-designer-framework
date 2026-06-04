 # Copyright (C) Sys Designer. 2026-2026 .All Rights Reserved.

#!/usr/bin/env bash
set -e
DIR=$(realpath "$(dirname "$0")")
cd "$DIR/../"
WORKDIR=$(pwd)
output_path="$WORKDIR/output"
function build_model() {
    cd "$WORKDIR/$1"
    mvn clean
    mvn install
    if [ -d "./target" ]; then
        cp "target/*.jar $output_path"
    fi
    cd "$WORKDIR"
}

if [ -d "$output_path" ]; then
  rm -rf ./output
fi
mkdir "$output_path"

build_model sys-designer-framework-parent
build_model sys-designer-framework-core
build_model sys-designer-framework-spring-boot
build_model sys-designer-framework-web-spring-boot
