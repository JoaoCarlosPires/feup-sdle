#!/usr/bin/env bash

argc=$#
if (( argc != 3 ))
then
	echo "Usage: $0 <subscriber_id> <action> <topic>"
	exit 1
fi

cd ..
gradle run -Pfile=pt/up/fe/sdle/reliableps/testapp/SubscriberTestApp --args="$1 $2 $3"