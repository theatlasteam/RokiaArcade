#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./build.sh
mkdir -p build/test
javac -d build/test -cp ../lib/midpapi20.jar:../lib/cldcapi11.jar src/arcade/SnakeLogic.java src/arcade/Grid2048.java src/arcade/BreakLogic.java test/ArcadeTest.java
java -cp build/test arcade.ArcadeTest
