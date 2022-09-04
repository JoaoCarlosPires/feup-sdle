#!/usr/bin/env bash

argc=$#
if (( argc != 3 ))
then
	echo "Usage: $0 <publisher_id> <topic> <content>"
	exit 1
fi

cd ..
gradle run -Pfile=pt/up/fe/sdle/reliableps/testapp/PublisherTestApp --args="$1 $2 $3"