#!/usr/bin/env bash

argc=$#
if (( argc != 1 ))
then
	echo "Usage: $0 <id>"
	exit 1
fi

cd ..
gradle run -Pfile=pt/up/fe/sdle/reliableps/Subscriber --args=$1