#!/bin/bash
# Get the app home
if [ -z "$TED_HOME" -o ! -d "$TED_HOME" ]; then
  PRG="$0"
  # need this for relative symlinks
  while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG=`dirname "$PRG"`"/$link"
    fi
  done

  TED_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  TED_HOME=`cd "$TED_HOME" > /dev/null && pwd`
fi

# TED main class
TED_CLASS=pw.phylame.ted.AppKt

# Set extension JAR
TED_CLASS_PATH=""
LIB_DIR="$TED_HOME"/lib
EXT_DIR="$TED_HOME"/lib/ext

find_jars(){
if [ -d "$1" ]; then
  for file in "$1"/*.jar; do
    TED_CLASS_PATH="$TED_CLASS_PATH:$file"
  done
  if [ -n "$TED_CLASS_PATH" ]; then
    len=`expr length "$TED_CLASS_PATH"`
    TED_CLASS_PATH=`expr substr "$TED_CLASS_PATH" 2 "$len"`
  fi
fi
}

find_jars ${LIB_DIR}
find_jars ${EXT_DIR}

# Run Jem SCI
java -cp "${TED_CLASS_PATH}" ${TED_CLASS} "$@"
